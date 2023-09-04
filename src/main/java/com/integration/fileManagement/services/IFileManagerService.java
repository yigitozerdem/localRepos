package com.integration.fileManagement.services;

import com.integration.fileManagement.entities.FileInformationEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IFileManagerService {

    void insertNewSingleFile(FileInformationEntity fileInformationEntity) throws Exception;

    String saveFile(String fileName, MultipartFile multipartFile, String uploadPathAsString) throws IOException;

    boolean checkFileExtension(MultipartFile file);

    List<FileInformationEntity> getListOfFileInformation();

    byte[] convertFileToByteArray(File file) throws IOException;

    boolean checkFilePathIsExist(String fileName, String uploadPathAsString);

    File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException;
}
