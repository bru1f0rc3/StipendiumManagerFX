package ru.demo.demo2.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.demo.demo2.util.HibernateSession;

import java.util.List;

public abstract class BaseDao<T> {
    private final Class<T> clazz;

    public BaseDao(Class<T> clazz){
        this.clazz = clazz;
    }

    protected Session getCurrentSession() {
        return HibernateSession.getSessionFactory().openSession();
    }

    public void save(T entity){
        Session session = getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.persist(entity);
        tx.commit();
        session.close();
    }
    
    public T findById(Integer id){
        Session session = getCurrentSession();
        T item = session.find(clazz, id);
        session.close();
        return item;
    }

    public void delete(T entity){
        Session session = getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.remove(entity);
        tx.commit();
        session.close();
    }

    public void update(T entity){
        Session session = getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.merge(entity);
        tx.commit();
        session.close();
    }

    public void deleteById(long id){
        T entity = findById((int) id);
        delete(entity);
    }

    public List<T> findAll(){
        Session session = getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<T> items = session.createQuery("from " + clazz.getName(), clazz).list();
        tx.commit();
        session.close();
        return items;
    }
}