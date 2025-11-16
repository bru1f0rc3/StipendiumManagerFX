package ru.demo.demo2.repository;

import ru.demo.demo2.model.Student;

public class StudentDao extends BaseDao<Student> {
    
    public StudentDao() {
        super(Student.class);
    }
}
