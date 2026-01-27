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
    status: 'PENDING' | 'VERIFIED' | 'REJECTED';
    submittedAt: Date | string;
    verification: Date | string;
}