// src/app/interfaces/trip-model.ts

import { Time } from "@angular/common";

export enum BusType {
    AC_SEATER = 'AC_SEATER',
    AC_SLEEPER = 'AC_SLEEPER',
    AC_HYBRID = 'AC_HYBRID',
    NON_AC_SEATER = 'NON_AC_SEATER',
    NON_AC_SLEEPER = 'NON_AC_SLEEPER',
    NON_AC_HYBRID = 'NON_AC_HYBRID'
}
export enum VerificationStatus {
    NOT_SUBMITTED = 'NOT_SUBMITTED',
    PENDING = 'PENDING',
    VERIFIED = 'VERIFIED',
    REJECTED = 'REJECTED'
}

export enum TripType {
    REGULAR = 'REGULAR',
    ONE_TIME = 'ONE_TIME'
}

export interface BusFleetResponse {
    busId: string;
    busName: string;
    status: VerificationStatus;
    registrationNumber: string;
    company: {
        companyName: string;
        companyId: string;
    };
    template: {
        templateName: string;
        busType: BusType | string; // Now BusType is defined
        layoutData: string;
    };
}

export interface TripCreationRequest {
    routeId: string;
    busId: string;
    companyId: string;
    tripType: TripType;
    departureDate: string | null; // Changed to string
    departureTime: string | null; // Changed to string
    arrivalDate: string | null;   // Changed to string
    arrivalTime: string | null;   // Changed to string
    scheduledDay: any | null;
    regularDepartureTime: string | null; // Consistent with others
    baseFare: number;
}

export interface tripDetail {
    tripId: string;
    busId: string;
    arrivalDate: Date;
    arrivalTime: Time;
    departureDate: Date;   // Changed from date to Date
    departureTime: Time; // Changed from date to Date
    baseFare: number;
    busDetails: BusFleetResponse;
}