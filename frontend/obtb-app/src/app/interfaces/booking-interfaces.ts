export enum SeatType{
    SEATER = 'SEATER',
    SLEEPER = 'SLEEPER',
    WALKWAY = 'WALKWAY'
}

export enum SeatStatus{
    AVAILABLE = 'AVAILABLE',
    BOOKED = 'BOOKED',
    BLOCKED= 'BLOCKED',
    NONE = 'NONE'
}

export interface SeatMappingDto {
    instanceId: string;
    seats: EnrichedSeatDto [];
}

export interface EnrichedSeatDto{
    seatNumber: string;
    tripSeatId: string;
    seatType: SeatType;
    seatStatus: SeatStatus;
    isWindow: boolean;
    x_coordinate: number;
    y_coordinate: number;
    deck: number;
} 