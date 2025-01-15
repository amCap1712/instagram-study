import json
import logging
import os
import pathlib
from typing import Annotated

import sentry_sdk
from dotenv import load_dotenv
from fastapi import FastAPI, Request, Path
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from sqlalchemy.orm import joinedload
from sqlmodel import Session, select, col
from starlette import status
from starlette.background import BackgroundTasks
from starlette.responses import HTMLResponse, JSONResponse, Response
from starlette.staticfiles import StaticFiles
from starlette.templating import Jinja2Templates

from .download import download_submission
from .db import get_engine, init_engine, Collection, Item, CollectionSource, DownloadStatus, CollectionType, Survey, \
    Usage, InjectItem
from .inject import find_user_id, download_inject_items
from .model import Submission, SubmissionResult, SurveyResult, UsageResult, InjectItemResponse

load_dotenv()
init_engine()

logger = logging.getLogger("uvicorn.error")

sentry_sdk.init(dsn=os.environ.get("SENTRY_DSN"), enable_tracing=True)


app = FastAPI()
app.mount("/media", StaticFiles(directory="media"), name="media")

templates = Jinja2Templates(directory="templates")


@app.post("/survey/")
async def submit_survey(survey: Survey):
    with Session(get_engine()) as session:
        session.add(survey)
        session.commit()
        return SurveyResult(id=survey.id)


@app.post("/usage/")
async def submit_usage(usage: Usage):
    with Session(get_engine()) as session:
        session.add(usage)
        session.commit()
        return UsageResult(id=usage.id)


@app.post("/{source}/")
async def submit_data(
    source: Annotated[CollectionSource, Path(title="The source of the data being submitted")],
    submission: Submission,
    background_tasks: BackgroundTasks
) -> SubmissionResult:
    with Session(get_engine()) as session:
        if submission.collection_id is not None:
            query = select(Collection).where(Collection.id == submission.collection_id)
            collection = session.exec(query).first()
        else:
            collection = None

        if collection is None:
            collection = Collection(
                user_id=submission.user_id,
                source=source,
                type_=submission.type_,
                survey_user_id=submission.survey_user_id
            )
            session.add(collection)
            session.flush()
            session.refresh(collection)

        submission.collection_id = collection.id

        items = []
        for submitted_item in submission.items:
            item = Item(collection_id=collection.id, rank=submitted_item["rank"],
                        data=submitted_item, download_status=DownloadStatus.PENDING)
            session.add(item)
            items.append(item)
        session.commit()

        item_ids = [item.id for item in items]

        background_tasks.add_task(download_submission, item_ids=item_ids)

        return SubmissionResult(collection_id=collection.id)


def retrieve_media(user_id: str, collection_id: str, collection_source: CollectionSource, media_type: str):
    media = []
    if user_id.isalnum() and collection_id.isalnum():
        base_path = pathlib.Path.cwd() / "media" / media_type
        dir_path = base_path / user_id / collection_source.value / collection_id
        for file in dir_path.glob("**/*"):
            if file.is_file():
                media.append(dir_path / file)
        media.sort(key=lambda p: (int(p.parent.parent.name), int(p.parent.name)))
        media = [str(p.relative_to(base_path)) for p in media]
    return media


@app.get("/feed/", response_class=HTMLResponse)
async def retrieve_feed(request: Request, user_id: str, collection_id: str):
    images = retrieve_media(user_id, collection_id, CollectionSource.FEED, "images")
    return templates.TemplateResponse("feed.html", {
        "request": request,
        "images": images,
        "prefix": os.environ.get("ROOT_PATH_PREFIX", "")
    })


@app.get("/reels/", response_class=HTMLResponse)
async def retrieve_reels(request: Request, user_id: str, collection_id: str):
    videos = retrieve_media(user_id, collection_id, CollectionSource.REELS, "videos")
    return templates.TemplateResponse("reels.html", {
        "request": request,
        "videos": videos,
        "prefix": os.environ.get("ROOT_PATH_PREFIX", "")
    })


@app.get("/explore/", response_class=HTMLResponse)
async def retrieve_explore(request: Request, user_id: str, collection_id: str):
    images = retrieve_media(user_id, collection_id, CollectionSource.EXPLORE, "images")
    return templates.TemplateResponse("explore.html", {
        "request": request,
        "images": images,
        "prefix": os.environ.get("ROOT_PATH_PREFIX", "")
    })


