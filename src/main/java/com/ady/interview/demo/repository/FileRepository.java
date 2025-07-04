package com.ady.interview.demo.repository;

import com.ady.interview.demo.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByFileCode(String fileCode);

    @Query("SELECT f FROM saved_files f WHERE f.uploadDate <= :cutoffDate")
    List<File> findExpiredFiles(@Param("cutoffDate") Date cutoffDate);
}
