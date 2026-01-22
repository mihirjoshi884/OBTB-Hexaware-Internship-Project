package org.hexaware.userservice.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


public interface ImageUploadService {

    public Map uploadImage(MultipartFile file,String username,String publicID) throws RuntimeException,  IOException;
}
