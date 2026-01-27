export const environment = {
  production: false,
  environmentName: 'dev',
  featureFlag: true,
  // ✅ Hardcoded URLs for DEV profile
  baseUrls: {
    'userservice.base-uri': 'http://localhost:9090/user',
    'angular.base-uri': 'http://localhost:4200',
    'authservice.base-uri': 'http://localhost:9090/auth',
    'authservice.issuer': 'http://localhost:8081',
    'configserver.base-uri': 'http://localhost:8083',
    'notificationservice.base-uri': 'http://localhost:8084',
    'txnBaseUri': 'http://localhost:8085',
    'busService.base-uri':'http://localhost:8086'
  }
};
