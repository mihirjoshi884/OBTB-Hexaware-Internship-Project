import httpx
from jose import jwt, JWTError
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from app.config.configurations import settings
import requests

# This tells FastAPI to look for the "Authorization: Bearer <token>" header
oauth2_scheme = OAuth2PasswordBearer(
    tokenUrl=settings.spring_security_oauth2_client_provider_oauthservice_token_uri
)


class SecurityManager:
    def __init__(self):
        # Ensure your settings use the synced names (dots/dashes replaced by underscores)
        self.jwks_url = f"{settings.spring_security_oauth2_client_provider_oauthservice_issuer_uri}/oauth2/jwks"
        self._cached_keys = None

    async def get_public_key(self):
        """Fetches and caches the Public Key Set (JWKS) from Spring Auth Service"""
        if not self._cached_keys:
            async with httpx.AsyncClient() as client:
                response = await client.get(self.jwks_url)
                response.raise_for_status()
                self._cached_keys = response.json()["keys"]
        return self._cached_keys

    async def validate_token(self, token: str):
        try:
            header = jwt.get_unverified_header(token)
            kid = header.get("kid")

            # Fetch fresh keys from Spring
            jwks = requests.get(f"{settings.authservice_base_uri}/oauth2/jwks").json()

            # Find the specific key matching the token's kid
            key = next((k for k in jwks["keys"] if k["kid"] == kid), None)

            if not key:
                print(f"❌ No matching key found for kid: {kid}")
                raise JWTError("Public key not found")

            return jwt.decode(
                token,
                key,
                algorithms=["RS256"],
                audience="obtb-client-007",
                issuer="http://localhost:8081"
            )
        except Exception as e:
            print(f"❌ Signature failed: {str(e)}")
            raise HTTPException(status_code=401, detail=f"Signature verification failed: {str(e)}")

# Create the instance to be used as a Dependency in your routes
security_manager = SecurityManager()
get_current_user = security_manager.validate_token