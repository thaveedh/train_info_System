from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from api.core.db import Base, engine
from api.routers.query_router import router as query_router
from api.routers.train_list_router import router as train_list_router
from api.core.wait_for_db import wait_for_db

wait_for_db()

app = FastAPI(title="Train Info System")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

Base.metadata.create_all(bind=engine)

app.include_router(train_list_router)
app.include_router(query_router)

@app.get("/")
def root():
    return {"message": "Train Info System backend up"}
