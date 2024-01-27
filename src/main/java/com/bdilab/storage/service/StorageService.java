package com.bdilab.storage.service;

import com.bdilab.storage.dto.FileObjectInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface StorageService {
//    Map<String, Object> getFileInfos();
    void createBucket(String bucketName);
    void removeBucket(String bucketName);
    void removeObject(String bucketName, String objectName);
    void putObject(String bucketName, String objectName, MultipartFile file, Boolean isOverride);
    void putObjects(String bucketName, String objectName, List<MultipartFile> files, Boolean isOverride);
    List<FileObjectInfoDTO> listObjects(String bucketName);
    byte[] getFileObjectBytes(File fileObject);
    boolean bucketExists(String bucketName);
}
