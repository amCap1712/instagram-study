import asyncio
import json
import logging
import uuid
from copy import deepcopy
from datetime import datetime, timedelta
from json import JSONDecodeError
from pathlib import Path
from urllib.parse import urlparse

import requests
from aiofile import async_open
from aiohttp import ClientSession
from aiohttp_retry import ExponentialRetry, RetryClient
from bs4 import BeautifulSoup
from sqlalchemy.dialects.postgresql import insert
from sqlmodel import Session

from app.db import InjectItem, get_engine

logger = logging.getLogger(__name__)

semaphore = asyncio.Semaphore(10)
retry_options = ExponentialRetry(attempts=1)

BASE_URL = "https://kiran-research2.comminfo.rutgers.edu/data-collector-admin/"


async def download(client: RetryClient, url, path):
    try:
        async with semaphore, client.get(url) as response:
            response.raise_for_status()
            data = await response.read()

            Path(path).parent.mkdir(parents=True, exist_ok=True)
            async with async_open(path, "wb+") as afp:
                await afp.write(data)
    except Exception as e:
        print(e)


async def download_batch(items: list[tuple[str, Path]]):
    async with ClientSession() as downloader:
        client = RetryClient(downloader, retry_options=retry_options)
        tasks = []
        for (url, path) in items:
            tasks.append(download(client, url, path))
        return await asyncio.gather(*tasks)


def get_storage_path(user_id, pk, url):
    media_name = str(urlparse(url).path).split('/')[-1]
    sub_path = Path("media", str(user_id), str(pk), str(uuid.uuid4()), media_name)
    local_url = BASE_URL + str(sub_path)
    path = Path.cwd() / sub_path
    return url, local_url, path


