import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DataStore {

  private readonly userIdSignal = signal<string>('');
  private readonly isLoadingSignal = signal<boolean>(false);
  
  setUserId(id: string) {
    this.userIdSignal.set(id);
  }

  getUserId(): string {
    return this.userIdSignal();
  }

  userId = this.userIdSignal.asReadonly();

  setLoading(isLoading: boolean) {
    this.isLoadingSignal.set(isLoading);
  }

  getLoading(): boolean {
    return this.isLoadingSignal();
  }

  isLoading = this.isLoadingSignal.asReadonly();
}
