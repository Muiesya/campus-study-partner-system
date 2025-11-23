import os
from pydantic import BaseSettings


class Settings(BaseSettings):
    database_url: str = os.getenv("DATABASE_URL", "sqlite:///./app.db")
    app_name: str = "校园学习伙伴推荐系统 API"
    debug: bool = True

    class Config:
        env_file = ".env"


settings = Settings()
