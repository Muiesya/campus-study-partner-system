from collections import defaultdict
from typing import Dict, List, Tuple

from sqlalchemy.orm import Session

from .models import Collaboration, Student, StudentCourse, StudentTag


class RecommendationEngine:
    """Lightweight rule-based recommender using shared courses, interests and past collaborations."""

    def __init__(self, db: Session):
        self.db = db

    def recommend(self, target_student_id: int, limit: int = 5) -> List[Tuple[int, float, List[str]]]:
        students = {s.student_id: s for s in self.db.query(Student).all()}
        target = students.get(target_student_id)
        if not target:
            return []

        course_map: Dict[int, set] = defaultdict(set)
        for sc in self.db.query(StudentCourse).all():
            course_map[sc.student_id].add(sc.course_id)

        tag_map: Dict[int, set] = defaultdict(set)
        for st in self.db.query(StudentTag).all():
            tag_map[st.student_id].add(st.tag_id)

        collaboration_map: Dict[Tuple[int, int], Collaboration] = {}
        for collab in self.db.query(Collaboration).all():
            key = tuple(sorted((collab.student_id1, collab.student_id2)))
            collaboration_map[key] = collab

        target_courses = course_map.get(target_student_id, set())
        target_tags = tag_map.get(target_student_id, set())

        scores: List[Tuple[int, float, List[str]]] = []
        for student_id, student in students.items():
            if student_id == target_student_id:
                continue

            shared_courses = target_courses & course_map.get(student_id, set())
            shared_tags = target_tags & tag_map.get(student_id, set())
            collab = collaboration_map.get(tuple(sorted((target_student_id, student_id))))
            collab_bonus = (collab.collaboration_count or 0) * 0.2 if collab else 0

            if not shared_courses and not shared_tags and not collab_bonus:
                continue

            score = len(shared_courses) * 0.6 + len(shared_tags) * 0.3 + collab_bonus
            reason_parts = []
            if shared_courses:
                reason_parts.append(f"共同课程 {len(shared_courses)} 门")
            if shared_tags:
                reason_parts.append(f"共同兴趣 {len(shared_tags)} 个")
            if collab_bonus:
                reason_parts.append("历史合作记录")

            scores.append((student_id, score, reason_parts))

        scores.sort(key=lambda x: x[1], reverse=True)
        return scores[:limit]
