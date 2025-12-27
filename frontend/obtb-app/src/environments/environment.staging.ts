export const environment = {
  production: true,
  environmentName: 'staging',
  featureFlag: false,
  // ✅ Hardcoded URLs for STAGING profile (using Docker service names)
  baseUrls: {
    'userservice.base-uri': 'http://user-service:8082',
    'authservice.base-uri': 'http://auth-service:8081',
    'configserver.base-uri': 'http://config-service:8083',
    'notificationservice.base-uri': 'http://notification-service:8084',
    'angular.base-uri': 'http://localhost:4200'
  }
};
