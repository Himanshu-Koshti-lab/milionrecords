package com.million.data.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class StudentBatchScheduler {

    @Autowired
    private StudentBatchService studentBatchService;

    public StudentBatchScheduler(StudentBatchService studentBatchService) {
        this.studentBatchService = studentBatchService;
        startFirstInstanceScheduler();
    }

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startFirstInstanceScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("Running scheduled student batch processing...");
                studentBatchService.processStudentsInParallel();
            } catch (Exception e) {
                log.error("Error during scheduled batch:", e);
            }
        }, 0, 20, TimeUnit.SECONDS); // first run immediately, then every 60 min
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdown();
        log.info("Student batch scheduler shut down.");
    }
}
