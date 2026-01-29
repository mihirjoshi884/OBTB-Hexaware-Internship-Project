import os
from pydantic_settings import BaseSettings, SettingsConfigDict

import requests

config_server_url = os.getenv("config_server_url","http://localhost:8083")
app_name = "ai_service"
profile = os.getenv("app_profile","dev")


def sync_spring_config():
    """
    Manually fetches config from Spring and persists it to os.environ.
    This replaces the library call which was failing to parse the JSON correctly.
    """
    try:
        url = f"{config_server_url}/{app_name}/{profile}"
        print(f"🔄 Syncing from: {url}")

        response = requests.get(url)
        response.raise_for_status()
        data = response.json()

        if not data.get('propertySources'):
            print("⚠️ No property sources found on the server.")
            return

        # Extract the source dictionary from the first property source
        source = data['propertySources'][0]['source']

        # PERSISTENCE STEP: Map Spring keys to Environment Variables
        for key, value in source.items():
            # 1. Standardize the key for Pydantic
            # 'scope[0]' becomes 'SCOPE_0'
            env_key = (
                key.replace('.', '_')
                .replace('-', '_')
                .replace('[', '_')  # Replace [ with _
                .replace(']', '')  # Remove ]
                .upper()
            )
            os.environ[env_key] = str(value)
        print(f"✅ Successfully persisted {len(source)} properties to memory.")

    except Exception as e:
        print(f"❌ Sync failed: {e}")


# Trigger the manual sync before initializing Settings
sync_spring_config()


class Settings(BaseSettings):
    # Base URIs
    angular_base_uri: str
    authservice_base_uri: str
    busservice_base_uri: str
    userservice_base_uri: str

    # Cloudinary
    cloudinary_cloudname: str = ""
    cloudinary_apikey: str = ""
    cloudinary_apisecret: str = ""

    # OAuth2 Client Registration
    oauth2_client_registration_key: str
    spring_security_oauth2_client_registration_client_client_id: str
    spring_security_oauth2_client_registration_client_client_name: str
    spring_security_oauth2_client_registration_client_client_secret: str
    spring_security_oauth2_client_registration_client_authorization_grant_type: str

    # OAuth2 Provider
    spring_security_oauth2_client_provider_oauthservice_issuer_uri: str
    spring_security_oauth2_client_provider_oauthservice_token_uri: str

    # Scopes (Matches the _0, _1 pattern from sync)
    spring_security_oauth2_client_registration_client_scope_0: str
    spring_security_oauth2_client_registration_client_scope_1: str

    model_config = SettingsConfigDict(
        case_sensitive=False,
        extra="ignore"
    )


# Create the instance
settings = Settings()

print(f"--- Verification ---")
print(f"Cloudinary Name: {settings.cloudinary_cloudname}")
print(f"Auth Service: {settings.authservice_base_uri}")