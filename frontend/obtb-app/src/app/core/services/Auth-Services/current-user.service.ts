import { Injectable, inject, signal } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';


/**
 * User information model
 * Holds all data related to current logged-in user
 */
export interface CurrentUser {
    username: string;
    email: string;
    givenName?: string;
    familyName?: string;
    preferredUsername?: string;
    fullName?: string;
    accessToken: string;
    idToken: string;
    expiresIn?: number;
    tokenExpiration?: Date;
    claims?: any;
    roles?: string[];
}


@Injectable({ providedIn: 'root' })
export class CurrentUserService {
    private readonly oauthService = inject(OAuthService);
    

    /**
     * Reactive signal holding current user information
     * Null when no user is logged in
     */
    currentUser = signal<CurrentUser | null>(null);

    /**
     * Signal for tracking user loading state
     */
    isLoading = signal(false);

    /**
     * Signal for tracking user availability
     */
    isUserAvailable = signal(false);

    constructor() {
        // Try to restore user info from session storage on service initialization
        this.restoreUserFromStorage();
        
    }

    /**
     * Initialize and store current user information from OAuth claims
     * Called after successful OAuth token exchange
     */
    initializeUser(claims: any): void {
        try {
        this.isLoading.set(true);

        const username = claims?.preferred_username || claims?.username || claims?.name || '';
        const accessToken = this.oauthService.getAccessToken() || '';
        const idToken = this.oauthService.getIdToken() || '';
        const expiresIn = this.oauthService.getAccessTokenExpiration();

        if (!username || !accessToken) {
            console.warn('⚠️ Missing username or access token');
            this.currentUser.set(null);
            this.isUserAvailable.set(false);
            return;
        }

        const user: CurrentUser = {
            username: username,
            email: claims?.email || '',
            givenName: claims?.given_name || '',
            familyName: claims?.family_name || '',
            preferredUsername: claims?.preferred_username || '',
            fullName: claims?.name || '',
            accessToken: accessToken,
            idToken: idToken,
            expiresIn: expiresIn,
            tokenExpiration: new Date(expiresIn * 1000),
            claims: claims,
            roles: claims?.roles || []
        };

        // Store in signal
        this.currentUser.set(user);
        this.isUserAvailable.set(true);

        // Store in sessionStorage for persistence
        this.saveUserToStorage(user);

        console.log('👤 Current user initialized:', {
            username: user.username,
            email: user.email,
            accessTokenLength: user.accessToken.length,
            expiresAt: user.tokenExpiration
        });

        } catch (error) {
        console.error('❌ Error initializing user:', error);
        this.currentUser.set(null);
        this.isUserAvailable.set(false);
        } finally {
        this.isLoading.set(false);
        }
    }

    /**
     * Get current user (reactive signal)
     */
    getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Get current username
     */
    getUsername(): string {
        return this.currentUser()?.username || '';
    }

    /**
     * Get first letter of username for avatar
     */
    getAvatarLetter(): string {
        const username = this.getUsername();
        return username ? username.charAt(0).toUpperCase() : 'U';
    }

    /**
     * Get current user's email
     */
    getEmail(): string {
        return this.currentUser()?.email || '';
    }

    /**
     * Get current user's access token
     */
    getAccessToken(): string {
        return this.currentUser()?.accessToken || '';
    }

    /**
     * Get current user's ID token
     */
    getIdToken(): string {
        return this.currentUser()?.idToken || '';
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated(): boolean {
        return this.isUserAvailable() && !!this.currentUser()?.username;
    }

    /**
     * Check if token is expired or about to expire
     * @param bufferSeconds - Additional buffer time in seconds (default: 60)
     */
    isTokenExpired(bufferSeconds: number = 60): boolean {
        const user = this.currentUser();
        if (!user?.tokenExpiration) return true;

        const now = new Date();
        const buffer = bufferSeconds * 1000;
        return now.getTime() + buffer >= user.tokenExpiration.getTime();
    }

    /**
     * Clear current user information
     * Called on logout
     */
    clearUser(): void {
        this.currentUser.set(null);
        this.isUserAvailable.set(false);
        this.removeUserFromStorage();
        console.log('🚪 Current user cleared');
    }

    /**
     * Save user to sessionStorage for persistence across page reloads
     */
    private saveUserToStorage(user: CurrentUser): void {
        try {
        const userData = {
            username: user.username,
            email: user.email,
            givenName: user.givenName,
            familyName: user.familyName,
            preferredUsername: user.preferredUsername,
            fullName: user.fullName,
            roles: user.roles
        };
        sessionStorage.setItem('currentUser', JSON.stringify(userData));
        console.log('💾 User data saved to sessionStorage');
        } catch (error) {
        console.error('❌ Error saving user to storage:', error);
        }
    }

    /**
     * Restore user from sessionStorage
     */
    private restoreUserFromStorage(): void {
        try {
        const storedUser = sessionStorage.getItem('currentUser');
        if (storedUser) {
            const userData = JSON.parse(storedUser);
            console.log('📂 User data restored from sessionStorage:', userData.username);
            // Note: We don't restore tokens from storage for security reasons
            // User needs to complete OAuth flow to get new tokens
        }
        } catch (error) {
        console.warn('⚠️ Error restoring user from storage:', error);
        }
    }

    /**
     * Remove user from sessionStorage
     */
    private removeUserFromStorage(): void {
        try {
        sessionStorage.removeItem('currentUser');
        console.log('🗑️ User data removed from sessionStorage');
        } catch (error) {
        console.error('❌ Error removing user from storage:', error);
        }
    }

    /**
     * Get user summary for logging/debugging
     */
    getUserSummary(): any {
        const user = this.currentUser();
        if (!user) return null;

        return {
        username: user.username,
        email: user.email,
        fullName: user.fullName || user.givenName || '',
        authenticated: this.isAuthenticated(),
        tokenExpired: this.isTokenExpired(),
        roles: user.roles
        };
    }

    /**
     * Log current user info to console
     */
    logUserInfo(): void {
        const summary = this.getUserSummary();
        if (summary) {
        console.log('👤 Current User Info:', summary);
        } else {
        console.log('⚠️ No user currently logged in');
        }
    }


}
