package org.hexaware.userservice.services;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hexaware.userservice.dtos.*;
import org.hexaware.userservice.exceptions.RoleNotFoundException;
import org.hexaware.userservice.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


public interface UserService {

    UserSummary addUser(UserCreationRequest user) throws RoleNotFoundException;

    String getUserRole(UUID roleMappingId) throws RoleNotFoundException;

    UserSummary getUser(UUID userId) throws RoleNotFoundException, UserNotFoundException;

    String getEmail(UUID userId);

    String getEmail(String username);

    ResponseDto<UserDashboardSummary> getUserDashboardSummary(String username) throws RoleNotFoundException;
    ResponseDto<FundsSummaryDto> addFunds(String username, Double amount);
    ResponseDto<FundsSummaryDto> withDrawFunds(String username, Double amount);
    // The method we will use for the Cloud Sync
    ResponseDto<UserDashboardSummary> updateUser(String username, MultipartFile file, UpdateUserRequest updateRequest) throws RoleNotFoundException, FileUploadException;
}
