

export interface ResponseDto<T> {
  body: T;
  status: number;
  message: string;
}

export enum SeatType {
  SEATER = 'SEATER',
  SLEEPER = 'SLEEPER',
  WALKWAY = 'WALKWAY'
}

export enum SeatStatus {
  AVAILABLE = 'AVAILABLE',
  BOOKED = 'BOOKED',
  BLOCKED = 'BLOCKED',
  NONE = 'NONE'
}

export interface PassengerDetailDto {
  passengerName: string;
  age: number;
  gender: 'MALE' | 'FEMALE';
  seatNumber: string;
  idProofType: 'AADHAR' | 'PAN' | 'DRIVING_LICENSE';
  idNumber: string;
}

export interface BookingRequestDto {
  tripInstanceId: string;
  source: string;
  destination: string;
  userId: string;
  passengers: PassengerDetailDto[];
}

export interface BookingInitiatedResponse {
  bookingId: string;
  pnr: string;
  redirectUrl: string;
  amountToPay: number;
  source: string;
  destination: string;
}

export interface CurrentUserResponse{

  userId: string;
  username: string;
  enabled: boolean;
  accountNonLocked: boolean;
  authorities: any [];
}

export interface PrimaryPassangerDetailDto {
  userId: string;
  name: string;
  email: string;
}

export interface PrimaryPassangerDetailCreationRequest {
  userId: string;
  name: string;
  phone: string;
  email: string;
  emergencyContactName: string;
  emergencyContact: string;
}

export interface SeatMappingDto {
  instanceId: string;
  seats: EnrichedSeatDto[];
}

export interface EnrichedSeatDto {
  seatNumber: string;
  tripSeatId: string;
  seatType: SeatType;
  seatStatus: SeatStatus;
  isWindow: boolean;
  x_coordinate: number;
  y_coordinate: number;
  deck: number;
}