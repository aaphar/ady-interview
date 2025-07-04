package com.ady.interview.demo.service;

import com.ady.interview.demo.exception.FileExpiredException;
import com.ady.interview.demo.exception.MaxFileSizeExceededException;
import com.ady.interview.demo.exception.StorageException;
import com.ady.interview.demo.model.File;
import com.ady.interview.demo.repository.FileRepository;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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
                .uploadDate(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))
                .fileUrl("http://localhost:8080/api/file/download/expiredCode")
                .originalFileName("expiredFile.txt")
                .size(1000L)
                .build();
        when(fileRepository.findByFileCode(anyString())).thenReturn(Optional.ofNullable(file));
        assertThrows(FileExpiredException.class, () -> fileService.download("expiredCode"));
        verify(fileRepository, times(1)).findByFileCode("expiredCode");
        verify(minioClient, never()).getObject(any());
    }

    @Test
    public void fileDownloadNotFoundThrowStorageException() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        when(fileRepository.findByFileCode(anyString())).thenReturn(Optional.empty());
        assertThrows(StorageException.class, () -> fileService.download("nonExistentCode"));
        verify(fileRepository, times(1)).findByFileCode("nonExistentCode");
        verify(minioClient, never()).getObject(any());
    }

    @Test
    public void fileStorageSuccess() throws Exception {
        File file = File.builder()
                .fileCode("expiredCode")
                .uploadDate(new Date())
                .fileUrl("http://localhost:8080/api/file/download/expiredCode")
                .fileName("1699100000000_expiredFile.txt")
                .originalFileName("expiredFile.txt")
                .size(1000L)
                .build();

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(9_000_000L);
        when(multipartFile.getOriginalFilename()).thenReturn("expiredFile.txt");
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(multipartFile.getContentType()).thenReturn("text/plain");

        when(fileRepository.save(any(File.class))).thenReturn(file);

        ReflectionTestUtils.setField(fileService, "bucketName", "ady-bucket");

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
                .originalFileName("validFile.txt")
                .size(1000L)
                .build();

        when(fileRepository.findByFileCode("validCode")).thenReturn(Optional.of(file));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse);

    }
}
