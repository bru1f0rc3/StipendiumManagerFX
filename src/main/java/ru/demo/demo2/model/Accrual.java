package ru.demo.demo2.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "accruals", 
       uniqueConstraints = @UniqueConstraint(
           name = "unique_accrual",
           columnNames = {"student_id", "for_month", "type_id"}
       ))
public class Accrual {
    
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
    
    @ManyToOne
    @JoinColumn(name = "payroll_id")
    private Payroll payroll;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "for_month", nullable = false)
    private LocalDate forMonth;
    
    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;
    
    public Accrual() {}
    
    public Accrual(Student student, ScholarshipType type, Payroll payroll, BigDecimal amount, LocalDate forMonth, Status status) {
        this.student = student;
        this.type = type;
        this.payroll = payroll;
        this.amount = amount;
        this.forMonth = forMonth;
        this.status = status;
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
    
    public Payroll getPayroll() {
        return payroll;
    }
    
    public void setPayroll(Payroll payroll) {
        this.payroll = payroll;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getForMonth() {
        return forMonth;
    }
    
    public void setForMonth(LocalDate forMonth) {
        this.forMonth = forMonth;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Accrual accrual = (Accrual) o;
        return Objects.equals(id, accrual.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Начисление #" + id;
    }
}
