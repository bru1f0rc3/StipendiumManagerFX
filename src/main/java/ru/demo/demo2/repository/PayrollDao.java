package ru.demo.demo2.repository;

import org.hibernate.Session;
import ru.demo.demo2.model.Payroll;
import ru.demo.demo2.util.HibernateSession;

import java.time.LocalDate;
import java.util.List;

public class PayrollDao extends BaseDao<Payroll> {
    
    public PayrollDao() {
        super(Payroll.class);
    }

    public Payroll findByMonth(LocalDate forMonth) {
        Session session = HibernateSession.getSessionFactory().openSession();
        try {
            String hql = "FROM Payroll WHERE forMonth = :forMonth";
            List<Payroll> payrolls = session.createQuery(hql, Payroll.class)
                    .setParameter("forMonth", forMonth)
                    .getResultList();
            
            return payrolls.isEmpty() ? null : payrolls.get(0);
        } finally {
            session.close();
        }
    }
}
