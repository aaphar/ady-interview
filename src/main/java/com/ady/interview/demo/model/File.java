package com.ady.interview.demo.model;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String fileName;

    private String originalFileName;

    @Column(unique = true, nullable = false)
    private String fileCode;

    @Column(nullable = false)
    private String fileUrl;

    private Long size;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;

}
