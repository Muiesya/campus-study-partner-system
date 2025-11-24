package com.campus.partner.controller;

import com.campus.partner.dto.*;
import com.campus.partner.entity.Collaboration;
import com.campus.partner.entity.Course;
import com.campus.partner.entity.InterestTag;
import com.campus.partner.entity.Student;
import com.campus.partner.repository.CollaborationRepository;
import com.campus.partner.repository.CourseRepository;
import com.campus.partner.repository.InterestTagRepository;
import com.campus.partner.repository.StudentRepository;
import com.campus.partner.service.GraphSyncService;
import com.campus.partner.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class StudentController {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final InterestTagRepository tagRepository;
    private final CollaborationRepository collaborationRepository;
    private final RecommendationService recommendationService;
    private final GraphSyncService graphSyncService;

    public StudentController(StudentRepository studentRepository, CourseRepository courseRepository,
                             InterestTagRepository tagRepository, CollaborationRepository collaborationRepository,
                             RecommendationService recommendationService, GraphSyncService graphSyncService) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.tagRepository = tagRepository;
        this.collaborationRepository = collaborationRepository;
        this.recommendationService = recommendationService;
        this.graphSyncService = graphSyncService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ok");
        status.put("students", studentRepository.count());
        return status;
    }

    @PostMapping("/students")
    public Student createStudent(@Valid @RequestBody StudentRequest request) {
        Student student = new Student(request.getName(), request.getMajor());
        Student saved = studentRepository.save(student);
        graphSyncService.syncStudent(saved);
        return saved;
    }

    @GetMapping("/students")
    public List<Student> listStudents() {
        return studentRepository.findAll();
    }

    @PostMapping("/courses")
    public Course createCourse(@Valid @RequestBody CourseRequest request) {
        Course course = new Course(request.getName(), request.getDescription());
        Course saved = courseRepository.save(course);
        graphSyncService.syncCourse(saved);
        return saved;
    }

    @GetMapping("/courses")
    public List<Course> listCourses() {
        return courseRepository.findAll();
    }

    @PostMapping("/tags")
    public InterestTag createTag(@Valid @RequestBody TagRequest request) {
        InterestTag tag = new InterestTag(request.getName());
        InterestTag saved = tagRepository.save(tag);
        graphSyncService.syncTag(saved);
        return saved;
    }

    @GetMapping("/tags")
    public List<InterestTag> listTags() {
        return tagRepository.findAll();
    }

    @PostMapping("/students/{studentId}/courses/{courseId}")
    @Transactional
    public ResponseEntity<Student> addCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();
        student.getCourses().add(course);
        course.getStudents().add(student);
        Student saved = studentRepository.saveAndFlush(student);
        graphSyncService.linkStudentCourse(saved, course);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/students/{studentId}/tags/{tagId}")
    @Transactional
    public ResponseEntity<Student> addTag(@PathVariable Long studentId, @PathVariable Long tagId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        InterestTag tag = tagRepository.findById(tagId).orElseThrow();
        student.getTags().add(tag);
        tag.getStudents().add(student);
        Student saved = studentRepository.saveAndFlush(student);
        graphSyncService.linkStudentTag(saved, tag);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/collaborations")
    public Collaboration recordCollaboration(@Valid @RequestBody CollaborationRequest request) {
        if (request.getStudentId1().equals(request.getStudentId2())) {
            throw new IllegalArgumentException("合作双方不能相同");
        }
        Long first = Math.min(request.getStudentId1(), request.getStudentId2());
        Long second = Math.max(request.getStudentId1(), request.getStudentId2());
        Student s1 = studentRepository.findById(first).orElseThrow();
        Student s2 = studentRepository.findById(second).orElseThrow();
        Collaboration collaboration = collaborationRepository.findPair(first, second)
                .orElseGet(() -> new Collaboration(s1, s2, 0));
        collaboration.setCount(collaboration.getCount() + 1);
        Collaboration saved = collaborationRepository.save(collaboration);
        graphSyncService.recordCollaboration(s1, s2);
        return saved;
    }

    @GetMapping("/students/{studentId}/recommendations")
    public List<RecommendationEntry> recommend(@PathVariable Long studentId, @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.recommendFor(studentId, limit);
    }
}
