from fastapi import APIRouter,Request ,Depends, HTTPException, Header
import hmac
import hashlib
from app.config.configurations import settings
from app.config.oauth_client import OAuthClient
import httpx

router = APIRouter(prefix="/ai/webhook", tags=["Webhook"])

@router.post("/cloudinary")
async def cloudinary_webhook(
        request: Request,
    x_cld_signature: str = Header(None),
    x_cld_timestamp: str = Header(None)):
    # 1. Get raw body for signature verification
    body = await request.body()
    body_str = body.decode("utf-8")

    if x_cld_signature:
        to_sign = f"{body_str}{x_cld_timestamp}{settings.cloudinary_api_secret}"
        expected_signature = hashlib.sha1(to_sign.encode("utf-8")).hexdigest()

        if not hmac.compare_digest(expected_signature, x_cld_signature):
            raise HTTPException(status_code=401, detail="Invalid webhook signature")
    else:
        print("⚠️ Skipping signature check (Manual Test Mode)")

    if not hmac.compare_digest(expected_signature, x_cld_signature):
        raise HTTPException(status_code=401, detail="Invalid webhook signature")

    payload = await request.json()
    public_ID = payload("public_id")

    return {"status": "ok"}

