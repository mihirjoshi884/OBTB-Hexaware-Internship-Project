package org.hexaware.oauthservice.mappers;


import org.hexaware.oauthservice.dtos.AuthUserCreationRequest;
import org.hexaware.oauthservice.dtos.Summary;
import org.hexaware.oauthservice.dtos.UserCreationRequest;
import org.hexaware.oauthservice.dtos.UserSummary;
import org.hexaware.oauthservice.entites.AuthIdentity;
import org.mapstruct.factory.Mappers;

@org.mapstruct.Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    AuthIdentity toAuthIdentity(AuthUserCreationRequest authUserCreationRequest);
    AuthIdentity toAuthIdentity(UserSummary userSummary);
    UserCreationRequest toUserCreationRequest(AuthUserCreationRequest authUserCreationRequest);
    UserSummary toUserSummary(UserCreationRequest userCreationRequest);
    Summary toSummary(AuthIdentity authIdentity);
    Summary toSummary(UserSummary userSummary);
}
