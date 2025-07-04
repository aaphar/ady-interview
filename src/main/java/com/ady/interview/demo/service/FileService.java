package com.ady.interview.demo.service;

import com.ady.interview.demo.dto.FileDownloadResponse;
import com.ady.interview.demo.dto.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileResponse store(MultipartFile file);

    FileDownloadResponse download(String fileCode);

}
