package ru.demo.demo2.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "grounds")
public class Ground {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private ScholarshipType type;
    
    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "valid_until")
    private LocalDate validUntil;
    
    public Ground() {}
    
    public Ground(Student student, ScholarshipType type, String docType, LocalDate issueDate, LocalDate validUntil) {
        this.student = student;
        this.type = type;
        this.docType = docType;
        this.issueDate = issueDate;
        this.validUntil = validUntil;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public ScholarshipType getType() {
        return type;
    }
    
    public void setType(ScholarshipType type) {
        this.type = type;
    }
    
    public String getDocType() {
        return docType;
    }
    
    public void setDocType(String docType) {
        this.docType = docType;
    }
    
    public LocalDate getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
    
    public LocalDate getValidUntil() {
        return validUntil;
    }
    
    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ground ground = (Ground) o;
        return Objects.equals(id, ground.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return docType + " от " + issueDate;
    }
}
