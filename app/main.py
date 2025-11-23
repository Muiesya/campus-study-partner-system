from datetime import datetime
from typing import List

from fastapi import Depends, FastAPI, HTTPException, Query
from sqlalchemy.orm import Session

from . import models, schemas
from .config import settings
from .database import Base, engine, get_db
from .recommendation import RecommendationEngine

Base.metadata.create_all(bind=engine)

app = FastAPI(title=settings.app_name, version="0.1.0", debug=settings.debug)


@app.get("/health")
def health_check():
    """Simple liveness probe for IDE / deployment checks."""
    return {"status": "ok"}


@app.post("/students", response_model=schemas.Student)
def create_student(student: schemas.StudentCreate, db: Session = Depends(get_db)):
    db_student = db.query(models.Student).filter(models.Student.student_id == student.student_id).first()
    if db_student:
        raise HTTPException(status_code=400, detail="学生已存在")
    db_student = models.Student(**student.dict())
    db.add(db_student)
    db.commit()
    db.refresh(db_student)
    return db_student


@app.get("/students/{student_id}", response_model=schemas.Student)
def get_student(student_id: int, db: Session = Depends(get_db)):
    student = db.query(models.Student).filter(models.Student.student_id == student_id).first()
    if not student:
        raise HTTPException(status_code=404, detail="学生不存在")
    return student


@app.post("/courses", response_model=schemas.Course)
def create_course(course: schemas.CourseCreate, db: Session = Depends(get_db)):
    db_course = db.query(models.Course).filter(models.Course.course_id == course.course_id).first()
    if db_course:
        raise HTTPException(status_code=400, detail="课程已存在")
    db_course = models.Course(**course.dict())
    db.add(db_course)
    db.commit()
    db.refresh(db_course)
    return db_course


@app.post("/students/{student_id}/courses", response_model=schemas.Student)
def add_course_to_student(student_id: int, course_id: str = Query(...), db: Session = Depends(get_db)):
    student = db.query(models.Student).filter(models.Student.student_id == student_id).first()
    course = db.query(models.Course).filter(models.Course.course_id == course_id).first()
    if not student or not course:
        raise HTTPException(status_code=404, detail="学生或课程不存在")

    exists = db.query(models.StudentCourse).filter(
        models.StudentCourse.student_id == student_id, models.StudentCourse.course_id == course_id
    ).first()
    if exists:
        return student

    db.add(models.StudentCourse(student_id=student_id, course_id=course_id))
    db.commit()
    db.refresh(student)
    return student


@app.post("/tags", response_model=schemas.Tag)
def create_tag(tag: schemas.TagCreate, db: Session = Depends(get_db)):
    db_tag = db.query(models.Tag).filter(models.Tag.tag_id == tag.tag_id).first()
    if db_tag:
        raise HTTPException(status_code=400, detail="标签已存在")
    db_tag = models.Tag(**tag.dict())
    db.add(db_tag)
    db.commit()
    db.refresh(db_tag)
    return db_tag


@app.post("/students/{student_id}/tags", response_model=schemas.Student)
def add_tag_to_student(student_id: int, tag_id: int = Query(...), db: Session = Depends(get_db)):
    student = db.query(models.Student).filter(models.Student.student_id == student_id).first()
    tag = db.query(models.Tag).filter(models.Tag.tag_id == tag_id).first()
    if not student or not tag:
        raise HTTPException(status_code=404, detail="学生或标签不存在")

    exists = db.query(models.StudentTag).filter(
        models.StudentTag.student_id == student_id, models.StudentTag.tag_id == tag_id
    ).first()
    if exists:
        return student

    db.add(models.StudentTag(student_id=student_id, tag_id=tag_id))
    db.commit()
    db.refresh(student)
    return student


@app.post("/collaborations")
def log_collaboration(student_id1: int, student_id2: int, db: Session = Depends(get_db)):
    if student_id1 == student_id2:
        raise HTTPException(status_code=400, detail="合作双方不能相同")

    for sid in (student_id1, student_id2):
        if not db.query(models.Student).filter(models.Student.student_id == sid).first():
            raise HTTPException(status_code=404, detail="学生不存在")

    pair = tuple(sorted((student_id1, student_id2)))
    record = db.query(models.Collaboration).filter(
        models.Collaboration.student_id1.in_(pair), models.Collaboration.student_id2.in_(pair)
    ).first()

    if record:
        record.collaboration_count = (record.collaboration_count or 0) + 1
        record.last_collab_time = datetime.utcnow()
    else:
        record = models.Collaboration(
            student_id1=pair[0], student_id2=pair[1], collaboration_count=1, last_collab_time=datetime.utcnow()
        )
        db.add(record)

    db.commit()
    return {"message": "合作记录已更新"}


@app.get("/students/{student_id}/recommendations", response_model=List[schemas.Recommendation])
def recommend_peers(student_id: int, limit: int = 5, db: Session = Depends(get_db)):
    engine = RecommendationEngine(db)
    recommendations = [
        schemas.Recommendation(student_id=sid, similarity_score=score, reason=reason)
        for sid, score, reason in engine.recommend(student_id, limit)
    ]
    return recommendations


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
