package ru.demo.demo2.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "scholarship_types")
public class ScholarshipType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;
    
    @Column(name = "requires_docs")
    private Boolean requiresDocs = false;
    
    public ScholarshipType() {}
    
    public ScholarshipType(String name, BigDecimal baseAmount, Boolean requiresDocs) {
        this.name = name;
        this.baseAmount = baseAmount;
        this.requiresDocs = requiresDocs;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getBaseAmount() {
        return baseAmount;
    }
    
    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }
    
    public Boolean getRequiresDocs() {
        return requiresDocs;
    }
    
    public void setRequiresDocs(Boolean requiresDocs) {
        this.requiresDocs = requiresDocs;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScholarshipType that = (ScholarshipType) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
