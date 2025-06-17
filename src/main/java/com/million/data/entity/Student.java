package com.million.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    private Long id;

    private String name;

    private String deptName;

    private int age;


    private static final String[] DEPARTMENTS = {
            "Computer Science", "Mechanical", "Electrical", "Civil", "Biotech", "Electronics"
    };
    private static final String[] NAMES = {
            "Aarav", "Vivaan", "Aditya", "Sai", "Arjun", "Krishna", "Ishaan", "Rohan", "Dev", "Aryan"
    };

    public static List<Student> generateStudents(int count) {
        List<Student> students = new ArrayList<>(count);
        Random random = new Random();

        for (long i = 1; i <= count; i++) {
            String name = NAMES[random.nextInt(NAMES.length)];
            String dept = DEPARTMENTS[random.nextInt(DEPARTMENTS.length)];
            int age = 18 + random.nextInt(10); // Age between 18 and 27

            students.add(new Student(i, name + " " + (1000 + random.nextInt(9000)), dept, age));
        }

        return students;
    }
}



