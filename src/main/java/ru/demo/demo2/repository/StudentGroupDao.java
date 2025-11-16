package ru.demo.demo2.repository;

import ru.demo.demo2.model.StudentGroup;

public class StudentGroupDao extends BaseDao<StudentGroup> {
    
    public StudentGroupDao() {
        super(StudentGroup.class);
    }
}
