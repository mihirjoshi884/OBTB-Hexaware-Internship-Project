import { AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';


export const authConfig: AuthConfig = {
  issuer: environment.baseUrls['authservice.issuer'],
  clientId: 'obtb-client-001',
  responseType: 'code',
  redirectUri: globalThis.location.origin + '/login/callback',
  postLogoutRedirectUri: globalThis.location.origin + '/login',
  silentRefreshRedirectUri: globalThis.location.origin + '/silent-refresh.html',
  scope: 'openid profile offline_access',
  
  // Refresh token configuration
  useSilentRefresh: false,
  useIdTokenHintForSilentRefresh: true,
  
  // Security settings: Use environment to toggle
  requireHttps: environment.production, 
  showDebugInformation: !environment.production,
  
  disablePKCE: false,
  timeoutFactor: 0.75,
  sessionChecksEnabled: false,
  strictDiscoveryDocumentValidation: false,
  skipIssuerCheck: false
};



