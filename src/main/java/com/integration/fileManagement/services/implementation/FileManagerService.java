package com.integration.fileManagement.services.implementation;

import com.integration.fileManagement.entities.FileInformationEntity;
import com.integration.fileManagement.repositories.IFileRepository;
import com.integration.fileManagement.services.IFileManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileManagerService implements IFileManagerService {
    private final IFileRepository fileRepository;
    @Autowired
    public FileManagerService(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public void insertNewSingleFile(FileInformationEntity fileInformationEntity) throws Exception{
        System.out.println(fileInformationEntity.getFileName());
        fileRepository.save(fileInformationEntity);
    }

    @Override
    public String saveFile(String fileName, MultipartFile multipartFile, String uploadPathAsString)
            throws IOException {
        String message = "";
        Path uploadPath = Paths.get(uploadPathAsString);
        boolean checkFilePathIsExist = checkFilePathIsExist(fileName,uploadPathAsString);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        try (InputStream inputStream = multipartFile.getInputStream()) {
            if(checkFilePathIsExist){
                String tempFileName = processToKeepSameFiles(fileName);
                fileName = tempFileName;
            }
            Path filePath = uploadPath.resolve( fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            message = fileName;
        } catch (IOException ioe) {
            throw new IOException("Could not save file: " + fileName, ioe);
        }
        return message;
    }

    public boolean checkFileExtension(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        return (contentType != null &&
                (contentType.equals("application/pdf") || contentType.equals("application/docx") ||
                    contentType.equals("application/xlsx") ||
                        contentType.equals("image/jpeg") || contentType.equals("image/jpg") ||
                        contentType.equals("image/png"))) ||
                (originalFilename != null &&
                        (originalFilename.toLowerCase().endsWith(".pdf") ||
                                originalFilename.toLowerCase().endsWith(".docx") || originalFilename.toLowerCase().endsWith(".xlsx") ||
                                originalFilename.toLowerCase().endsWith(".jpeg") || originalFilename.toLowerCase().endsWith(".jpg") ||
                                originalFilename.toLowerCase().endsWith(".png")));
    }

    public boolean checkFilePathIsExist(String fileName, String uploadPathAsString){
        File filePath = new File(uploadPathAsString + File.separator + fileName);
        if(filePath.exists()){
            return true;
        }else{
            return false;
        }
    }

    private String processToKeepSameFiles(String fileName){
        String filetype = "";
        Pattern regex = Pattern.compile("\\.([A-Za-z0-9]+)$");
        Matcher regexMatcher = regex.matcher(fileName != null ? fileName : "");
        long recordCount = fileRepository.count();
        if (regexMatcher.find()) {
            filetype = regexMatcher.group();
        }

        int endIndex = fileName.indexOf(".");
        if (endIndex != -1) {
            String substringFileName = fileName.substring(0, endIndex);
            fileName = substringFileName + "(" + recordCount + ")" +filetype;
            System.out.println(fileName);
            return fileName;
        } else {
            System.out.println("File extension was not found in the string.");
            return fileName;
        }
    }
}
