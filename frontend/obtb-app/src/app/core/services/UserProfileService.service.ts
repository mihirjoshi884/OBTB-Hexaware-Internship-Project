import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { UserProfile } from '../../interfaces/user-profile';

interface ResponseDto<T> {
    body: T;
    status: number;
    message: string;
}

interface FundsSummaryDto {
    username: string;
    fundsAmount: number;
}

@Injectable({ providedIn: 'root' })
export class UserProfileService {

    private readonly http = inject(HttpClient);
    private readonly apibaseUrl = environment.baseUrls['userservice.base-uri'];
    private readonly txnBaseUrl = environment.baseUrls['txnBaseUri'];
    private readonly profilePicSubject = new BehaviorSubject<string | null>(null);
    profilePic$ = this.profilePicSubject.asObservable();

    getUserProfile(username: string): Observable<UserProfile> {
        const url = `${this.apibaseUrl}/user-api/v1/dashboard/${username}`;
        return this.http.get<ResponseDto<UserProfile>>(url)
            .pipe(
                map(response => response.body),
                tap(user => this.profilePicSubject.next(user.profilePictureUrl))
            );
    }

    updateUserProfile(username: string, formData: FormData): Observable<UserProfile> {
        const url = `${this.apibaseUrl}/user-api/v1/update-user/${username}`;
        return this.http.put<ResponseDto<UserProfile>>(url, formData)
            .pipe(
                map(response => response.body),
                tap(user => this.profilePicSubject.next(user.profilePictureUrl))
            );
    }

    /**
     * Add funds to user's wallet.
     * ✅ FIX 3: Backend uses @RequestBody Double amount, so we send amount IN THE BODY,
     * not as a query parameter.
     */
    addFunds(username: string, amount: number): Observable<FundsSummaryDto> {
        const url = `${this.apibaseUrl}/user-api/v1/add-funds/${username}`;
        // Send amount directly as the JSON body — matches @RequestBody Double amount in Java
        return this.http.put<ResponseDto<FundsSummaryDto>>(url, amount)
            .pipe(
                map(response => response.body),
                tap(fundsSummary => {
                    console.log('✅ Funds added successfully:', fundsSummary);
                })
            );
    }

    /**
     * Withdraw funds from user's wallet.
     * Backend uses @RequestParam Double amount, so amount stays as a query param — already correct.
     */
    withDrawFunds(username: string, amount: number): Observable<FundsSummaryDto> {
        const url = `${this.apibaseUrl}/user-api/v1/withdraw-funds/${username}?amount=${amount}`;
        return this.http.delete<ResponseDto<FundsSummaryDto>>(url)
            .pipe(
                map(response => response.body),
                tap(fundsSummary => {
                    console.log('✅ Funds withdrawn successfully:', fundsSummary);
                })
            );
    }

    getTransactionHistory(userId: string, page: number, size: number): Observable<any> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<any>(`${this.txnBaseUrl}/txn-api/v1/history/${userId}`, { params });
    }
}