package com.ady.interview.demo.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.InputStreamResource;

@Builder
@Data
public class FileDownloadResponse {
    private InputStreamResource inputStream;
    private String header;
}
