package ru.demo.demo2.service;

import ru.demo.demo2.model.Accrual;
import ru.demo.demo2.model.ScholarshipType;
import ru.demo.demo2.model.Status;
import ru.demo.demo2.model.Student;
import ru.demo.demo2.repository.AccrualDao;
import ru.demo.demo2.repository.GroundDao;
import ru.demo.demo2.repository.ScholarshipTypeDao;
import ru.demo.demo2.repository.StatusDao;
import ru.demo.demo2.repository.StudentDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScholarshipCalculationService {
    
    private StudentDao studentDao = new StudentDao();
    private ScholarshipTypeDao typeDao = new ScholarshipTypeDao();
    private AccrualDao accrualDao = new AccrualDao();
    private GroundDao groundDao = new GroundDao();
    private StatusDao statusDao = new StatusDao();
    
    public List<Accrual> calculateScholarships(LocalDate month) {
        List<Accrual> result = new ArrayList<>();
        Status status = statusDao.findByCode("calculated");
        if (status == null) throw new RuntimeException("Статус 'calculated' не найден");
        
        for (Student s : studentDao.findAll()) {
            for (ScholarshipType t : typeDao.findAll()) {
                if (checkConditions(s, t, month)) {
                    Accrual a = new Accrual();
                    a.setStudent(s);
                    a.setType(t);
                    a.setAmount(t.getBaseAmount());
                    a.setForMonth(month);
                    a.setStatus(status);
                    try { accrualDao.save(a); result.add(a); } catch (Exception e) {}
                }
            }
        }
        return result;
    }

    private boolean checkConditions(Student s, ScholarshipType t, LocalDate m) {
        String name = t.getName().toLowerCase();
        if (name.contains("академическая") && s.getAvgGrade().compareTo(new BigDecimal("4.00")) < 0) return false;
        if (name.contains("губернаторская") && s.getAvgGrade().compareTo(new BigDecimal("4.50")) < 0) return false;
        if (name.contains("социальная") && !s.getHasSocialStatus()) return false;
        if (t.getRequiresDocs() != null && t.getRequiresDocs()) return groundDao.hasValidGround(s, t, m);
        return true;
    }

    public List<Accrual> getAccrualsForMonth(LocalDate month) {
        List<Accrual> result = new ArrayList<>();
        for (Accrual a : accrualDao.findAll())
            if (a.getForMonth().equals(month)) result.add(a);
        return result;
    }
}
