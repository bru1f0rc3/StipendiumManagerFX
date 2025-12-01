package ru.demo.demo2.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.demo.demo2.model.Status;
import ru.demo.demo2.util.HibernateSession;

import java.util.List;

public class StatusDao extends BaseDao<Status> {
    
    public StatusDao() {
        super(Status.class);
    }
    
    public Status findByCode(String statusCode) {
        try (Session session = HibernateSession.getSessionFactory().openSession()) {
            String hql = "FROM Status WHERE statusCode = :statusCode";
            Query<Status> query = session.createQuery(hql, Status.class);
            query.setParameter("statusCode", statusCode);

            List<Status> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        }
    }
}
