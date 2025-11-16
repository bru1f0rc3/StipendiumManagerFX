package ru.demo.demo2.service;

import org.junit.jupiter.api.Test;
import ru.demo.demo2.model.Accrual;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScholarshipCalculationServiceTest {
    
    private final ScholarshipCalculationService service = new ScholarshipCalculationService();
    
    @Test
    void shouldCalculateScholarships() {
        LocalDate month = LocalDate.of(2026, 1, 1);
        List<Accrual> result = service.calculateScholarships(month);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
    
    @Test
    void shouldNotCreateDuplicates() {
        LocalDate month = LocalDate.of(2026, 2, 1);

        List<Accrual> firstCalculation = service.calculateScholarships(month);
        List<Accrual> secondCalculation = service.calculateScholarships(month);
        
        assertEquals(0, secondCalculation.size());
    }
    
    @Test
    void shouldMatchBaseAmounts() {
        LocalDate month = LocalDate.of(2026, 3, 1);
        List<Accrual> accruals = service.calculateScholarships(month);
        
        for (Accrual accrual : accruals) {
            assertEquals(accrual.getType().getBaseAmount(), accrual.getAmount());
        }
    }
    
    @Test
    void shouldGetAccrualsForMonth() {
        LocalDate month = LocalDate.of(2026, 4, 1);
        service.calculateScholarships(month);
        
        List<Accrual> retrieved = service.getAccrualsForMonth(month);
        
        assertFalse(retrieved.isEmpty());
        for (Accrual accrual : retrieved) {
            assertEquals(month, accrual.getForMonth());
        }
    }
    
    @Test
    void shouldSetCorrectStatus() {
        LocalDate month = LocalDate.of(2026, 5, 1);
        List<Accrual> result = service.calculateScholarships(month);
        
        for (Accrual accrual : result) {
            assertNotNull(accrual.getStatus());
            assertEquals("calculated", accrual.getStatus().getStatusCode());
        }
    }
}
