export const environment = {
  production: true,
  environmentName: 'staging',
  featureFlag: false,
  // ✅ Hardcoded URLs for STAGING profile (using Docker service names)
  baseUrls: {
    'userservice.base-uri': 'http://localhost:9090/user',
    'authservice.base-uri': 'http://localhost:9090/auth',
    'txnBaseUri': 'http://localhost:8085'
  }
};
