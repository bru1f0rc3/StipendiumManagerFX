package ru.demo.demo2.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "fio", nullable = false, length = 200)
    private String fio;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;
    
    @Column(name = "avg_grade", precision = 3, scale = 2)
    private BigDecimal avgGrade;
    
    @Column(name = "has_social_status")
    private Boolean hasSocialStatus = false;
    
    public Student() {}
    
    public Student(String fio, StudentGroup group, BigDecimal avgGrade, Boolean hasSocialStatus) {
        this.fio = fio;
        this.group = group;
        this.avgGrade = avgGrade;
        this.hasSocialStatus = hasSocialStatus;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getFio() {
        return fio;
    }
    
    public void setFio(String fio) {
        this.fio = fio;
    }
    
    public StudentGroup getGroup() {
        return group;
    }
    
    public void setGroup(StudentGroup group) {
        this.group = group;
    }
    
    public String getGroupCode() {
        return group != null ? group.getGroupCode() : "";
    }
    
    public BigDecimal getAvgGrade() {
        return avgGrade;
    }
    
    public void setAvgGrade(BigDecimal avgGrade) {
        this.avgGrade = avgGrade;
    }
    
    public Boolean getHasSocialStatus() {
        return hasSocialStatus;
    }
    
    public void setHasSocialStatus(Boolean hasSocialStatus) {
        this.hasSocialStatus = hasSocialStatus;
    }
    
    public String getFullName() {
        return fio;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return fio;
    }
}
