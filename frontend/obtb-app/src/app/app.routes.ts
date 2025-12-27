import { Routes } from '@angular/router';
import { ActivateAccount } from './features/activate-account/activate-account';
import { ChangePasswordComponent } from './features/change-password/change-password';
import { Home } from './features/home/home';
import { LoginComponent } from './features/login/login';
import { LoginCallbackComponent } from './features/login/login-callback/login-callback-component/login-callback-component';
import { ProfileComponent } from './features/profile/profile';
import { VerifyAccount } from './features/verify-account/verify-account';

export const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: 'home', component: Home },
    { path: 'search-bus', component: Home },  // Redirect to home with search focus
    { path: 'profile', component: ProfileComponent },
    { path: 'change-password', component: ChangePasswordComponent },
    { path: 'activate-account', component: ActivateAccount },
    { path: 'verify-account', component: VerifyAccount },
    { path: 'login', component: LoginComponent },
    { path: 'login/callback', component: LoginCallbackComponent },
    {
        path: 'signup',
        loadComponent: () =>
        import('./features/signup/signup-wizard/signup-wizard')
            .then(m => m.SignupWizardComponent)
    }
];
