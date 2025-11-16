package ru.demo.demo2.service;

import org.junit.jupiter.api.Test;
import ru.demo.demo2.model.Accrual;
import ru.demo.demo2.model.Payroll;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PayrollGenerationServiceTest {
    
    private final PayrollGenerationService payrollService = new PayrollGenerationService();
    private final ScholarshipCalculationService calcService = new ScholarshipCalculationService();
    
    @Test
    void shouldCreatePayroll() {
        LocalDate month = LocalDate.of(2026, 7, 1);
        calcService.calculateScholarships(month);
        
        Payroll payroll = payrollService.createPayroll(month);
        
        assertNotNull(payroll);
        assertNotNull(payroll.getId());
        assertEquals("Черновик", payroll.getStatus().getStatusCode());
        assertEquals(month, payroll.getForMonth());
    }
    
    @Test
    void shouldGetAccrualsForPayroll() {
        LocalDate month = LocalDate.of(2026, 8, 1);
        calcService.calculateScholarships(month);
        Payroll payroll = payrollService.createPayroll(month);
        
        List<Accrual> accruals = payrollService.getAccrualsForPayroll(payroll.getId());
        
        assertFalse(accruals.isEmpty());
        for (Accrual accrual : accruals) {
            assertEquals(payroll.getId(), accrual.getPayroll().getId());
        }
    }
    
    @Test
    void shouldGeneratePdfFile() {
        LocalDate month = LocalDate.of(2026, 9, 1);
        calcService.calculateScholarships(month);
        Payroll payroll = payrollService.createPayroll(month);
        
        String path = payrollService.generatePayrollFile(payroll);
        
        assertNotNull(path);
        assertTrue(path.endsWith(".pdf"));
        
        File file = new File(path);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
    
    @Test
    void shouldNotCreateDuplicatePayroll() {
        LocalDate month = LocalDate.of(2026, 10, 1);
        calcService.calculateScholarships(month);
        payrollService.createPayroll(month);
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            payrollService.createPayroll(month);
        });
        
        assertTrue(exception.getMessage().contains("уже существует"));
    }
    
    @Test
    void shouldUpdateStatusAfterPdfGeneration() {
        LocalDate month = LocalDate.of(2026, 11, 1);
        calcService.calculateScholarships(month);
        Payroll payroll = payrollService.createPayroll(month);
        
        payrollService.generatePayrollFile(payroll);
        
        List<Payroll> payrolls = payrollService.getAllPayrolls();
        Payroll updated = payrolls.stream()
            .filter(p -> p.getId().equals(payroll.getId()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(updated);
        assertEquals("Сформирована", updated.getStatus().getStatusCode());
        assertNotNull(updated.getFilePath());
    }
}