@app.get("/", response_class=HTMLResponse)
async def list_collections(request: Request):
    with Session(get_engine()) as session:
        query = select(Collection).order_by(col(Collection.created_at).desc())
        items = session.exec(query).all()
    return templates.TemplateResponse("list.html", {
        "request": request,
        "items": items
    })


@app.exception_handler(RequestValidationError)
async def request_validation_error_handler(request: Request, exc: RequestValidationError):
    content = jsonable_encoder({"detail": exc.errors(), "body": exc.body})
    logger.error("Validation error: %s", json.dumps(content))
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content=content,
    )


@app.get("/quantify-feed/", response_class=HTMLResponse)
async def quantify_feed(request: Request, collection_ids: str):
    collection_ids = [int(_id) for _id in collection_ids.split(",")]
    with Session(get_engine()) as session:
        query = select(Collection)\
            .filter(Collection.id.in_(collection_ids), Collection.source == CollectionSource.FEED)\
            .order_by(Collection.created_at)\
            .options(joinedload(Collection.items))
        results = session.scalars(query).unique()

        actives = []
        passives = []
        all_active_quantification = {
            "media": set(),
            "ads": set(),
            "explore_story": set(),
            "end_of_feed_demarcator": set()
        }
        all_passive_quantification = {
            "media": set(),
            "ads": set(),
            "explore_story": set(),
            "end_of_feed_demarcator": set()
        }

        for collection in results:
            quantification = {
                "collection": collection,
                "media": set(),
                "ads": set(),
                "explore_story": set(),
                "end_of_feed_demarcator": set()
            }

            for item in collection.items:
                try:
                    if media_or_ad := item.original.get("media_or_ad"):
                        if ad_id := media_or_ad.get("ad_id"):
                            quantification["ads"].add(ad_id)
                        else:
                            quantification["media"].add(media_or_ad["id"])
                    elif explore_story := item.original.get("explore_story"):
                        quantification["explore_story"].add(explore_story["id"])
                    elif end_of_feed_demarcator := item.original.get("end_of_feed_demarcator"):
                        quantification["end_of_feed_demarcator"].add(end_of_feed_demarcator["id"])
                    else:
                        print(json.dumps(item.original, indent=4))
                except Exception as e:
                    print(e)
                    print(json.dumps(item.original, indent=4))

            if collection.type_ == CollectionType.ACTIVE:
                actives.append(quantification)
                total_category = all_active_quantification
            else:
                passives.append(quantification)
                total_category = all_passive_quantification

            total_category["media"].update(quantification["media"])
            total_category["ads"].update(quantification["ads"])
            total_category["explore_story"].update(quantification["explore_story"])
            total_category["end_of_feed_demarcator"].update(quantification["end_of_feed_demarcator"])

        difference_quantification = {
            "media": all_active_quantification["media"].difference(all_passive_quantification["media"]),
            "ads": all_active_quantification["ads"].difference(all_passive_quantification["ads"]),
            "explore_story": all_active_quantification["explore_story"].difference(all_passive_quantification["explore_story"]),
            "end_of_feed_demarcator": all_active_quantification["end_of_feed_demarcator"].difference(all_passive_quantification["end_of_feed_demarcator"]),
        }

        return templates.TemplateResponse("quantify.html", {
            "request": request,
            "actives": actives,
            "passives": passives,
            "all_active_quantification": all_active_quantification,
            "all_passive_quantification": all_passive_quantification,
            "difference_quantification": difference_quantification
        })


@app.post("/inject/username/{username}/")
async def download_inject_items_username(username: Annotated[str, Path(title="The instagram username to fetch posts for")]):
    user_id = find_user_id(username)
    await download_inject_items(user_id)
    return Response(status_code=201)


@app.post("/inject/user_id/{user_id}/")
async def download_inject_items_handler_user_id(user_id: Annotated[str, Path(title="The user id of the instagram user to fetch posts for")]):
    await download_inject_items(user_id)
    return Response(status_code=201)


@app.get("/inject/")
async def retrieve_injected_items() -> InjectItemResponse:
    with Session(get_engine()) as session:
        result = session.query(InjectItem)
        response = [x.processed for x in result]
        return InjectItemResponse(items=response)


@app.get("/privacy/", response_class=HTMLResponse)
def privacy(request: Request):
    return templates.TemplateResponse("privacy.html", {
        "request": request,
    })


@app.get("/study/", response_class=HTMLResponse)
def study(request: Request):
    return templates.TemplateResponse("study.html", {
        "request": request,
        "prefix": os.environ.get("ROOT_PATH_PREFIX", "")
    })
