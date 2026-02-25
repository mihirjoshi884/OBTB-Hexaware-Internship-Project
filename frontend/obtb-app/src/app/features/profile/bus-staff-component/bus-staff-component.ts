import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, NO_ERRORS_SCHEMA, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusService } from 'src/app/core/services/bus-service';
import {
    BusStaffResponse,
    StaffType
} from 'src/app/interfaces/bus-operator.models';
import { StaffCreationComponent } from './staff-creation-component';
import { StaffMasterListComponent } from './staff-master-list-component';

@Component({
    selector: 'app-bus-staff-component',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        StaffCreationComponent,
        StaffMasterListComponent
    ],
    schemas: [NO_ERRORS_SCHEMA],
    templateUrl: './bus-staff-component.html',
    styleUrl: './bus-staff-component.css'
})
export class BusStaffComponent implements OnInit {
    @Input() userId!: string;
    @Input() companyId?: string;

    constructor(
        private readonly busService: BusService,
        private readonly cdr: ChangeDetectorRef
    ) {}

    staffList: BusStaffResponse[] = [];
    isLoading = false;
    activeSection: 'create' | 'list' = 'list';
    successMessage: string | null = null;

    ngOnInit(): void {
        if (this.userId) {
        this.fetchCompanyStaff();
        }
    }

    fetchCompanyStaff(): void {
        if (!this.companyId) {
            console.warn("Cannot fetch staff: companyId is undefined");
            this.isLoading = false; // Ensure loader stops
            return;
        }

        this.isLoading = true;
        
        this.busService.fetchCompanyStaff(this.companyId).subscribe({
            next: response => {
                this.staffList = response.body ? [...response.body] : [];
                this.isLoading = false;
                this.cdr.detectChanges();
            },
            error: err => {
                console.error("Error fetching staff:", err);
                this.staffList = [];
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    onStaffCreated(staff: any): void {
        this.successMessage = `✓ ${staff.name} successfully created!`;
        setTimeout(() => {
        this.successMessage = null;
        this.activeSection = 'list';
        this.fetchCompanyStaff();
        this.cdr.detectChanges();
        }, 2000);
    }

    switchSection(section: 'create' | 'list'): void {
        this.activeSection = section;
        this.successMessage = null;
    }

    getDriverCount(): number {
        return this.staffList.filter(s => s.staffType === StaffType.BUS_DRIVER).length;
    }

    getConductorCount(): number {
        return this.staffList.filter(s => s.staffType === StaffType.BUS_CONDUCTOR).length;
    }

    getTotalStaffCount(): number {
        return this.staffList.length;
    }
}
