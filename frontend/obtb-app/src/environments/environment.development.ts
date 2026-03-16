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
        'busService.base-uri':'http://localhost:9090/bus',
        'bookingService.base-uri':'http://localhost:9090/booking'
    },
    mapApiKey: 'eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjA2OGMzMDRiYWIwODQxNWZiZmQ1M2FjN2RlYTMzMmZjIiwiaCI6Im11cm11cjY0In0='
};