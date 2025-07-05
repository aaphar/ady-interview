package com.ady.interview.demo.service;

import com.ady.interview.demo.exception.FileExpiredException;
import com.ady.interview.demo.exception.FileNotFoundException;
import com.ady.interview.demo.exception.MaxFileSizeExceededException;
import com.ady.interview.demo.exception.StorageException;
import com.ady.interview.demo.model.File;
import com.ady.interview.demo.repository.FileRepository;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @InjectMocks
    FileServiceImpl fileService;

    @Mock
    MinioClient minioClient;

    @Mock
    FileRepository fileRepository;

    @Mock
    MultipartFile multipartFile;

    @Mock
    GetObjectResponse objectResponse;

    @Mock
    ObjectWriteResponse objectWriteResponse;

    @Mock
    InputStream inputStream;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(fileService, "bucketName", "ady-bucket");
    }

    @Test
    public void fileEmptyThrowStorageException() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThrows(StorageException.class, () -> fileService.store(multipartFile));

        verify(minioClient, never()).putObject(any());
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void fileSizeExceedsLimitThrowMaxFileSizeExceededException() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(15_000_000L);

        assertThrows(MaxFileSizeExceededException.class, () -> fileService.store(multipartFile));

        verify(minioClient, never()).putObject(any());
        verify(fileRepository, never()).save(any());
    }

    @Test
    public void fileDownloadExpiredThrowFileExpiredException() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File file = File.builder()
                .fileCode("expiredCode")
                .fileName("expiredFile.txt")
                .uploadDate(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 25))
                .fileUrl("http://localhost:8080/api/file/download/expiredCode")
                .originalFileName("expiredFile.txt")
                .size(1000L)
                .build();
        when(fileRepository.findByFileCode(anyString())).thenReturn(Optional.of(file));
        assertThrows(FileExpiredException.class, () -> fileService.download("expiredCode"));
        verify(fileRepository, times(1)).findByFileCode("expiredCode");
        verify(minioClient, never()).getObject(any());
    }

    @Test
    public void fileDownloadNotFoundThrowStorageException() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        when(fileRepository.findByFileCode(anyString())).thenReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> fileService.download("nonExistentCode"));
        verify(fileRepository, times(1)).findByFileCode("nonExistentCode");
        verify(minioClient, never()).getObject(any());
    }

    @Test
    public void fileStorageSuccess() throws Exception {
        File file = File.builder()
                .fileCode("successCode")
                .uploadDate(new Date())
                .fileUrl("http://localhost:8080/api/file/download/successCode")
                .fileName("successFile.txt")
                .originalFileName("successFile.txt")
                .size(1000L)
                .build();

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1000L);
        when(multipartFile.getOriginalFilename()).thenReturn("successFile.txt");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getContentType()).thenReturn("text/plain");

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(objectWriteResponse);
        when(fileRepository.save(any(File.class))).thenReturn(file);

        fileService.store(multipartFile);

        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
        verify(fileRepository, times(1)).save(any(File.class));
    }

    @Test
    public void fileDownloadSuccess() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File file = File.builder()
                .fileCode("validCode")
                .uploadDate(new Date())
                .fileUrl("http://localhost:8080/api/file/download/validCode")
                .fileName("validFile.txt")
                .originalFileName("validFile.txt")
                .size(1000L)
                .build();

        when(fileRepository.findByFileCode("validCode")).thenReturn(Optional.of(file));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse);

        fileService.download("validCode");

        verify(fileRepository, times(1)).findByFileCode("validCode");
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));

    }
}
