import os


class Settings:
    PROJECT_NAME: str = "TRAIN_INFO_SYSTEM"

    DB_HOST: str = os.getenv("DB_HOST", "db")
    DB_PORT: int = int(os.getenv("DB_PORT", 5432))
    DB_USER: str = os.getenv("DB_USER", "train_user")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "train_pass")
    DB_NAME: str = os.getenv("DB_NAME", "train_db")

    @property
    def database_url(self) -> str:
        return (
            f"postgresql+psycopg2://{self.DB_USER}:{self.DB_PASSWORD}"
            f"@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
        )


settings = Settings()
