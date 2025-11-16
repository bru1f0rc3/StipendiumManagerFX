package ru.demo.demo2.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ru.demo.demo2.model.Accrual;

import java.util.List;

public class AccrualDao extends BaseDao<Accrual> {
    
    public AccrualDao() {
        super(Accrual.class);
    }

    public List<Accrual> getAccrualsForStudent(Integer studentId) {
        Session session = getCurrentSession();
        Transaction tx = session.beginTransaction();
        Query<Accrual> query = session.createQuery(
            "FROM Accrual a WHERE a.student.id = :studentId ORDER BY a.forMonth DESC", 
            Accrual.class
        );
        query.setParameter("studentId", studentId);
        List<Accrual> accruals = query.list();
        tx.commit();
        session.close();
        return accruals;
    }
}
