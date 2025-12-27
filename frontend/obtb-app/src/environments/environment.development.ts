export const environment = {
    production: false,
    environmentName: 'dev',
    featureFlag: true,
    // ✅ Hardcoded URLs for DEV profile
    baseUrls: {
        'userservice.base-uri': 'http://localhost:8082',
        'angular.base-uri': 'http://localhost:4200',
        'authservice.base-uri': 'http://localhost:8081',
        'configserver.base-uri': 'http://localhost:8083',
        'notificationservice.base-uri': 'http://localhost:8084'
    }
};