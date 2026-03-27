
export enum JourneyType {
    ONE_WAY= 'ONE_WAY',
    ROUND_TRIP = 'ROUND_TRIP'
}
export enum TripStatus {
    SCHEDULED = 'SCHEDULED',
    COMPLETED = 'COMPLETED'
}
export interface SearchRequestDto {
    source: string;
    destination: string;
    departureDate: string;
    returnDate: string;
    journeyType: JourneyType;
}

export interface TripSearchResponseDto {

    instanceId: string;
    routeName: string;
    busName: string;
    source: string;
    destination: string;
    departureTime: string;
    arrivalTime: string;
    fare: number;
    availableSeats: number;
    status: TripStatus;
    direction: string;

}
