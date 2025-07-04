package com.ady.interview.demo.service;

import com.ady.interview.demo.dto.FileDownloadResponse;
import com.ady.interview.demo.model.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    File store(MultipartFile file);

    FileDownloadResponse download(String fileCode);

}
