package com.million.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sub_batch_table")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer batchNumber;

    @Column(name = "student_id")
    private List<Long> studentIds;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;


    private Long masterBatch_id;

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
