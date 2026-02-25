import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-bus-layout-preview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bus-layout-preview.component.html',
  styles: [`
    .deck-grid { display: grid; gap: 0.75rem; }
    .seat-base { border-radius: 0.5rem; border: 2px solid; transition: all 0.2s; }
  `]
})
export class BusLayoutPreviewComponent implements OnChanges {
  @Input() layoutData: string | any[] = '';
  @Input() columns: number = 4;

  lowerDeck: any[] = [];
  upperDeck: any[] = [];
  gridCols: number = 4;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['layoutData'] || changes['columns']) {
      this.processLayout();
    }
  }

  private processLayout() {
    let seats: any[] = [];
    
    // Handle both string and already-parsed array
    if (typeof this.layoutData === 'string' && this.layoutData) {
      try { seats = JSON.parse(this.layoutData); } catch (e) { seats = []; }
    } else if (Array.isArray(this.layoutData)) {
      seats = this.layoutData;
    }

    // Split Decks
    this.lowerDeck = seats.filter(s => s.deck === 0 || s.deck === '0' || !s.deck);
    this.upperDeck = seats.filter(s => s.deck === 1 || s.deck === '1');

    // Calculate columns if not explicitly provided
    if (seats.length > 0) {
      this.gridCols = this.columns || (Math.max(...seats.map(s => s.x_coordinate)) + 1);
    }
  }
}