// Matches ResponseDto.java
export interface ResponseDto<T> {
    body: T;
    status: number;
    message: string;
}

// Matches DocumentUploadResponse.java
export interface DocumentUploadResponse {
    busOperatorId: string ;
    aadharNumber: string;
    panNumber: string;
    aadharUrl: string;
    panUrl: string;
    status: 'NOT_SUBMITTED' | 'PENDING' | 'VERIFIED' | 'REJECTED';
    submittedAt: string; // ISO LocalDateTime string
    verificationAt: string;
}

// Matches DocumentUploadRequest.java (used for sending data)
export interface DocumentUploadRequest {
    userId: string;
    aadharNumber: string;
    panNumber: string;
}

export interface DocumentResponse {
    busOperatorId: string ;
    aadharNumber: string;
    panNumber: string;
    aadharUrl: string;
    panUrl: string;
    status: 'PENDING' | 'VERIFIED' | 'REJECTED' | 'NOT_SUBMITTED';
    submittedAt: Date | string;
    verification: Date | string;
}

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
    status: 'PENDING' | 'VERIFIED' | 'REJECTED' | 'NOT_SUBMITTED';
}

export interface BusTemplateCreationRequest {

    templateName: string;
    layoutId: string;
    totalSeats: number;
    busType: 'AC_SEATER' | 'AC_SLEEPER' | 'AC_HYBRID'| 'NON_AC_SEATER' | 'NON_AC_SLEEPER' | 'NON_AC_HYBRID';
    ownerId: string;
}

export interface BusTemplateCreationResponse {

    templateId: string;
    templateName: string;
    layoutData: string;
    totalSeats: number;
}

export interface BusCreationRequest {
    busName: string;
    busType: 'AC_SEATER' | 'AC_SLEEPER' | 'AC_HYBRID'| 'NON_AC_SEATER' | 'NON_AC_SLEEPER' | 'NON_AC_HYBRID';
    companyId: string;
    templateId: string;
    registrationNumber: string;
    insurancePolicyNumber: string;
    rcNumber: string;
}

export interface BusCreationResponse{
    busId: string;
    busName: string;
    busType: 'AC_SEATER' | 'AC_SLEEPER' | 'AC_HYBRID'| 'NON_AC_SEATER' | 'NON_AC_SLEEPER' | 'NON_AC_HYBRID';
    companyName: string;
    companyId: string;

}
export interface LayoutLookupResponse {
    layoutId: string;
    layoutName: string;
    description: string;
}

export interface BusTemplate {
    templateId: string;
    templateName: string;
    busType: string;
    totalSeats: number;
    layoutData: string; 
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
        busType: string;
    };
}
