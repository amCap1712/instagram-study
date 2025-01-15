import json
import logging
import os
import tempfile
import zipfile

from sqlalchemy import text

from app.db import init_engine, get_engine

logger = logging.getLogger(__name__)
handler = logging.StreamHandler()
handler.setFormatter(logging.Formatter("%(asctime)s - %(levelname)s - %(message)s"))
logger.addHandler(handler)


def export():
    engine = get_engine()

    logger.info("Starting export...")

    with engine.connect() as connection:
        results = connection.execute(text("""
            with t as (
               select s.prolific_id
                    , i.data
                    , row_number() over (partition by s.prolific_id order by c.created_at, i.rank) as rownum
                 from collection c
                 join survey s
                   on c.survey_user_id = s.id
                 join item i
                   on i.collection_id = c.id
                 where (data->>'media' IS NOT NULL OR data->>'ad' IS NOT NULL OR data->'explore_story'->>'media' IS NOT NULL)
            ), collect_feed as (
                select prolific_id
                       , jsonb_agg(data ORDER BY rownum) as feed
                  from t
              group by prolific_id
            )   select c.prolific_id
                     , s.politics
                     , s.ethnicity
                     , s.gender_identity
                     , s.birth_year
                     , s.can_contact
                     , s.email
                     , c.feed
                  from collect_feed c
                  join survey s
                    on c.prolific_id = s.prolific_id
        """))
        rows = results.all()

    logger.info("Retrieved %d data for users", len(rows))

    with tempfile.TemporaryDirectory() as tmp_dir, \
            zipfile.ZipFile("export.zip", mode="w", allowZip64=True) as archive:
        for row in rows:
            prolific_id = row.prolific_id

            os.makedirs(os.path.join(tmp_dir, prolific_id), exist_ok=True)

            info_file = f"{prolific_id}/info.json"
            info_file_path = os.path.join(tmp_dir, info_file)
            with open(info_file_path, "w") as f:
                json.dump({
                    "prolific_id": prolific_id,
                    "politics": row.politics,
                    "ethnicity": row.ethnicity,
                    "gender_identity": row.gender_identity,
                    "birth_year": row.birth_year,
                    "can_contact": row.can_contact,
                    "email": row.email,
                }, f, indent=2)

            feed_file = f"{prolific_id}/feed.json"
            feed_file_path = os.path.join(tmp_dir, feed_file)

            with open(feed_file_path, "w") as f:
                json.dump(row.feed, f, indent=2)

            archive.write(info_file_path, arcname=info_file)
            archive.write(feed_file_path, arcname=feed_file)

            logger.info(f"Exported data for user: %s", prolific_id)

    logger.info("Finished export.")


if __name__ == "__main__":
    init_engine()
    export()
