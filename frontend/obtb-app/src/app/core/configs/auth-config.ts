import { AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

export const authConfig: AuthConfig = {
  issuer: (environment.baseUrls as any)['authservice.issuer'],
  clientId: 'obtb-client-001',
  responseType: 'code',
  redirectUri: globalThis.location.origin + '/login/callback',
  postLogoutRedirectUri: globalThis.location.origin + '/login',
  silentRefreshRedirectUri: globalThis.location.origin + '/silent-refresh.html',
  scope: 'openid profile offline_access',
  
  // Refresh token configuration
  useSilentRefresh: false,
  useIdTokenHintForSilentRefresh: true,
  
  // FORCE TO FALSE FOR LOCAL HTTP DEVELOPMENT
  requireHttps: false, 
  showDebugInformation: true,
  
  disablePKCE: false,
  timeoutFactor: 0.75,
  sessionChecksEnabled: false,
  strictDiscoveryDocumentValidation: false,
  skipIssuerCheck: true // Helps avoid strict validation issues with cluster-internal domain naming
};