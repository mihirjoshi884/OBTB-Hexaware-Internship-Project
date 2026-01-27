import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth-service';
import { UserProfileService } from '../../core/services/UserProfileService.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterModule],
  templateUrl: './navbar.html',
})
export class Navbar implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly userProfileService = inject(UserProfileService);
  
  // Track dropdown open/close state
  isDropdownOpen = signal(false);
  profilePicUrl = signal<string | null>(null); 
  // Track logged-in state as a signal for reactivity
  loggedInState = signal(false);
  currentUser = signal('');

  ngOnInit(): void {
    // Initialize logged-in state
    this.updateLoginState();
    
    // Listen to route changes to update state
    this.router.events.subscribe(() => {
      this.updateLoginState();
    });

    this.userProfileService.profilePic$.subscribe(Url => {
        if (Url) {
          this.profilePicUrl.set(Url); // Update your existing signal
        }
      });
  }

  private updateLoginState(): void {
    const isLogged = this.authService.isLoggedIn();
    this.loggedInState.set(isLogged);
    if (isLogged) {
      this.currentUser.set(this.authService.getUsername());
    } else {
      this.currentUser.set('');
    }
  }

  isLoggedIn() {
    return this.loggedInState();
  }

  currentUsername() {
    return this.currentUser();
  }

  /**
   * Get user initials for avatar display
   */
  getUserInitials(): string {
    const username = this.currentUsername();
    if (!username) return 'U';
    
    // If username has spaces (e.g., "John Doe"), use first letters
    const parts = username.split(' ');
    if (parts.length > 1) {
      return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    
    // Otherwise, use first two characters
    return username.substring(0, 2).toUpperCase();
  }

  /**
   * Get user display name (capitalize first letter of each word)
   */
  getUserDisplayName(): string {
    const username = this.currentUsername();
    if (!username) return '';
    
    return username
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }

  async logout(): Promise<void> {
    await this.authService.logout();
    this.updateLoginState();
  }

  toggleDropdown() {
    this.isDropdownOpen.update(state => !state);
  }

  closeDropdown() {
    this.isDropdownOpen.set(false);
  }

  navigateToProfile() {
    this.router.navigate(['/profile']);
    this.closeDropdown();
  }

  navigateToChangePassword() {
    this.router.navigate(['/change-password']);
    this.closeDropdown();
  }

  handleLogout() {
    this.closeDropdown();
    this.logout();
  }

  scrollToFooter() {
    const footer = document.getElementById('footer-section');
    if (footer) {
      footer.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  scrollToSearchSection() {
    // First, navigate to home if not already there
    const currentRoute = this.router.url;
    if (currentRoute === '/home' || currentRoute === '/' || currentRoute === '/search-bus') {
      // Already on home page, just scroll
      const searchSection = document.querySelector('[data-section="search"]');
      if (searchSection) {
        searchSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    } else {
      // Navigate to home first
      this.router.navigate(['/search-bus']).then(() => {
        // After navigation, wait a moment for the DOM to render
        setTimeout(() => {
          const searchSection = document.querySelector('[data-section="search"]');
          if (searchSection) {
            searchSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }, 300);
      });
    }
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}