def transform_inject_feed_item(item):
    downloads = []
    item["cursor"] = ""

    item["node"] = {
        "media": item.pop("node"),
        "ad": None,
        "explore_story": None,
        "end_of_feed_demarcator": None,
        "stories_netego": None,
        "suggested_users": None,
        "bloks_netego": None,
        "__typename": "XDTFeedItem"
    }

    media = item["node"]["media"]
    media["inventory_source"] = "media_or_ad"
    media["pk"] = media["id"]
    media["id"] = media["id"] + "_" + media["owner"]["id"]
    media["owner"]["__typename"] = "XDTUserDict"
    media["owner"]["pk"] = media["owner"]["id"]
    media["user"] = dict(**media["owner"])

    if "location" in media and media["location"] is not None:
        media["location"]["pk"] = media["location"]["id"]

    edge_media_to_caption = media.pop("edge_media_to_caption")["edges"]
    if len(edge_media_to_caption) > 0:
        media["caption"] = edge_media_to_caption[0].pop("node")
        media["caption"].update({"has_translation": False, "pk": 1})

    candidates = []
    for media_item in media.pop("display_resources", []):
        download_url, local_url, path = get_storage_path(media["owner"]["id"], media["pk"], media_item["src"])
        downloads.append((download_url, path))
        candidates.append({
            "url": local_url,
            "height": media_item["config_height"],
            "width": media_item["config_width"]
        })
    media["image_versions2"] = {"candidates": candidates}

    dimensions = media.pop("dimensions")
    media["original_height"] = dimensions["height"]
    media["original_width"] = dimensions["width"]

    if "video_url" in media:
        video_url = media.pop("video_url")
        download_url, local_url, path = get_storage_path(media["owner"]["pk"], media["pk"], video_url)
        downloads.append((download_url, path))
        media["video_versions"] = [{
            "type": 101,
            "url": local_url,
            **dimensions
        }]
        media["media_type"] = 2
    else:
        media["video_versions"] = None

    if "edge_sidecar_to_children" in media:
        children = media.pop("edge_sidecar_to_children")["edges"]
        media["carousel_media_count"] = len(children)
        carousel_media = []
        for _child in children:
            child_node = _child["node"]
            child_node["pk"] = child_node["id"]
            child_node["id"] = child_node["id"] + "_" + media["owner"]["id"]
            child_node["is_dash_eligible"] = None
            child_node["video_dash_manifest"] = None

            child_dimensions = child_node.pop("dimensions", [])
            child_node["original_height"] = child_dimensions["height"]
            child_node["original_width"] = child_dimensions["width"]

            child_node["preview"] = child_node.pop("media_preview", None)
            child_node["organic_tracking_token"] = child_node.pop("tracking_token", None)

            child_node_candidates = []
            for child_item in child_node.pop("display_resources", []):
                download_url, local_url, path = get_storage_path(media["owner"]["id"], media["pk"], item["src"])
                downloads.append((download_url, path))
                child_node_candidates.append({
                    "url": local_url,
                    "height": child_item["config_height"],
                    "width": child_item["config_width"]
                })
            child_node["image_versions2"] = {"candidates": child_node_candidates}

            child_node["carousel_parent_id"] = media["id"]
            child_node.pop("edge_media_to_tagged_user", None)
            child_node.pop("display_url", None)
            child_node.pop("__typename", None)
            child_node.pop("gating_info", None)
            child_node.pop("__typename", None)
            child_node.pop("has_upcoming_event", None)
            child_node.pop("fact_check_information", None)
            child_node.pop("fact_check_overall_rating", None)
            child_node.pop("sensitivity_friction_info", None)
            child_node.pop("is_video", None)
            child_node.update({
                "has_audio": None,
                "headline": None,
                "carousel_media": None,
                "usertags": None,
                "link": None,
                "story_cta": None
            })
            if "video_url" in child_node:
                video_url = child_node.pop("video_url")
                download_url, local_url, path = get_storage_path(media["owner"]["pk"], media["pk"], video_url)
                downloads.append((download_url, path))
                child_node["video_versions"] = [{
                    "type": 101,
                    "url": local_url,
                    **child_dimensions
                }]
                child_node["media_type"] = 2
            else:
                child_node["video_versions"] = None

            if "media_type" not in child_node:
                child_node["media_type"] = 1

            carousel_media.append(child_node)

        media["carousel_media"] = carousel_media
        media["media_type"] = 8
    else:
        media["carousel_media_count"] = None
        media["carousel_media"] = None

    coauthor_producers = media.pop("coauthor_producers", None)
    if coauthor_producers:
        for coauthor in coauthor_producers:
            coauthor["pk"] = coauthor["id"]
        media["coauthor_producers"] = coauthor_producers

    media["preview"] = media.pop("media_preview")
    media["code"] = media.pop("shortcode")
    media["comment_count"] = media.pop("edge_media_to_comment")["count"]
    media["like_count"] = media.pop("edge_media_preview_like")["count"]
    media["taken_at"] = media.pop("taken_at_timestamp")
    media["organic_tracking_token"] = media.pop("tracking_token")
    media["can_viewer_reshare"] = media.pop("viewer_can_reshare")
    media["view_count"] = media.pop("video_view_count", None)

    media.pop("__typename")
    media.pop("gating_info", None)
    media.pop("fact_check_information", None)
    media.pop("fact_check_overall_rating", None)
    media.pop("sensitivity_friction_info", None)
    media.pop("thumbnail_src", None)
    media.pop("thumbnail_resources", None)
    media.pop("viewer_has_saved", None)
    media.pop("viewer_has_saved_to_collection", None)
    media.pop("viewer_has_liked", None)
    media.pop("pinned_for_users", None)
    media.pop("edge_media_to_tagged_user", None)
    media.pop("edge_media_to_sponsor_user", None)
    media.pop("is_affiliate", None)
    media.pop("display_url", None)
    media.pop("has_upcoming_event", None)
    media.pop("viewer_in_photo_of_you", None)
    media.pop("nft_asset_info", None)
    media.pop("dash_info", None)
    media.pop("is_video", None)

    if "media_type" not in media:
        media["media_type"] = 1

    media.update({
        "view_state_item_type": 128,
        "like_and_view_counts_disabled": False,
        "explore": None,
        "main_feed_carousel_starting_media_id": None,
        "audience": None,
        "is_seen": False,
        "has_liked": False,
        "carousel_parent_id": None,
        "clips_attribution_info": None,
        "comments": [],
        "affiliate_info": None,
        "follow_hashtag_info": None,
        "accessibility_caption": None,
        "brs_severity": None,
        "comments_disabled": None,
        "commenting_disabled_for_viewer": None,
        "has_viewer_saved": None,
        "link": None,
        "story_cta": None,
        "can_reshare": None,
        "social_context": [],
        "photo_of_you": None,
        "media_level_comment_controls": None,
        "feed_recs_demotion_control": None,
        "feed_demotion_control": None,
        "share_urls": None,
        "usertags": None,
        "boost_unavailable_identifier": None,
        "boost_unavailable_reason": None,
        "boosted_status": None,
        "can_see_insights_as_brand": False,
        "caption_is_edited": False,
        "clips_metadata": None,
        "facepile_top_likers": [],
        "top_likers": [],
        "sponsor_tags": None,
        "saved_collection_ids": None,
        "headline": None,
        "expiring_at": None,
        "invited_coauthor_producers": [],
        "visibility": None,
        "ig_media_sharing_disabled": False,
        "video_dash_manifest": None,
    })
    return item, downloads


