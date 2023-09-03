package com.integration.fileManagement.services;

import com.integration.fileManagement.entities.FileInformationEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFileManagerService {

    void insertNewSingleFile(FileInformationEntity fileInformationEntity) throws Exception;

    String saveFile(String fileName, MultipartFile multipartFile, String uploadPathAsString) throws IOException;

    boolean checkFileExtension(MultipartFile file);
}
