import httpx
import time
from app.config.configurations import settings

class OAuthClient:
    def __init__(self):
        self.client_id = settings.spring_security_oauth2_client_registration_client_client_id
        self.client_secret = settings.spring_security_oauth2_client_registration_client_client_secret
        self.token_endpoint = settings.spring_security_oauth2_client_provider_oauthservice_token_uri
        self._access_token = None


    async def get_access_token(self):
        if self._access_token and time.time() < self._expires_at:
            return self._access_token

        async with httpx.AsyncClient() as client:
            response = await client.post(
                self.token_url,
                auth=(self.client_id, self.client_secret),
                data={
                    "grant_type": "client_credentials",
                    "scope": "openid profile"
                }
            )
            response.raise_for_status()
            data = response.json()

            self._access_token = data["access_token"]
            # Buffer of 30 seconds to prevent edge-case expirations
            self._expires_at = time.time() + data["expires_in"] - 30

            return self._access_token

# Global instance
ai_oauth_client = OAuthClient()