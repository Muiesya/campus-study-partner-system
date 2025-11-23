package com.campus.partner.service;

import com.campus.partner.entity.Course;
import com.campus.partner.entity.InterestTag;
import com.campus.partner.entity.Student;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GraphSyncService {
    private final Neo4jClient neo4jClient;

    public GraphSyncService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public void syncStudent(Student student) {
        neo4jClient.query("MERGE (s:Student {id:$id}) SET s.name=$name, s.major=$major")
                .bindAll(Map.of(
                        "id", student.getId(),
                        "name", student.getName(),
                        "major", student.getMajor()))
                .run();
    }

    public void syncCourse(Course course) {
        neo4jClient.query("MERGE (c:Course {id:$id}) SET c.name=$name, c.description=$description")
                .bindAll(Map.of(
                        "id", course.getId(),
                        "name", course.getName(),
                        "description", course.getDescription()))
                .run();
    }

    public void syncTag(InterestTag tag) {
        neo4jClient.query("MERGE (t:Tag {id:$id}) SET t.name=$name")
                .bindAll(Map.of(
                        "id", tag.getId(),
                        "name", tag.getName()))
                .run();
    }

    public void linkStudentCourse(Student student, Course course) {
        syncStudent(student);
        syncCourse(course);
        neo4jClient.query("MATCH (s:Student {id:$sid}), (c:Course {id:$cid}) MERGE (s)-[:ENROLLED_IN]->(c)")
                .bindAll(Map.of("sid", student.getId(), "cid", course.getId()))
                .run();
    }

    public void linkStudentTag(Student student, InterestTag tag) {
        syncStudent(student);
        syncTag(tag);
        neo4jClient.query("MATCH (s:Student {id:$sid}), (t:Tag {id:$tid}) MERGE (s)-[:HAS_TAG]->(t)")
                .bindAll(Map.of("sid", student.getId(), "tid", tag.getId()))
                .run();
    }

    public void recordCollaboration(Student a, Student b) {
        syncStudent(a);
        syncStudent(b);
        long first = Math.min(a.getId(), b.getId());
        long second = Math.max(a.getId(), b.getId());
        neo4jClient.query("""
                        MATCH (s1:Student {id:$first}), (s2:Student {id:$second})
                        MERGE (s1)-[r:COLLABORATED_WITH]->(s2)
                        ON CREATE SET r.count = 1
                        ON MATCH SET r.count = coalesce(r.count, 0) + 1
                        """)
                .bindAll(Map.of("first", first, "second", second))
                .run();
    }
}
