package ru.demo.demo2.repository;

import org.hibernate.Session;
import ru.demo.demo2.model.Ground;
import ru.demo.demo2.model.ScholarshipType;
import ru.demo.demo2.model.Student;
import ru.demo.demo2.util.HibernateSession;

import java.time.LocalDate;
import java.util.List;

public class GroundDao extends BaseDao<Ground> {
    
    public GroundDao() {
        super(Ground.class);
    }
    
    public boolean hasValidGround(Student student, ScholarshipType type, LocalDate forMonth) {
        try (Session session = HibernateSession.getSessionFactory().openSession()) {
            String hql = "FROM Ground WHERE student.id = :studentId AND type.id = :typeId " +
                    "AND (validUntil IS NULL OR validUntil >= :forMonth)";
            List<Ground> grounds = session.createQuery(hql, Ground.class)
                    .setParameter("studentId", student.getId())
                    .setParameter("typeId", type.getId())
                    .setParameter("forMonth", forMonth)
                    .getResultList();

            return !grounds.isEmpty();
        }
    }
}
