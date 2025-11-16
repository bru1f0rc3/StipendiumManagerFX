package ru.demo.demo2.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "student_groups")
public class StudentGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "group_code", nullable = false, unique = true, length = 50)
    private String groupCode;
    
    @Column(name = "course", nullable = false)
    private Integer course;
    
    @Column(name = "faculty", length = 100)
    private String faculty;
    
    public StudentGroup() {}
    
    public StudentGroup(String groupCode, Integer course, String faculty) {
        this.groupCode = groupCode;
        this.course = course;
        this.faculty = faculty;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getGroupCode() {
        return groupCode;
    }
    
    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
    
    public Integer getCourse() {
        return course;
    }
    
    public void setCourse(Integer course) {
        this.course = course;
    }
    
    public String getFaculty() {
        return faculty;
    }
    
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentGroup that = (StudentGroup) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return groupCode;
    }
}
