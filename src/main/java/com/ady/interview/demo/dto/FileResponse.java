package com.ady.interview.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileResponse {
    private String fileUrl;
}
