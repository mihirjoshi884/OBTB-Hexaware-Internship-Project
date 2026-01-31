from fastapi import APIRouter, Depends
from app.config.security import get_current_user

router = APIRouter(
    prefix="/ai/id-verify",
    tags=["ID Verification"],
    dependencies=[Depends(get_current_user)] # Protects every route in this file
)
