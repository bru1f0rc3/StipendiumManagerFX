package ru.demo.demo2.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "statuses")
public class Status {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "status_code", nullable = false, unique = true, length = 20)
    private String statusCode;
    
    @Column(name = "description", length = 100)
    private String description;
    
    public Status() {}
    
    public Status(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return Objects.equals(id, status.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return description != null ? description : statusCode;
    }
}
