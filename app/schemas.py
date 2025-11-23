from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel


class StudentBase(BaseModel):
    student_id: int
    name: str
    major: Optional[str] = None
    enrollment_year: Optional[int] = None


class StudentCreate(StudentBase):
    pass


class Student(StudentBase):
    class Config:
        orm_mode = True


class CourseBase(BaseModel):
    course_id: str
    course_name: str
    credit: Optional[int] = None
    semester: Optional[str] = None


class CourseCreate(CourseBase):
    pass


class Course(CourseBase):
    class Config:
        orm_mode = True


class TagBase(BaseModel):
    tag_id: int
    tag_name: str


class TagCreate(TagBase):
    pass


class Tag(TagBase):
    class Config:
        orm_mode = True


class Recommendation(BaseModel):
    student_id: int
    similarity_score: float
    reason: List[str]


class CollaborationRecord(BaseModel):
    student_id1: int
    student_id2: int
    collaboration_count: int
    last_collab_time: datetime

    class Config:
        orm_mode = True
