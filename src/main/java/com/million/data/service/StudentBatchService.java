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
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentBatchService {

    private final StudentRepository studentRepository;
    private final MasterBatchRepository masterBatchRepository;
    private final SubBatchRepository subBatchRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(5); // processing threads

    public void processStudentsInParallel() {
//        load student when empty
        if (studentRepository.findAll().isEmpty()) {
            ;
            studentRepository.saveAll(Student.generateStudents(10000));
        }

//        get all student
        List<Student> allStudents = studentRepository.findAll();

//        Chunk Size
        int batchSize = 2000;
//        Splits batches
        List<List<Student>> batches = splitIntoBatches(allStudents, batchSize);
        log.info("batch creation done....{}", batches.size());

//        Master batch Add
        MasterBatch master = MasterBatch.builder()
                .startTime(LocalDateTime.now())
                .status(MasterBatch.Status.START)
                .build();

        master = masterBatchRepository.save(master);
        log.info("add master record in table... {} with status {}", master.getMasterId(), master.getStatus());

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

        // Simulate work
        try {
            Thread.sleep(10);
            log.info("Delay between start and in_progress");
        } catch (InterruptedException ignored) {

        }


        List<Long> allProcessedIds = new ArrayList<>();

        try {
            master.setStatus(MasterBatch.Status.IN_PROGRESS);

            master = masterBatchRepository.save(master);
            log.info("master update {} with status {}", master.getMasterId(), master.getStatus());

            List<Future<List<Long>>> futures = new ArrayList<>();


            for (List<Student> batch : batches) {
                futures.add(executor.submit(() -> processBatch(batch)));

            }

            for (Future<List<Long>> future : futures) {
                allProcessedIds.addAll(future.get()); // waits for each task
            }

            master.setStudentIds(allProcessedIds);
            master.setStatus(MasterBatch.Status.COMPLETE);
            log.info("master process completed....");

        } catch (Exception e) {
            log.error("Error during parallel processing: ", e);
            master.setStatus(MasterBatch.Status.INTERRUPTED);
        } finally {
            master.setEndTime(LocalDateTime.now());
            masterBatchRepository.save(master);
            log.info("master process completed.... updated master");
        }
    }

    private List<Long> processBatch(List<Student> students) {
        log.info("batch task processing.... started");
        List<Long> processed = new ArrayList<>();
        for (Student student : students) {
            // Simulate work
            try {
                Thread.sleep(10); // simulate per-student task
            } catch (InterruptedException ignored) {
            }

            processed.add(student.getId());
        }
        log.info("batch task processing.... completed");
        return processed;
    }

    //    to Split into small batches
    private List<List<Student>> splitIntoBatches(List<Student> students, int batchSize)  {
        List<List<Student>> batches = new ArrayList<>();
        for (int i = 0; i < students.size(); i += batchSize) {
            int end = Math.min(i + batchSize, students.size());
            batches.add(students.subList(i, end));
        }
        return batches;
    }
}
