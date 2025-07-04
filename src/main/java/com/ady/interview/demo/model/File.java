package com.ady.interview.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity(name = "saved_files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "File name cannot be blank")
    @Column(nullable = false)
    private String fileName;

    private String originalFileName;

    @NotBlank(message = "File code is required")
    @Size(min = 6, max = 8, message = "File code must be between 6 and 8 characters")
    @Column(unique = true, nullable = false)
    private String fileCode;

    @NotBlank(message = "File URL is required")
    @Column(nullable = false)
    private String fileUrl;

    @NotNull(message = "File size cannot be null")
    private Long size;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;

}
