package com.integration.fileManagement.controllers;

import com.integration.fileManagement.entities.FileInformationEntity;
import com.integration.fileManagement.services.IFileManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping(path="/api")
public class FileManagerController {

    @Autowired
    IFileManagerService fileManagerService;

    @Value("${file.upload.directory}")
    private String fileUploadFolderAsString;

    private String userDirectory;

    private String fileUploadDirectoryAsString;

    private String fileName = "";

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public FileManagerController(IFileManagerService fileManagerService) {
        this.fileManagerService = fileManagerService;
        this.userDirectory = System.getProperty("user.dir");
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.ok("File size exceeds the maximum allowed size.");
            } else {
                if(fileManagerService.checkFileExtension(file)){
                    FileInformationEntity fileInformationEntity = new FileInformationEntity();
                    fileUploadDirectoryAsString = userDirectory + File.separator + fileUploadFolderAsString;
                    fileName = fileManagerService.saveFile(file.getOriginalFilename(),file,fileUploadDirectoryAsString);
                    if(!fileName.equals("")){
                        fileInformationEntity.setFileName(fileName);
                        fileInformationEntity.setFilePath(fileUploadDirectoryAsString + File.separator + fileName);
                        fileInformationEntity.setFileSize(String.valueOf(file.getSize()));
                        fileInformationEntity.setFileExtension(file.getContentType());
                        fileManagerService.insertNewSingleFile(fileInformationEntity);
                        return ResponseEntity.ok("File uploaded and saved.");
                    }else{
                        return ResponseEntity.ok("File could not uploaded!");
                    }
                }else{
                    return ResponseEntity.ok("File Extension does not supported!");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(@RequestParam("fileName") String fileName,
                                             @RequestParam("fileId") Long fileId) throws IOException {
        fileUploadDirectoryAsString = userDirectory + File.separator + fileUploadFolderAsString;
        String filePath = fileUploadDirectoryAsString + File.separator + fileName;
        File fileToDelete = new File(filePath);
        File parentDirectory = fileToDelete.getParentFile();
        if (!parentDirectory.exists()) {
            return ResponseEntity.badRequest().body("Parent directory not found.");
        }
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                fileManagerService.deleteFile(fileId);
                return ResponseEntity.ok("File deleted successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the file!");
            }
        } else {
            return ResponseEntity.badRequest().body("File not found, check your file is exist!");
        }
    }

    @GetMapping("/listOfFiles")
    public ResponseEntity<List<FileInformationEntity>> listOfFiles(){
        List<FileInformationEntity> files = fileManagerService.getListOfFileInformation(); // Assuming findAll() retrieves all files
        return ResponseEntity.ok(files);
    }

    @GetMapping("/getFileContextWithByteArray/{fileId}")
    public byte[] convertFileToByteArray(@PathVariable("fileId") Long fileId) throws IOException {
      return fileManagerService.getFileByteArray(fileId);
    }

    @PutMapping("/updateFile/{fileId}")
    public ResponseEntity<String> updateFile(@PathVariable("fileId") Long fileId,
                                              @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                fileUploadDirectoryAsString = userDirectory + File.separator + fileUploadFolderAsString;
                File existingFile = new File(fileUploadDirectoryAsString);
                if (existingFile.exists()) {
                        String response = fileManagerService.updateFile(fileId,file,fileUploadDirectoryAsString);
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body("File not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the file.");
            }
        } else {
            return ResponseEntity.badRequest().body("No file provided for update.");
        }
    }
}


