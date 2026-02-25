import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusService } from 'src/app/core/services/bus-service';
import {
    BusStaffCreationRequest,
    BusStaffCreationResponse,
    ResponseDto,
    StaffType
} from 'src/app/interfaces/bus-operator.models';

@Component({
    selector: 'app-staff-creation-component',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './staff-creation-component.html',
    styleUrl: './staff-creation-component.css'
})
export class StaffCreationComponent implements OnInit {
    @Input() userId!: string;
    @Input() companyId?: string;
    @Output() staffCreated = new EventEmitter<BusStaffCreationResponse>();

    StaffType = StaffType;
    staffType: StaffType = StaffType.BUS_DRIVER;
    fullName = '';
    age: number | null = null;
    phoneNumber = '';
    driverLicenseNumber = '';
    licenseFile: File | null = null;
    licenseFileName = '';

    isSubmitting = false;
    submitSuccess: string | null = null;
    submitError: string | null = null;
    validationErrors: { [key: string]: string | null } = {};
    Object = Object;

    constructor(
        private readonly busService: BusService,
        private readonly cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        // Reset form on init
        this.resetForm();
    }

    selectStaffType(type: StaffType): void {
        this.staffType = type;
        if (type === StaffType.BUS_CONDUCTOR) {
        this.driverLicenseNumber = '';
        this.licenseFile = null;
        this.licenseFileName = '';
        }
    }

    onLicenseFileSelected(event: any): void {
        const file = event.target.files?.[0];
        if (file) {
        if (file.type === 'application/pdf') {
            this.licenseFile = file;
            this.licenseFileName = file.name;
            this.validationErrors['license'] = null;
        } else {
            this.validationErrors['license'] = 'Only PDF files are allowed';
            this.licenseFile = null;
            this.licenseFileName = '';
        }
        }
    }

    clearLicenseFile(): void {
        this.licenseFile = null;
        this.licenseFileName = '';
    }

    validate(): boolean {
        this.validationErrors = {};

        if (!this.fullName.trim()) {
        this.validationErrors['name'] = 'Name cannot be empty';
        }

        if (!this.age || this.age < 18 || this.age > 70) {
        this.validationErrors['age'] = 'Age must be between 18 and 70';
        }

        if (!this.phoneNumber.trim() || !/^\d{10}$/.test(this.phoneNumber)) {
        this.validationErrors['phone'] = 'Phone number must be 10 digits';
        }

        if (this.staffType === StaffType.BUS_DRIVER) {
        if (!this.driverLicenseNumber.trim()) {
            this.validationErrors['licenseNumber'] = 'License number is required';
        }
        if (!this.licenseFile) {
            this.validationErrors['license'] = 'Driver license PDF is required';
        }
        }

        return Object.keys(this.validationErrors).length === 0;
    }

    createStaff(): void {
        if (!this.companyId) {
            this.submitError = "Company selection is required.";
            return;
        }

        if (!this.validate()) return;
        this.isSubmitting = true;
        
        const formData = new FormData();
        const staffRequest: BusStaffCreationRequest = {
            name: this.fullName.trim(),
            companyId: this.companyId, // Use the variable directly
            age: this.age as number,
            phoneNumber: this.phoneNumber.trim(),
            staffType: this.staffType,
            driverLicenseNumber: this.staffType === StaffType.BUS_DRIVER ? this.driverLicenseNumber.trim() : undefined
        };

        // Append as 'data' to match @RequestPart("data")
        formData.append('data', new Blob([JSON.stringify(staffRequest)], {
            type: 'application/json'
        }));

        if (this.staffType === StaffType.BUS_DRIVER && this.licenseFile) {
            formData.append('driverLicense', this.licenseFile);
        }
        this.busService.createBusStaff(formData).subscribe({
            next: (response: ResponseDto<BusStaffCreationResponse>) => {
            this.isSubmitting = false;
            this.submitSuccess = response?.message || 'Staff member created successfully!';
            this.staffCreated.emit(response.body);
            this.resetForm();
            this.cdr.detectChanges();
            },
            error: (error: HttpErrorResponse) => {
            this.isSubmitting = false;
            this.submitError = error?.error?.message || 'Failed to create staff member';
            console.error(error);
            this.cdr.detectChanges();
            }
        });
    }

    resetForm(): void {
        this.staffType = StaffType.BUS_DRIVER;
        this.fullName = '';
        this.age = null;
        this.phoneNumber = '';
        this.driverLicenseNumber = '';
        this.licenseFile = null;
        this.licenseFileName = '';
        this.validationErrors = {};
        this.submitSuccess = null;
        this.submitError = null;
    }
}
