export const environment = {
    production: false,
    environmentName: 'dev',
    featureFlag: true,
    // ✅ Hardcoded URLs for DEV profile
    baseUrls: {
        'userservice.base-uri': 'http://localhost:9090/user',
        'authservice.base-uri': 'http://localhost:9090/auth',
        'authservice.issuer': 'http://localhost:8081',
        'txnBaseUri': 'http://localhost:9090/txn',
        'busService.base-uri':'http://localhost:9090/bus'
    }
};