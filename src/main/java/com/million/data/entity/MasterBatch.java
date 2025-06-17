package com.million.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "master_batches")
@Data                       // getters, setters, toString, equals & hashCode
@NoArgsConstructor          // no-args constructor (required by JPA)
@AllArgsConstructor         // all-args constructor
@Builder                    // convenient builder pattern
public class MasterBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long masterId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "student_id")
    private List<Long> studentIds = new ArrayList<>();

    // --------------------------------------------------------------------
    // Status values
    // --------------------------------------------------------------------
    public enum Status {
        START,          // just created, not yet running
        IN_PROGRESS,    // actively processing
        INTERRUPTED,    // stopped before finishing
        COMPLETE        // finished successfully
    }
}
