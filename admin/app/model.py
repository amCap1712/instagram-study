from enum import Enum

from pydantic import BaseModel, Field

from app.db import CollectionType, CollectionSource, DownloadStatus


class Submission(BaseModel):
    survey_user_id: int
    user_id: str
    collection_id: str | None
    source: CollectionSource | None
    type_: CollectionType = Field(alias="type")
    items: list[dict]


class SubmissionResult(BaseModel):
    collection_id: str


class SurveyResult(BaseModel):
    id: int


class UsageResult(BaseModel):
    id: int


class MediaType(Enum):
    IMAGES = "images"
    VIDEOS = "videos"


class DownloadItem(BaseModel):
    url: str
    media_type: MediaType
    item: dict | None
    order: int = 0
    path: str | None
    status: DownloadStatus | None


class InjectItemResponse(BaseModel):
    items: list[dict]
