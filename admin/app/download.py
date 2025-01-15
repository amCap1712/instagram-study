import asyncio
import json
from pathlib import Path

from aiofile import async_open
from aiohttp import ClientSession
from aiohttp_retry import RetryClient, ExponentialRetry

from urllib.parse import urlparse

from sqlalchemy.orm import joinedload
from sqlmodel import Session, select

from app.db import CollectionSource, DownloadStatus, get_engine, Item
from app.model import DownloadItem, MediaType

semaphore = asyncio.Semaphore(10)
retry_options = ExponentialRetry(attempts=1)


async def download(client: RetryClient, submitted_item: Item, download_item: DownloadItem) \
        -> tuple[Item, DownloadItem, DownloadStatus]:
    try:
        async with semaphore, client.get(download_item.url) as response:
            response.raise_for_status()
            data = await response.read()
            async with async_open(download_item.path, "wb+") as afp:
                await afp.write(data)
        download_status = DownloadStatus.SUCCESS
    except Exception as e:
        print(e)
        download_status = DownloadStatus.FAILED

    return submitted_item, download_item, download_status


async def download_batch(items: list[tuple[Item, list[DownloadItem]]]) \
        -> tuple[tuple[Item, DownloadItem, DownloadStatus]]:
    async with ClientSession() as downloader:
        client = RetryClient(downloader, retry_options=retry_options)
        tasks = []
        for (submitted_item, items_to_download) in items:
            for download_item in items_to_download:
                tasks.append(download(client, submitted_item, download_item))
        return await asyncio.gather(*tasks)


def download_image_versions2(item) -> str | None:
    if images := item.get("image_versions2"):
        return images["candidates"][0]["url"]
    return None


def download_video_versions(item) -> str | None:
    if videos := item.get("video_versions"):
        return videos[0]["url"]
    return None


def download_media_version(item) -> list[DownloadItem]:
    downloads = []
    if url := download_image_versions2(item):
        downloads.append(DownloadItem(url=url, media_type=MediaType.IMAGES))
    if url := download_video_versions(item):
        downloads.append(DownloadItem(url=url, media_type=MediaType.VIDEOS))
    return downloads


def download_multiple_media(items) -> list[DownloadItem]:
    downloads = []
    for idx, item in enumerate(items):
        for download_item in download_media_version(item):
            download_item.order = idx
            downloads.append(download_item)
    return downloads


def download_media_or_ad(media_or_ad) -> list[DownloadItem]:
    if carousel_media := media_or_ad.get("carousel_media"):
        return download_multiple_media(carousel_media)
    elif items := media_or_ad.get("items"):
        return download_multiple_media(items)
    else:
        if downloads := download_media_version(media_or_ad):
            for download_item in downloads:
                download_item.order = 0
            return downloads
    return []


def download_explore_story(item) -> list[DownloadItem]:
    if media_or_ad := item.get("media"):
        return download_media_or_ad(media_or_ad)
    return []


def download_end_of_feed_demarcator(item) -> list[DownloadItem]:
    urls = []
    for group in item["group_set"]["groups"]:
        for feed_item in group["feed_items"]:
            urls.extend(download_feed_item(feed_item))
    return urls


def download_feed_item(item: dict) -> list[DownloadItem]:
    downloads = []
    if media_or_ad := item.get("media"):
        downloads = download_media_or_ad(media_or_ad)
    elif media_or_ad := item.get("ad"):
        downloads = download_media_or_ad(media_or_ad)
    elif explore_story := item.get("explore_story"):
        downloads = download_explore_story(explore_story)
    elif end_of_feed_demarcator := item.get("end_of_feed_demarcator"):
        pass
         # urls = download_end_of_feed_demarcator(end_of_feed_demarcator)
    else:
        print(json.dumps(item, indent=4))
    return downloads


def download_reel(item: dict) -> list[DownloadItem]:
    return [DownloadItem(order=0, item=item, media_type=MediaType.VIDEOS, url=item["media"]["video_versions"][0]["url"])]


def download_explore_clip_items(start, items) -> list[DownloadItem]:
    urls = []
    for idx, item in enumerate(items):
        if downloads := download_media_version(item["media"]):
            for download_item in downloads:
                download_item.order = start + idx
            return downloads
    return urls


def download_explore_item(item: dict) -> list[DownloadItem]:
    downloads = []
    try:
        layout_content = item["layout_content"]
        downloads.extend(download_explore_clip_items(0, layout_content["one_by_two_item"]["clips"]["items"]))
        downloads.extend(download_explore_clip_items(len(downloads), layout_content["fill_items"]))
        return downloads
    except (KeyError, TypeError):
        print("Error while finding explore items in: ", json.dumps(item))
        return []


def prepare_downloads(item: Item, items_to_download: list[DownloadItem]):
    collection = item.collection
    for download_item in items_to_download:
        if not download_item:
            continue

        media_name = str(urlparse(download_item.url).path).split('/')[-1]
        dir_path = Path.cwd() / "media" / download_item.media_type.value / str(collection.user_id) / \
            collection.source.value / str(collection.id) / str(item.rank) / str(download_item.order)
        dir_path.mkdir(parents=True, exist_ok=True)

        download_item.path = dir_path / media_name


def download_submission_item(item: Item):
    if item.collection.source == CollectionSource.FEED:
        download_func = download_feed_item
    elif item.collection.source == CollectionSource.REELS:
        download_func = download_reel
    else:
        download_func = download_explore_item
    items_to_download = download_func(item.data)
    prepare_downloads(item, items_to_download)
    return items_to_download


async def download_submission(item_ids: list[int]):
    inputs = []
    with Session(get_engine()) as session:
        query = select(Item).where(Item.id.in_(item_ids)).options(joinedload(Item.collection))
        submitted_items = session.scalars(query)

        for submitted_item in submitted_items:
            items_to_download = download_submission_item(submitted_item)
            inputs.append((submitted_item, items_to_download))

        results = await download_batch(inputs)
        status = {}
        for submitted_item, _, download_status in results:
            if submitted_item.id not in status or status[submitted_item.id] != DownloadStatus.FAILED:
                status[submitted_item.id] = download_status

        for submitted_item in submitted_items:
            submitted_item.download_status = status[submitted_item.id]

        session.commit()
