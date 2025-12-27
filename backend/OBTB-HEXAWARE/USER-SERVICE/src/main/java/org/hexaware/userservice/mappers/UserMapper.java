package org.hexaware.userservice.mappers;


import org.hexaware.userservice.dtos.UserCreationRequest;
import org.hexaware.userservice.dtos.UserSummary;
import org.hexaware.userservice.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(UserCreationRequest request);
    User toUser(UserSummary user);


    UserSummary toUserSummary(User user);

    // Optional: update existing user entity from DTO (useful for PATCH/PUT). Keep id unchanged.
//    @Mapping(target = "userId", ignore = true)
    void updateUserFromDto(UserCreationRequest request, @MappingTarget User user);
}
