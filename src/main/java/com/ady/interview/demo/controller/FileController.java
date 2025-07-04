package com.ady.interview.demo.controller;

import com.ady.interview.demo.dto.FileDownloadResponse;
import com.ady.interview.demo.dto.FileResponse;
import com.ady.interview.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Logger;

@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
@RequestMapping("/api/file")
public class FileController {

    Logger logger = Logger.getLogger(FileController.class.getName());

    @Autowired
    FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fileService.store(file));
    }

    @GetMapping("/download/{fileCode}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable("fileCode") String fileCode) {
        FileDownloadResponse response = fileService.download(fileCode);
        return ResponseEntity.ok()
                .header("Content-Disposition", response.getHeader())
                .body(response.getInputStream());
    }
}
