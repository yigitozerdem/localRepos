package com.integration.fileManagement.repositories;

import com.integration.fileManagement.entities.FileInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFileRepository extends JpaRepository<FileInformationEntity,Long> {

}
