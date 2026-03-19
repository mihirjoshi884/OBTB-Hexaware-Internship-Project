import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { TripInstanceDto } from 'src/app/interfaces/trip-model';

@Component({
  selector: 'app-view-more-instance-information',
  imports: [CommonModule],
  templateUrl: './view-more-instance-information.html'
})
export class ViewMoreInstanceInformation {

  @Input() instance: TripInstanceDto | null = null;

}
