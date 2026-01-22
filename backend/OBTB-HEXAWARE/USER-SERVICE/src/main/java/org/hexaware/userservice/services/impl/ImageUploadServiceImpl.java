package org.hexaware.userservice.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.hexaware.userservice.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map uploadImage(MultipartFile file, String username, String publicID) throws RuntimeException, IOException {
        Map<String, Object> imgUploadParam = new HashMap<>();

        if (publicID == null || publicID.trim().isEmpty()) {
            String newId = "profile_pics/" + username + "_" + UUID.randomUUID().toString();
            imgUploadParam.put("public_id", newId);
        } else {
            imgUploadParam.put("public_id", publicID);
            imgUploadParam.put("overwrite", true);
        }


        imgUploadParam.put("resource_type", "image");

        try {
            return this.cloudinary.uploader().upload(file.getBytes(), imgUploadParam);
        } catch (IOException ex) {
            throw new RuntimeException("Cloudinary upload failed", ex);
        }
    }
}
