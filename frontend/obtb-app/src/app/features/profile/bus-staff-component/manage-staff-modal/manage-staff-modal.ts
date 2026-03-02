import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
    AddBusStaffRequest,
    BusStaffResponse,
    DutyType,
    StaffType
} from 'src/app/interfaces/bus-operator.models';

@Component({
    selector: 'app-manage-staff-modal',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './manage-staff-modal.html'
})
export class ManageStaffModalComponent implements OnInit {
    @Input() busId!: string;
    @Input() busName!: string;
    @Input() companyStaff: BusStaffResponse[] = []; 
    @Input() currentStaff: AddBusStaffRequest[] = []; 
    @Input() isVisible = false;
    @Output() closeModal = new EventEmitter<void>();
    @Output() save = new EventEmitter<AddBusStaffRequest[]>();

    StaffType = StaffType;
    DutyType = DutyType;

    drivers: BusStaffResponse[] = [];
    conductors: BusStaffResponse[] = [];

    selectedDrivers: AddBusStaffRequest[] = [];
    selectedConductors: AddBusStaffRequest[] = [];
    removedStaffIds: string[] = [];

    activeSelectionType: string | null = null; 
    driverDropdownOpen = false;
    conductorDropdownOpen = false;

    isSaving = false;
    saveError: string | null = null;

    ngOnInit(): void {
        this.initializeStaff();
    }

    ngOnChanges(changes: SimpleChanges): void {
        // Re-initialize if currentStaff or companyStaff changes
        if (changes['currentStaff'] || changes['companyStaff']) {
            this.initializeStaff();
        }
    }

    initializeStaff(): void {
        // 1. Separate the company master list into drivers and conductors
        this.drivers = this.companyStaff.filter(s => s.staffType === StaffType.BUS_DRIVER);
        this.conductors = this.companyStaff.filter(s => s.staffType === StaffType.BUS_CONDUCTOR);

        // 2. Clear current selections
        const newSelectedDrivers: AddBusStaffRequest[] = [];
        const newSelectedConductors: AddBusStaffRequest[] = [];

        // 3. Process the currentStaff (those already assigned to this bus)
        if (this.currentStaff && this.currentStaff.length > 0) {
            this.currentStaff.forEach(assigned => {
                const assignedId = String(assigned.staffId).trim();

                // Look for this person in the master driver list
                const driverMatch = this.drivers.find(d => String(d.staffId).trim() === assignedId);
                if (driverMatch) {
                    newSelectedDrivers.push({ ...assigned, staffName: driverMatch.staffName });
                } 
                
                // Look for this person in the master conductor list
                const conductorMatch = this.conductors.find(c => String(c.staffId).trim() === assignedId);
                if (conductorMatch) {
                    newSelectedConductors.push({ ...assigned, staffName: conductorMatch.staffName });
                }
            });
        }

        // 4. Update the component properties with NEW array references
        // This is the CRITICAL part that makes the (0/2) change to (1/2) or (2/2)
        this.selectedDrivers = [...newSelectedDrivers];
        this.selectedConductors = [...newSelectedConductors];
    }

    getAvailableDrivers(): BusStaffResponse[] {
        return this.drivers.filter(d => 
            !this.selectedDrivers.some(s => s.staffId === d.staffId) &&
            (!d.busId || d.busId === "" || d.busId === this.busId)
        );
    }

    getAvailableConductors(): BusStaffResponse[] {
        return this.conductors.filter(c => 
            !this.selectedConductors.some(s => s.staffId === c.staffId) &&
            (!c.busId || c.busId === "" || c.busId === this.busId)
        );
    }

    // Slot Getters
    getActiveDriver() { return this.selectedDrivers.find(s => s.dutyType === DutyType.ACTIVE); }
    getReservedDriver() { return this.selectedDrivers.find(s => s.dutyType === DutyType.RESERVED); }
    getActiveConductor() { return this.selectedConductors.find(s => s.dutyType === DutyType.ACTIVE); }
    getReservedConductor() { return this.selectedConductors.find(s => s.dutyType === DutyType.RESERVED); }

    assignDriver(staff: BusStaffResponse, dutyType: DutyType): void {
        if (this.selectedDrivers.some(s => s.staffId === staff.staffId)) return;

        // Filter creates a new array reference
        const remaining = this.selectedDrivers.filter(s => s.dutyType !== dutyType);

        // Spread into a NEW array to trigger change detection
        this.selectedDrivers = [...remaining, {
            busId: this.busId,
            staffId: staff.staffId,
            staffName: staff.staffName,
            dutyType: dutyType
        }];
        
        this.activeSelectionType = null;
        this.driverDropdownOpen = false;
    }

    assignConductor(staff: BusStaffResponse, dutyType: DutyType): void {
        if (this.selectedConductors.some(s => s.staffId === staff.staffId)) return;

        const remaining = this.selectedConductors.filter(s => s.dutyType !== dutyType);

        this.selectedConductors = [...remaining, {
            busId: this.busId,
            staffId: staff.staffId,
            staffName: staff.staffName,
            dutyType: dutyType
        }];

        this.activeSelectionType = null;
        this.conductorDropdownOpen = false;
    }



    removeConductor(staffId: string): void {
        // 1. Remove from the local UI array
        this.selectedConductors = this.selectedConductors.filter(s => s.staffId !== staffId);
        
        // 2. IMPORTANT: Add to removal list so backend sets busId = null
        if (!this.removedStaffIds.includes(staffId)) {
            this.removedStaffIds.push(staffId);
        }
    }

    removeDriver(staffId: string): void {
        // 1. Remove from the local UI array
        this.selectedDrivers = this.selectedDrivers.filter(s => s.staffId !== staffId);
        
        // 2. Add to removal list so backend sets busId = null
        if (!this.removedStaffIds.includes(staffId)) {
            this.removedStaffIds.push(staffId);
        }
    }

    saveAssignments(): void {
        // 1. Map all currently selected staff (Drivers AND Conductors)
        const updates = [
            ...this.selectedDrivers,
            ...this.selectedConductors
        ].map(staff => ({
            staffId: staff.staffId,
            busId: this.busId,
            dutyType: staff.dutyType,
            staffName: staff.staffName
        }));

        // 2. Map removals (set busId and dutyType to null)
        const removals = this.removedStaffIds.map(id => ({
            staffId: id,
            busId: null,
            dutyType: null,
            staffName: '' 
        }));

        const finalPayload = [...updates, ...removals];

        // 3. Prevent sending an empty array to the backend
        if (finalPayload.length === 0) {
            console.warn('No changes to save.');
            this.closeModalEmit();
            return;
        }

        // 4. Emit the populated list
        this.save.emit(finalPayload as AddBusStaffRequest[]);
    }

    closeModalEmit(): void {
        this.closeModal.emit();
    }
}