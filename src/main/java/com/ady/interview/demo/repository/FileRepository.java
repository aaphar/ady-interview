package com.ady.interview.demo.repository;

import com.ady.interview.demo.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByFileCode(String fileCode);
}
