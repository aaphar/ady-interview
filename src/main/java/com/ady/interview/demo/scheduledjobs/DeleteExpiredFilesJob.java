package com.ady.interview.demo.scheduledjobs;

import com.ady.interview.demo.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class DeleteExpiredFilesJob {

    @Autowired
    FileService fileService;

    // every day once
    @Scheduled(cron = "0 0 0 * * ?")
    @Async("asyncTaskExecutor")
    public void deleteFiles() {
        log.info("Deleting expired files...");
        fileService.deleteExpiredFiles();
        log.info("Expired files deleted successfully.");
    }

}
