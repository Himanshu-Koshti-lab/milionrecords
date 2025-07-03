package com.million.data.resource;

import com.million.data.entity.MasterBatch;
import com.million.data.entity.Student;
import com.million.data.entity.SubBatch;
import com.million.data.entity.repository.MasterBatchRepository;
import com.million.data.entity.repository.StudentRepository;
import com.million.data.entity.repository.SubBatchRepository;
import com.million.data.service.StudentBatchScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class DataProcessResource {

    @Autowired
    MasterBatchRepository masterBatchRepository;

    @Autowired
    SubBatchRepository subBatchRepository;

    @Autowired
    StudentBatchScheduler studentBatchScheduler;

    @Autowired
    StudentRepository studentRepository;

    @PostMapping("/trigger-scheduler")
    public String triggerSchedulerManually() {
        studentBatchScheduler.startFirstInstanceScheduler();
        return "Batch processing triggered manually.";
    }

    @GetMapping("/getMasterTable")
    public List<MasterBatch> getMasterResponse() {
        return masterBatchRepository.findAll();
    }

    @GetMapping("/fetch-subBatchData")
    public List<SubBatch> getSubBatchData() {
        return subBatchRepository.findAll();
    }

    @GetMapping("/getStudentById")
    public ResponseEntity<Student> getStudentById(@RequestParam Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
