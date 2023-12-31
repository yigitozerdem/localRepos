package com.integration.fileManagement.services.implementation;

import com.integration.fileManagement.entities.FileInformationEntity;
import com.integration.fileManagement.repositories.IFileRepository;
import com.integration.fileManagement.services.IFileManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileManagerService implements IFileManagerService {
    private final IFileRepository fileRepository;
    AtomicLong sequence = new AtomicLong(1);
    @Autowired
    public FileManagerService(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public void insertNewSingleFile(FileInformationEntity fileInformationEntity) throws Exception{
        System.out.println(fileInformationEntity.getId());
        fileInformationEntity.setId(generateSequence());
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

    @Override
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

    @Override
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

    @Override
    public List<FileInformationEntity> getListOfFileInformation() {
        return fileRepository.findAll();
    }

    @Override
    public byte[] convertFileToByteArray(File file) throws IOException {
        byte[] bytesArray = null;

        try {
            bytesArray = Files.readAllBytes(Paths.get(file.getPath()));
            System.out.println();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
        return bytesArray;
    }

    @Override
    public File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }

    @Override
    public String deleteFile(Long id) throws IOException {
        fileRepository.deleteById(id);
        return "SUCCES";
    }

    @Override
    public String updateFile(Long id, MultipartFile multipartFile, String uploadPathAsString) throws IOException {
        FileInformationEntity fileInformation = fileRepository.findById(id).get();

        if (!multipartFile.isEmpty())
        {
            File fileToDelete = new File(fileInformation.getFilePath()+"/"+fileInformation.getFileName());
            if(fileToDelete.delete()){
                fileRepository.deleteById(id);
                saveFile(multipartFile.getOriginalFilename(),multipartFile,uploadPathAsString);
                fileInformation.setId(id);
                fileInformation.setFileName(multipartFile.getOriginalFilename());
                fileInformation.setFilePath(uploadPathAsString  + File.separator + multipartFile.getOriginalFilename());
                fileInformation.setFileExtension(multipartFile.getContentType());
                fileInformation.setFileSize(String.valueOf(multipartFile.getSize()));
                fileRepository.save(fileInformation);
                return "File updated successfully!";
            }else{
                return "File cannot delete!";
            }
        }else{
            return "There is no file for update!";
        }
    }

    @Override
    public byte[] getFileByteArray(Long id) throws IOException{

        FileInformationEntity fileInformation = fileRepository.findById(id).get();
        File file = new File(fileInformation.getFilePath() + "/" + fileInformation.getFileName());
        byte[] byteArray = convertFileToByteArray(file);
        return byteArray;
    }


    public Long generateSequence() {
        return sequence.getAndIncrement();
    }


}
