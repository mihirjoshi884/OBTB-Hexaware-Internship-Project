import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-bus-creation-component',
  standalone: true,
  imports: [],
  templateUrl: './bus-creation-component.component.html'
})
export class BusCreationComponentComponent {

  @Output() back = new EventEmitter<void>();

}
