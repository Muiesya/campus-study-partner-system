from datetime import datetime
from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, UniqueConstraint
from sqlalchemy.orm import relationship

from .database import Base


class Student(Base):
    __tablename__ = "students"

    student_id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), nullable=False)
    major = Column(String(50), nullable=True)
    enrollment_year = Column(Integer, nullable=True)

    courses = relationship("StudentCourse", back_populates="student", cascade="all, delete-orphan")
    tags = relationship("StudentTag", back_populates="student", cascade="all, delete-orphan")


class Course(Base):
    __tablename__ = "courses"

    course_id = Column(String(20), primary_key=True, index=True)
    course_name = Column(String(100), nullable=False)
    credit = Column(Integer, nullable=True)
    semester = Column(String(20), nullable=True)

    students = relationship("StudentCourse", back_populates="course", cascade="all, delete-orphan")


class StudentCourse(Base):
    __tablename__ = "student_courses"
    __table_args__ = (UniqueConstraint("student_id", "course_id", name="uq_student_course"),)

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(Integer, ForeignKey("students.student_id"), nullable=False)
    course_id = Column(String(20), ForeignKey("courses.course_id"), nullable=False)

    student = relationship("Student", back_populates="courses")
    course = relationship("Course", back_populates="students")


class Tag(Base):
    __tablename__ = "interest_tags"

    tag_id = Column(Integer, primary_key=True, index=True)
    tag_name = Column(String(50), nullable=False, unique=True)

    students = relationship("StudentTag", back_populates="tag", cascade="all, delete-orphan")


class StudentTag(Base):
    __tablename__ = "student_tags"
    __table_args__ = (UniqueConstraint("student_id", "tag_id", name="uq_student_tag"),)

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(Integer, ForeignKey("students.student_id"), nullable=False)
    tag_id = Column(Integer, ForeignKey("interest_tags.tag_id"), nullable=False)

    student = relationship("Student", back_populates="tags")
    tag = relationship("Tag", back_populates="students")


class Collaboration(Base):
    __tablename__ = "collaborations"
    __table_args__ = (UniqueConstraint("student_id1", "student_id2", name="uq_collaboration_pair"),)

    id = Column(Integer, primary_key=True, index=True)
    student_id1 = Column(Integer, ForeignKey("students.student_id"), nullable=False)
    student_id2 = Column(Integer, ForeignKey("students.student_id"), nullable=False)
    collaboration_count = Column(Integer, default=0)
    last_collab_time = Column(DateTime, default=datetime.utcnow)
