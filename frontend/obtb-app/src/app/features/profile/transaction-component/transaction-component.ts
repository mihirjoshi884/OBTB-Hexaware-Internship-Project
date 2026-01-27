import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';


@Component({
  selector: 'app-transaction-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transaction-component.html',
  styleUrl: './transaction-component.css',
})
export class TransactionComponent {
  @Input() transaction: any;
  get isCredit(): boolean {
    return this.transaction.type === 'CREDIT';
  }

}
