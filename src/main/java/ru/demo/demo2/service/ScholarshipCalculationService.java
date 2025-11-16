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
        List<Student> students = studentDao.findAll();
        List<ScholarshipType> types = typeDao.findAll();
        Status calculatedStatus = statusDao.findByCode("calculated");
        
        if (calculatedStatus == null) {
            throw new RuntimeException("Статус 'calculated' не найден в базе данных");
        }
        
        for (Student student : students) {
            for (ScholarshipType type : types) {
                if (checkConditions(student, type, month)) {
                    Accrual accrual = new Accrual();
                    accrual.setStudent(student);
                    accrual.setType(type);
                    accrual.setAmount(type.getBaseAmount());
                    accrual.setForMonth(month);
                    accrual.setStatus(calculatedStatus);
                    
                    try {
                        accrualDao.save(accrual);
                        result.add(accrual);
                    } catch (Exception e) {
                    }
                }
            }
        }
        
        return result;
    }

    private boolean checkConditions(Student student, ScholarshipType type, LocalDate month) {
        String name = type.getName().toLowerCase();

        if (name.contains("академическая")) {
            if (student.getAvgGrade().compareTo(new BigDecimal("4.00")) < 0) {
                return false;
            }
        }

        if (name.contains("губернаторская")) {
            if (student.getAvgGrade().compareTo(new BigDecimal("4.50")) < 0) {
                return false;
            }
        }

        if (name.contains("социальная")) {
            if (student.getHasSocialStatus() != true) {
                return false;
            }
        }

        if (type.getRequiresDocs() != null && type.getRequiresDocs()) {
            return groundDao.hasValidGround(student, type, month);
        }
        
        return true;
    }

    public List<Accrual> getAccrualsForMonth(LocalDate month) {
        List<Accrual> all = accrualDao.findAll();
        List<Accrual> result = new ArrayList<>();
        
        for (Accrual a : all) {
            if (a.getForMonth().equals(month)) {
                result.add(a);
            }
        }
        
        return result;
    }
}
