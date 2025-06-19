package com.million.data.service;

import com.million.data.entity.MasterBatch;
import com.million.data.entity.Student;
import com.million.data.entity.SubBatch;
import com.million.data.entity.repository.MasterBatchRepository;
import com.million.data.entity.repository.StudentRepository;
import com.million.data.entity.repository.SubBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentBatchService {

    private final StudentRepository studentRepository;
    private final MasterBatchRepository masterBatchRepository;
    private final SubBatchRepository subBatchRepository;


    public static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newFixedThreadPool(2); // processing threads

    int batchSize = 20;


    public void processStudentsInParallel() {

        List<Student> allStudents = getAllStudents();

        clearSuccessFullyLoadedMasterAndSubBatchData();

        List<List<Student>> batches = splitIntoBatches(allStudents, batchSize);

        MasterBatch master = loadMastedBatchDataIntoDB();

        List<SubBatch> subBatches = getSubBatchList(batches, master);

        // Simulate work
        simulatedWork();

        master = getMasterBatchAndUpdateToInProgress(master);

        addBatchesToJoinPool(subBatches);

        try {
            master.setStudentIds(allStudents.stream().map(Student::getId).toList());
            handleMasterStatusWhenBatchFailed(master);
        } catch (Exception e) {
            log.error("Error during processing: ", e);
            master.setStatus(MasterBatch.Status.INTERRUPTED);
        } finally {
            master.setEndTime(LocalDateTime.now());
            masterBatchRepository.save(master);

            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            log.info("Batch processing completed.");
        }
    }

    private void handleMasterStatusWhenBatchFailed(MasterBatch master) {
        if (subBatchRepository.findAll().stream().anyMatch(b -> b.getStatus().equals(SubBatch.Status.FAILED))) {
            master.setStatus(MasterBatch.Status.INTERRUPTED);
        } else {
            master.setStatus(MasterBatch.Status.COMPLETE);
        }
    }

    private void addBatchesToJoinPool(List<SubBatch> subBatches) {
        try (ForkJoinPool forkJoinPool = new ForkJoinPool(1)) {
            forkJoinPool.submit(
                    () -> subBatches.parallelStream().forEach(batch -> processBatch(batch.getStudentIds(), batch))
            );
        }
    }

    private MasterBatch getMasterBatchAndUpdateToInProgress(MasterBatch master) {
        master.setStatus(MasterBatch.Status.IN_PROGRESS);
        master = masterBatchRepository.save(master);
        return master;
    }

    private static void simulatedWork() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }

    private MasterBatch loadMastedBatchDataIntoDB() {
        MasterBatch master = MasterBatch.builder()
                .startTime(LocalDateTime.now())
                .status(MasterBatch.Status.START)
                .build();

        master = masterBatchRepository.save(master);

        log.info("Master record created ID : {}", master.getMasterId());
        return master;
    }

    private void clearSuccessFullyLoadedMasterAndSubBatchData() {
        if (!masterBatchRepository.findAll().isEmpty() && masterBatchRepository.findAll().getFirst().getStatus() == MasterBatch.Status.COMPLETE) {
            log.info("Delete master and sub batch data of successfully loaded.");
            masterBatchRepository.deleteAll();
            subBatchRepository.deleteAll();
        }
    }

    private List<SubBatch> getSubBatchList(List<List<Student>> batches, MasterBatch master) {
        List<SubBatch> subBatches = new ArrayList<>();

        for (int i = 0; i < batches.size(); i++) {
            SubBatch subBatch = SubBatch.builder()
                    .batchNumber(i + 1)
                    .studentIds(batches.get(i).stream().map(Student::getId).toList())
                    .status(SubBatch.Status.NOT_STARTED)
                    .masterBatch_id(master.getMasterId())
                    .build();
            subBatches.add(subBatch);
        }

        subBatchRepository.saveAll(subBatches);
        return subBatches;
    }

    private List<Student> getAllStudents() {
        List<Student> allStudents = studentRepository.findAll();

        if (allStudents.isEmpty()) {
            allStudents = studentRepository.saveAll(Student.generateStudents(100));
        }
        return allStudents;
    }

    private List<Long> processBatch(List<Long> studentsBatch, SubBatch subBatch) {
        log.info("Starting sub-batch: {}", subBatch.getBatchNumber());
        List<Long> processed = new ArrayList<>();
        subBatch.setStatus(SubBatch.Status.IN_PROGRESS);
        subBatch.setStartTime(LocalDateTime.now());
        subBatchRepository.save(subBatch);

        try {
            somethingWithStudentBatch(studentsBatch, processed);
            subBatch.setStatus(SubBatch.Status.COMPLETED);
        } catch (Exception e) {
            log.error("Failed sub-batch: {}", subBatch.getBatchNumber(), e);
            subBatch.setStatus(SubBatch.Status.FAILED);
        } finally {
            subBatch.setEndTime(LocalDateTime.now());
            subBatchRepository.save(subBatch);
            log.info("Completed sub-batch: {}", subBatch.getBatchNumber());
        }

        return processed;
    }

    private static void somethingWithStudentBatch(List<Long> studentsBatch, List<Long> processed) throws Exception {
        for (Long student : studentsBatch) {
            try {
                Thread.sleep(5); // Simulate processing
                processed.add(student);
            } catch (InterruptedException e) {
                log.warn("Student processing interrupted: {}", student);
            }
            simulatedWork();
            if (ATOMIC_INTEGER.getAndIncrement() == 3) {
                throw new Exception("Temp Exception");
            }
        }
    }

    private List<List<Student>> splitIntoBatches(List<Student> students, int batchSize) {
        List<List<Student>> batches = new ArrayList<>();
        for (int i = 0; i < students.size(); i += batchSize) {
            int end = Math.min(i + batchSize, students.size());
            batches.add(students.subList(i, end));
        }
        log.info("Batch creation done: {} of size  {}", batches.size(), batchSize);
        return batches;
    }
}
