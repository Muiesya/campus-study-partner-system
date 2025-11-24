package com.campus.partner.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String major;

    @ManyToMany
    @JoinTable(name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_student_courses_student")),
            inverseJoinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_student_courses_course")))
    private Set<Course> courses = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "student_tags",
            joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_student_tags_student")),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_student_tags_tag")))
    private Set<InterestTag> tags = new HashSet<>();

    public Student() {
    }

    public Student(String name, String major) {
        this.name = name;
        this.major = major;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public Set<InterestTag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
