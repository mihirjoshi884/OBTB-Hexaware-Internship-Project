import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
    BusStaffResponse,
    DutyType,
    StaffType
} from 'src/app/interfaces/bus-operator.models';

@Component({
    selector: 'app-staff-master-list-component',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './staff-master-list-component.html',
    styleUrl: './staff-master-list-component.css'
})
export class StaffMasterListComponent implements OnInit {
    @Input() staffList: BusStaffResponse[] = [];
    @Input() isLoading = false;
    @Input() totalStaffCount = 0;
    @Input() driverCount = 0;
    @Input() conductorCount = 0;

    searchQuery = '';
    filterType: 'all' | 'driver' | 'conductor' = 'all';
    selectedStaff: BusStaffResponse | null = null;
    showDetailsModal = false;

    StaffType = StaffType;
    DutyType = DutyType;

    ngOnInit(): void {
        // Component initialized with staff list input
    }

    getFilteredStaff(): BusStaffResponse[] {
        let filtered = this.staffList;

        // Filter by type
        if (this.filterType === 'driver') {
        filtered = filtered.filter(s => s.staffType === StaffType.BUS_DRIVER);
        } else if (this.filterType === 'conductor') {
        filtered = filtered.filter(s => s.staffType === StaffType.BUS_CONDUCTOR);
        }

        // Filter by search query
        if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(s =>
            s.staffName.toLowerCase().includes(query)
        );
        }

        return filtered;
    }

    getDriversOnly(): BusStaffResponse[] {
        return this.getFilteredStaff().filter(s => s.staffType === StaffType.BUS_DRIVER);
    }

    getConductorsOnly(): BusStaffResponse[] {
        return this.getFilteredStaff().filter(s => s.staffType === StaffType.BUS_CONDUCTOR);
    }

    getUnassignedStaff(type: StaffType): BusStaffResponse[] {
        return this.getFilteredStaff()
        .filter(s => s.staffType === type && !s.busId);
    }

    getAssignedStaff(type: StaffType): BusStaffResponse[] {
        return this.getFilteredStaff()
        .filter(s => s.staffType === type && s.busId);
    }

    openDetailsModal(staff: BusStaffResponse): void {
        this.selectedStaff = staff;
        this.showDetailsModal = true;
    }

    closeDetailsModal(): void {
        this.showDetailsModal = false;
        this.selectedStaff = null;
    }

    getDutyTypeColor(dutyType: DutyType | null): string {
        if (!dutyType) return 'bg-slate-700/30 border-slate-600 text-slate-300';
        switch (dutyType) {
        case DutyType.ACTIVE:
            return 'bg-green-900/30 border-green-500 text-green-400';
        case DutyType.RESERVED:
            return 'bg-blue-900/30 border-blue-500 text-blue-400';
        case DutyType.OFF_DUTY:
            return 'bg-amber-900/30 border-amber-500 text-amber-400';
        default:
            return 'bg-slate-700/30 border-slate-600 text-slate-300';
        }
    }

    getStatusColor(busId: string | null): string {
        return busId ? 'text-green-400' : 'text-amber-400';
    }

    getStatusText(busId: string | null): string {
        return busId ? 'ASSIGNED' : 'UNASSIGNED';
    }
}
