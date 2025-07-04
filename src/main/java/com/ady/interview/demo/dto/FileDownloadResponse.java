package com.ady.interview.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@Builder
@Data
public class FileDownloadResponse {
    private InputStream inputStream;
    private String fileName;
}
