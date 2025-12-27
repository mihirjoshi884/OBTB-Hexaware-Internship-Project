import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { DataStore } from '../../core/data-store/data-store';

@Component({
  selector: 'app-loader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './loader.html'
})
export class LoaderComponent {
  private readonly dataStore = inject(DataStore);

  isLoading = this.dataStore.isLoading;
}