def transform_inject_feed(body):
    items = []
    all_downloads = []
    for item in body["data"]["user"]["edge_owner_to_timeline_media"]["edges"]:
        original = deepcopy(item)
        processed, downloads = transform_inject_feed_item(item)
        inject_item = InjectItem(
            user_id=item["node"]["media"]["owner"]["id"],
            item_id=item["node"]["media"]["id"],
            original=original,
            processed=processed,
            expires_at=datetime.now() + timedelta(hours=4)
        )
        items.append(inject_item)
        all_downloads.extend(downloads)
    return items, all_downloads


def download_posts(user_id):
    params = {
        "doc_id": 7571407972945935,
        "variables": json.dumps(
            {"id": user_id, "after": None, "first": 12},
            separators=(",", ":"))
    }
    url = "https://www.instagram.com/graphql/query/"
    response = requests.get(url, params=params)
    response.raise_for_status()
    return response.json()


def find_user_id_in_script(data):
    if not data:
        return

    if isinstance(data, list):
        for item in data:
            user_id = find_user_id_in_script(item)
            if user_id:
                return user_id

    if isinstance(data, dict):
        if "props" in data and "id" in data["props"]:
            return data["props"]["id"]

        for value in data.values():
            if isinstance(value, dict) or isinstance(value, list):
                user_id = find_user_id_in_script(value)
                if user_id:
                    return user_id


def find_user_id(username):
    response = requests.get(f"https://www.instagram.com/{username}/")
    response.raise_for_status()
    soup = BeautifulSoup(response.text, features="html.parser")

    user_id = None
    script = soup.script
    while script:
        try:
            data = json.loads(script.text)
            user_id = find_user_id_in_script(data)
            if user_id:
                break
        except JSONDecodeError:
            pass
        script = script.next

    return user_id


async def download_inject_items(user_id):
    with Session(get_engine()) as session:
        response = download_posts(user_id)
        inject_items, all_downloads = transform_inject_feed(response)
        await download_batch(all_downloads)
        for item in inject_items:
            statement = insert(InjectItem).values(
                user_id=item.user_id,
                item_id=item.item_id,
                original=item.original,
                processed=item.processed,
                expires_at=item.expires_at
            ).on_conflict_do_update(
                index_elements=["item_id"],
                set_=dict(
                    original=item.original,
                    processed=item.processed,
                    expires_at=item.expires_at
                )
            ).returning(InjectItem.id)
            session.execute(statement)
        session.commit()
