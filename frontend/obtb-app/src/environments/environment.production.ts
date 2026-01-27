export const environment = {
  production: true,
  environmentName: 'prod',
  featureFlag: true,
  // ✅ Hardcoded URLs for PRODUCTION profile (update with your production URLs)
  baseUrls: {
    'userservice.base-uri': 'https://api.yourdomain.com/user-service',
    'authservice.base-uri': 'https://api.yourdomain.com/auth-service',
    'configserver.base-uri': 'https://api.yourdomain.com/config-service',
    'notificationservice.base-uri': 'https://api.yourdomain.com/notification-service',
    'angular.base-uri': 'https://yourdomain.com',
    'txnBaseUri': 'https://api.yourdomain.com/transaction-service'
  }
};
