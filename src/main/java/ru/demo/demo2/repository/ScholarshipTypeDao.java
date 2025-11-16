package ru.demo.demo2.repository;

import ru.demo.demo2.model.ScholarshipType;

public class ScholarshipTypeDao extends BaseDao<ScholarshipType> {
    
    public ScholarshipTypeDao() {
        super(ScholarshipType.class);
    }
}
