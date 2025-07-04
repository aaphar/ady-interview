package com.ady.interview.demo.service;

import com.ady.interview.demo.dto.FileDownloadResponse;
import com.ady.interview.demo.dto.FileResponse;
import com.ady.interview.demo.exception.FileExpiredException;
import com.ady.interview.demo.exception.FileNotFoundException;
import com.ady.interview.demo.exception.MaxFileSizeExceededException;
import com.ady.interview.demo.exception.StorageException;
import com.ady.interview.demo.model.File;
import com.ady.interview.demo.repository.FileRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.Date;
import java.util.Random;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository, MinioClient minioClient) {
        this.fileRepository = fileRepository;
        this.minioClient = minioClient;
    }

    @Override
    @Transactional
    public FileResponse store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            if (file.getSize() > 10485760) { // 10 MB limit
                throw new MaxFileSizeExceededException("File size exceeds the maximum limit of 10 MB.");
            }
            String fileName = System.currentTimeMillis() + "_" + normalizeFileName(file.getOriginalFilename());
            log.info(fileName);
            log.info(file.toString());

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            String fileCode = generateFileCode();
            File storedFile = File.builder()
                    .size(file.getSize())
                    .fileCode(fileCode)
                    .fileUrl("http://localhost:8080/api/file/download/" + fileCode)
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .uploadDate(new Date())
                    .build();

            File savedFile = fileRepository.save(storedFile);
            return FileResponse.builder()
                    .fileUrl(savedFile.getFileUrl())
                    .build();
        } catch (StorageException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new StorageException(e.getMessage());
        } catch (MaxFileSizeExceededException e) {
            log.error("File size exceeds limit: {}", file.getOriginalFilename(), e);
            throw new MaxFileSizeExceededException(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String generateFileCode() {
        int length = 6 + new Random().nextInt(3); // 6, 7, or 8
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder fileCode = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            fileCode.append(chars.charAt(random.nextInt(chars.length())));
        }

        return fileCode.toString();
    }


    @Override
    public FileDownloadResponse download(String fileCode) {
        try {
            File file = fileRepository.findByFileCode(fileCode)
                    .orElseThrow(() -> new FileNotFoundException("File not found with code: " + fileCode));
            if (file.getUploadDate().before(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))) {
                throw new FileExpiredException("File is older than 24 hours and cannot be accessed.");
            }
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getFileName())
                            .build()
            );
            if (inputStream == null) {
                throw new FileNotFoundException("File not found in storage.");
            }
            return FileDownloadResponse.builder()
                    .header("attachment; filename=\"" + file.getFileName() + "\"")
                    .inputStream(new InputStreamResource(inputStream))
                    .build();
        } catch (FileExpiredException e) {
            log.info("File expired: {}", fileCode);
            throw new FileExpiredException(e.getMessage());
        } catch (FileNotFoundException e) {
            log.info("File not found: {}", fileCode);
            throw new FileNotFoundException(e.getMessage());
        } catch (Exception e) {
            log.info("Failed to read file: {}", fileCode);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String normalizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD);

        normalized = normalized.replaceAll("\\p{M}", "");

        String latinFileName = normalized.replaceAll("[^\\p{ASCII}]", "");

        latinFileName = latinFileName.replaceAll(" ", "_");

        return latinFileName;
    }
}
