// src/app/interfaces/trip-model.ts

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

export interface tripCreationRequest {
    routeId: string;
    busId: string;
    companyId: string;
    tripType: TripType;
    firstDepartureTime: Date; // Changed from date to Date
    firstArrivalTime: Date;   // Changed from date to Date
    scheduledDay: any;
    dailyDepartureTime: any;
    baseFare: number;
}

export interface tripDetail {
    tripId: string;
    busId: string;
    arrivalTime: Date;   // Changed from date to Date
    departureTime: Date; // Changed from date to Date
    baseFare: number;
    busDetails: BusFleetResponse;
}