package com.ady.interview.demo.controller;

import com.ady.interview.demo.dto.FileDownloadResponse;
import com.ady.interview.demo.dto.FileResponse;
import com.ady.interview.demo.dto.MessageResponse;
import com.ady.interview.demo.model.File;
import com.ady.interview.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.logging.Logger;

@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
@RequestMapping("/api/file")
public class FileController {

    Logger logger = Logger.getLogger(FileController.class.getName());

    @Autowired
    FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File uploadedFile = fileService.store(file);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(FileResponse.builder()
                            .fileUrl(uploadedFile.getFileUrl())
                            .build());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/download/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {
        try {
            FileDownloadResponse response = fileService.download(fileCode);
            if (response.getInputStream() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            }
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + response.getFileName() + "\"")
                    .body(new InputStreamResource(response.getInputStream()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(HttpStatus.NOT_FOUND, e.getMessage()));
        }
    }
}
