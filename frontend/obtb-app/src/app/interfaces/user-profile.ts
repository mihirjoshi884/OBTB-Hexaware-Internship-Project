export interface UserProfile {
    userId: any;
    username: string;
    firstName: string;
    lastName: string;
    roleName: string;
    gender: string;
    dateOfBirth: string;       // Matches @JsonProperty("dateOfBirth")
    walletBalance: number;
    contact: string;
    email: string;
    profilePictureUrl: string;
    createdAt: string;
    passwordLastUpdated: string | null;
    lastLogin: string | null;   // Matches @JsonProperty("lastLogin")
}