/**
 * ==========================================
 * ENUMS (Matches Java Enumerated Types)
 * ==========================================
 */

export enum VerificationStatus {
    NOT_SUBMITTED = 'NOT_SUBMITTED',
    PENDING = 'PENDING',
    VERIFIED = 'VERIFIED',
    REJECTED = 'REJECTED'
}

export enum BusType {
    AC_SEATER = 'AC_SEATER',
    AC_SLEEPER = 'AC_SLEEPER',
    AC_HYBRID = 'AC_HYBRID',
    NON_AC_SEATER = 'NON_AC_SEATER',
    NON_AC_SLEEPER = 'NON_AC_SLEEPER',
    NON_AC_HYBRID = 'NON_AC_HYBRID'
}

export enum StaffType {
    BUS_DRIVER = 'BUS_DRIVER',
    BUS_CONDUCTOR = 'BUS_CONDUCTOR'
}

export enum DutyType {
    ACTIVE = 'ACTIVE',
    RESERVED = 'RESERVED',
    OFF_DUTY = 'OFF_DUTY'
}

/**
 * ==========================================
 * CORE INTERFACES
 * ==========================================
 */

export interface ResponseDto<T> {
    body: T;
    status: number;
    message: string;
}

// --- Company & Operator Documents ---

export interface DocumentUploadRequest {
    userId: string;
    aadharNumber: string;
    panNumber: string;
}

export interface DocumentUploadResponse {
    busOperatorId: string;
    aadharNumber: string;
    panNumber: string;
    aadharUrl: string;
    panUrl: string;
    status: VerificationStatus;
    submittedAt: string; 
    verificationAt: string;
}

export interface DocumentResponse extends DocumentUploadResponse {}

export interface CompanyCreationRequest {
    companyName: string;
    ownerName: string;
    ownerId: string;
}

export interface CompanyCreationResponse {
    companyId: string;
    companyName: string;
    ownerName: string;
    ownerId: string;
    status: VerificationStatus;
}

// --- Bus & Template Management ---

export interface BusTemplate {
    templateId: string;
    templateName: string;
    busType: BusType | string;
    totalSeats: number;
    layoutData: string; 
}

export interface BusTemplateCreationRequest {
    templateName: string;
    layoutId: string;
    totalSeats: number;
    busType: BusType;
    ownerId: string;
}

export interface BusTemplateCreationResponse {
    templateId: string;
    templateName: string;
    layoutData: string;
    totalSeats: number;
}

export interface LayoutLookupResponse {
    layoutId: string;
    layoutName: string;
    description: string;
}

export interface BusCreationRequest {
    busName: string;
    busType: BusType;
    companyId: string;
    templateId: string;
    registrationNumber: string;
    insurancePolicyNumber: string;
    rcNumber: string;
}

export interface BusCreationResponse {
    busId: string;
    busName: string;
    busType: BusType;
    companyName: string;
    companyId: string;
}

export interface BusFleetResponse {
    busId: string;
    busName: string;
    registrationNumber: string;
    company: {
        companyName: string;
        companyId: string;
    };
    template: {
        templateName: string;
        busType: BusType | string;
        layoutData: string;
    };
}

// --- Staff Management ---

export interface BusStaffCreationRequest {
    name: string;
    companyId: string;
    age: number;
    phoneNumber: string;
    driverLicenseNumber?: string;
    staffType: StaffType;
}

export interface BusStaffCreationResponse {
    staffId: string;
    name: string;
    phoneNumber: string;
    driverLicenseNumber?: string;
    staffType: StaffType;
}

export interface BusStaffResponse {
    staffType: StaffType;
    staffId: string;
    dutyType: DutyType | null;
    staffName: string;
    busId?: string;
}

export interface AddBusStaffRequest {
    staffId: string;
    busId: string;
    staffName: string;
    dutyType: DutyType;
}