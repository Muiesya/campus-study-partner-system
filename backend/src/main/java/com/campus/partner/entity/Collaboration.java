package com.campus.partner.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "collaborations")
public class Collaboration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_a")
    private Student studentA;

    @ManyToOne
    @JoinColumn(name = "student_b")
    private Student studentB;

    private int count;

    public Collaboration() {
    }

    public Collaboration(Student studentA, Student studentB, int count) {
        this.studentA = studentA;
        this.studentB = studentB;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Student getStudentA() {
        return studentA;
    }

    public Student getStudentB() {
        return studentB;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
