export const environment = {
  production: true,
  environmentName: 'staging',
  featureFlag: false,
  // ✅ Hardcoded URLs for STAGING profile (using Docker service names)
    baseUrls: {
        'userservice.base-uri': 'http://localhost:9090/user',
        'authservice.base-uri': 'http://localhost:9090/auth',
        'authservice.issuer': 'http://localhost:8081',
        'txnBaseUri': 'http://localhost:9090/txn',
        'busService.base-uri':'http://localhost:9090/bus',
        'bookingService.base-uri':'http://localhost:9090/booking'
    },
};
