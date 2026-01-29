from fastapi import FastAPI, Depends
from config.configurations import settings
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import uvicorn
from config.security import get_current_user


@asynccontextmanager
async def startup_event(app: FastAPI):
    # This runs right as the server starts
    print(f"🚀 Service started with Cloudinary: {settings.cloudinary_cloudname}")
    yield
    # --- SHUTDOWN ---
    print("Shutting down AI Service...")


app = FastAPI(title="OBTB-AI-SERVICE", lifespan=startup_event)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.angular_base_uri, settings.authservice_base_uri, settings.busservice_base_uri, settings.userservice_base_uri],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=["*"]
)
@app.get("/ai/validate-session")
async def validate_session(token_data: dict = Depends(get_current_user)):
    return {
        "status": "Authorized",
        "user": token_data.get("username"),
        "roles": token_data.get("roles")
    }

if __name__ == "__main__":
    # This keeps the process alive and starts the server on port 8000
    uvicorn.run(app, host="0.0.0.0", port=8087)


