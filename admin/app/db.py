import enum
import os
from datetime import datetime
from typing import Optional

from sqlalchemy import Identity, Column, DateTime, func, Enum, Text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.engine import Engine
from sqlmodel import Field, SQLModel, Relationship, create_engine


class CollectionSource(enum.Enum):
    FEED = "feed"
    EXPLORE = "explore"
    REELS = "reels"


class CollectionType(enum.Enum):
    ACTIVE = "active"
    PASSIVE = "passive"


class DownloadStatus(enum.Enum):
    FAILED = "failed"
    SUCCESS = "success"
    PENDING = "pending"


class Collection(SQLModel, table=True):
    __tablename__ = "collection"

    id: Optional[int] = Field(
        default=None,
        sa_column_args=(Identity(always=True),),
        primary_key=True,
        nullable=False,
    )
    user_id: str = Field(sa_column=Column(Text()))
    survey_user_id: int = Field(foreign_key="survey.id")
    type_: CollectionType = Field(sa_column=Column("type", Enum(CollectionType)))
    source: CollectionSource = Field(sa_column=Column(Enum(CollectionSource)))
    created_at: datetime = Field(sa_column=Column(DateTime(timezone=True), server_default=func.now()))

    items: list["Item"] = Relationship(back_populates="collection")


class Item(SQLModel, table=True):
    __tablename__ = "item"

    id: Optional[int] = Field(
        default=None,
        sa_column_args=(Identity(always=True),),
        primary_key=True,
        nullable=False,
    )
    collection_id: int = Field(foreign_key="collection.id")
    rank: int
    data: dict = Field(default={}, sa_column=Column(JSONB))
    created_at: datetime = Field(sa_column=Column(DateTime(timezone=True), server_default=func.now()))

    download_status: DownloadStatus = Field(sa_column=Column(Enum(DownloadStatus)))

    collection: Optional[Collection] = Relationship(back_populates="items")


class Survey(SQLModel, table=True):
    __tablename__ = "survey"
    id: Optional[int] = Field(
        default=None,
        sa_column_args=(Identity(always=True),),
        primary_key=True,
        nullable=False,
    )
    prolific_id: str = Field(sa_column=Column(Text()))
    politics: str = Field(sa_column=Column(Text()))
    ethnicity: str = Field(sa_column=Column(Text()))
    gender_identity: str = Field(sa_column=Column(Text()))
    birth_year: int
    can_contact: str = Field(sa_column=Column(Text()))
    email: Optional[str] = Field(sa_column=Column(Text()))


class Usage(SQLModel, table=True):
    __tablename__ = "usage"
    id: Optional[int] = Field(
        default=None,
        sa_column_args=(Identity(always=True),),
        primary_key=True,
        nullable=False,
    )
    survey_user_id: int = Field(foreign_key="survey.id")
    usage_time: datetime = Field(sa_column=Column(DateTime(timezone=True)))


class InjectItem(SQLModel, table=True):
    __tablename__ = "inject_item"
    id: Optional[int] = Field(
        default=None,
        sa_column_args=(Identity(always=True),),
        primary_key=True,
        nullable=False,
    )
    user_id: str
    item_id: str = Field(unique=True)
    original: dict = Field(default={}, sa_column=Column(JSONB))
    processed: Optional[dict] = Field(default=None, sa_column=Column(JSONB))
    expires_at: datetime = Field(sa_column=Column(DateTime(timezone=True)))


_engine: Engine | None = None


def init_engine():
    global _engine
    _engine = create_engine(os.environ.get("SQLALCHEMY_DATABASE_URI"))


def get_engine() -> Engine:
    global _engine
    return _engine


def create_db_and_tables():
    SQLModel.metadata.create_all(_engine)


if __name__ == "__main__":
    init_engine()
    create_db_and_tables()
