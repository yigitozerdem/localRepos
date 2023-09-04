package com.integration.fileManagement.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "files_information")
public class FileInformationEntity {
    @Id
    private Long id;
    private String fileName;
    private String fileSize;
    private String filePath;
    private String fileExtension;
}
