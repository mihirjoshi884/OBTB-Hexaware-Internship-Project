import { Component, Input } from '@angular/core';
import { TripTemplateDto } from 'src/app/interfaces/trip-model';

@Component({
  selector: 'app-view-more-template-info',
  imports: [],
  templateUrl: './view-more-template-info.html'
})
export class ViewMoreTemplateInfo {
  @Input() template: TripTemplateDto | null = null; 
}
