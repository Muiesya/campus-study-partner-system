package com.campus.partner.service;

import com.campus.partner.dto.RecommendationEntry;
import com.campus.partner.entity.Collaboration;
import com.campus.partner.entity.Student;
import com.campus.partner.repository.CollaborationRepository;
import com.campus.partner.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final StudentRepository studentRepository;
    private final CollaborationRepository collaborationRepository;

    private static final double COURSE_WEIGHT = 0.6;
    private static final double TAG_WEIGHT = 0.3;
    private static final double COLLAB_WEIGHT = 0.2;

    public RecommendationService(StudentRepository studentRepository, CollaborationRepository collaborationRepository) {
        this.studentRepository = studentRepository;
        this.collaborationRepository = collaborationRepository;
    }

    public List<RecommendationEntry> recommendFor(Long studentId, int limit) {
        Student target = studentRepository.findById(studentId).orElseThrow();
        Set<Long> courseIds = target.getCourses().stream().map(c -> c.getId()).collect(Collectors.toSet());
        Set<Long> tagIds = target.getTags().stream().map(t -> t.getId()).collect(Collectors.toSet());

        List<RecommendationEntry> entries = new ArrayList<>();
        for (Student candidate : studentRepository.findAll()) {
            if (candidate.getId().equals(studentId)) {
                continue;
            }
            double score = 0;
            List<String> reasons = new ArrayList<>();

            int sharedCourses = intersectionSize(courseIds, candidate.getCourses().stream().map(c -> c.getId()).collect(Collectors.toSet()));
            if (sharedCourses > 0) {
                double part = sharedCourses * COURSE_WEIGHT;
                score += part;
                reasons.add("共同课程 " + sharedCourses + " 门");
            }

            int sharedTags = intersectionSize(tagIds, candidate.getTags().stream().map(t -> t.getId()).collect(Collectors.toSet()));
            if (sharedTags > 0) {
                double part = sharedTags * TAG_WEIGHT;
                score += part;
                reasons.add("共同兴趣 " + sharedTags + " 个");
            }

            int collab = collaborationCount(target, candidate);
            if (collab > 0) {
                double part = collab * COLLAB_WEIGHT;
                score += part;
                reasons.add("历史合作 " + collab + " 次");
            }

            if (score > 0) {
                entries.add(new RecommendationEntry(candidate.getId(), candidate.getName(), score, reasons));
            }
        }

        return entries.stream()
                .sorted(Comparator.comparing(RecommendationEntry::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private int collaborationCount(Student a, Student b) {
        Long first = Math.min(a.getId(), b.getId());
        Long second = Math.max(a.getId(), b.getId());
        return collaborationRepository.findPair(first, second).map(Collaboration::getCount).orElse(0);
    }

    private int intersectionSize(Set<Long> left, Set<Long> right) {
        Set<Long> tmp = new HashSet<>(left);
        tmp.retainAll(right);
        return tmp.size();
    }
}
