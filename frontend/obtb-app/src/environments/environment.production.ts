export const environment = {
  production: true,
  environmentName: 'prod',
  featureFlag: true,
  baseUrls: {
    // All microservice traffic routes through /api — the ngrok traffic policy
    // strips the /api prefix before forwarding to the API Gateway (port 9090)
    'userservice.base-uri':        'https://disgrace-shampoo-detonator.ngrok-free.dev/api/user',
    'authservice.base-uri':        'https://disgrace-shampoo-detonator.ngrok-free.dev/api/auth',
    'txnBaseUri':                  'https://disgrace-shampoo-detonator.ngrok-free.dev/api/txn',
    'busService.base-uri':         'https://disgrace-shampoo-detonator.ngrok-free.dev/api/bus',
    'bookingService.base-uri':     'https://disgrace-shampoo-detonator.ngrok-free.dev/api/booking',

    // Auth service is exposed directly at /auth (not through the API gateway)
    // This is the OIDC issuer — must match what Keycloak/auth server uses in its tokens
    'authservice.issuer':          'https://disgrace-shampoo-detonator.ngrok-free.dev/auth',

    // Angular app's own origin (used for OIDC redirect URIs)
    'angular.base-uri':            'https://disgrace-shampoo-detonator.ngrok-free.dev',
  },
  mapApiKey: 'eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjA2OGMzMDRiYWIwODQxNWZiZmQ1M2FjN2RlYTMzMmZjIiwiaCI6Im11cm11cjY0In0='
};