import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-bus-company-component',
  imports: [FormsModule],
  templateUrl: './bus-company-component.html'
})
export class BusCompanyComponent {

  currentStep = 1;
  companyName = '';
  ownerName = '';

  registerCompany(){
    if(!this.companyName.trim() || !this.ownerName.trim()){
      console.log("can't be empty");
      return;
    }else{
      this.currentStep ++;
    }
  }
}
