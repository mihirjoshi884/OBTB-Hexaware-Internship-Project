import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { UserProfile } from '../../interfaces/user-profile';

interface ResponseDto<T> {
    body: T;         // Matches your Java 'body' field
    status: number;
    message: string;
}

interface FundsSummaryDto {
    username: string;
    previousBalance: number;
    amountAdded: number;
    newBalance: number;
    transactionDate: string;
}

@Injectable({ providedIn: 'root' })
export class UserProfileService {

    private readonly http = inject(HttpClient);
    private readonly apibaseUrl = environment.baseUrls['userservice.base-uri'];
    private readonly txnBaseUrl = environment.baseUrls['txnBaseUri'];
    private readonly profilePicSubject = new BehaviorSubject<string | null>(null);
    profilePic$ = this.profilePicSubject.asObservable();

    getUserProfile(username: string): Observable<UserProfile>{
        const url = `${this.apibaseUrl}/user-api/v1/dashboard/${username}`;
        return this.http.get<ResponseDto<UserProfile>>(url)
            .pipe(
                map(response => response.body),
                // 3. Update the stream whenever data is fetched
                tap(user => this.profilePicSubject.next(user.profilePictureUrl))
            );
    }

    updateUserProfile(username: string, formData: FormData): Observable<UserProfile> {
        const url = `${this.apibaseUrl}/user-api/v1/update-user/${username}`;
        return this.http.put<ResponseDto<UserProfile>>(url, formData)
            .pipe(
                map(response => response.body),
                // 4. Update the stream whenever data is updated
                tap(user => this.profilePicSubject.next(user.profilePictureUrl))
            );
    }

    /**
     * Add funds to user's wallet
     * @param username - Username to add funds to
     * @param amount - Amount to add (in rupees)
     * @returns Observable with funds summary after addition
     */
    addFunds(username: string, amount: number): Observable<FundsSummaryDto> {
        const url = `${this.apibaseUrl}/user-api/v1/add-funds/${username}`;
        return this.http.put<ResponseDto<FundsSummaryDto>>(url, amount)
            .pipe(
                map(response => response.body),
                tap(fundsSummary => {
                    console.log('✅ Funds added successfully:', fundsSummary);
                })
            );
    }

    withDrawFunds(username: string, amount: number): Observable<FundsSummaryDto>{
        // http://localhost:9090/user/user-api/v1/withdraw-funds/{username}
        const url = `${this.apibaseUrl}/user-api/v1/withdraw-funds/${username}?amount=${amount}`;
        return this.http.delete<ResponseDto<FundsSummaryDto>>(url)
            .pipe(
                map(response => response.body),
                tap(fundsSummary =>{
                    console.log('Funds withdrawn successfully:', fundsSummary);
                }) 
            );
    }
    getTransactionHistory(userId: string, page: number, size: number): Observable<any> {
    // Build parameters: /history/uuid?page=0&size=10
        const params = new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString());

        return this.http.get<any>(`${this.txnBaseUrl}/txn-api/v1/history/${userId}`, { params });
    }
}