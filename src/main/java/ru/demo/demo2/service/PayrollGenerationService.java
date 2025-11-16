package ru.demo.demo2.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.demo.demo2.model.Accrual;
import ru.demo.demo2.model.Payroll;
import ru.demo.demo2.model.Status;
import ru.demo.demo2.repository.AccrualDao;
import ru.demo.demo2.repository.PayrollDao;
import ru.demo.demo2.repository.StatusDao;
import ru.demo.demo2.util.HibernateSession;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PayrollGenerationService {
    
    private PayrollDao payrollDao;
    private AccrualDao accrualDao;
    private StatusDao statusDao;
    
    public PayrollGenerationService() {
        this.payrollDao = new PayrollDao();
        this.accrualDao = new AccrualDao();
        this.statusDao = new StatusDao();
    }

    public Payroll createPayroll(LocalDate forMonth) {
        Payroll existingPayroll = payrollDao.findByMonth(forMonth);
        if (existingPayroll != null) {
            throw new RuntimeException("Ведомость за " + forMonth + " уже существует (ID: " + existingPayroll.getId() + ")");
        }
        
        Session session = HibernateSession.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();

            Status draftStatus = statusDao.findByCode("Черновик");
            if (draftStatus == null) {
                throw new RuntimeException("Статус 'Черновик' не найден в базе данных");
            }

            Payroll payroll = new Payroll();
            payroll.setForMonth(forMonth);
            payroll.setCreatedAt(LocalDateTime.now());
            payroll.setStatus(draftStatus);
            
            payrollDao.save(payroll);

            String hql = "FROM Accrual WHERE forMonth = :forMonth AND (payroll IS NULL OR payroll.id = :payrollId)";
            List<Accrual> accruals = session.createQuery(hql, Accrual.class)
                    .setParameter("forMonth", forMonth)
                    .setParameter("payrollId", payroll.getId())
                    .getResultList();
            
            for (Accrual accrual : accruals) {
                accrual.setPayroll(payroll);
                accrualDao.update(accrual);
            }
            
            transaction.commit();
            return payroll;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка при создании ведомости: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }
    
    public List<Payroll> getAllPayrolls() {
        return payrollDao.findAll();
    }

    public List<Accrual> getAccrualsForPayroll(Integer payrollId) {
        Session session = HibernateSession.getSessionFactory().openSession();
        try {
            String hql = "FROM Accrual WHERE payroll.id = :payrollId ORDER BY student.fio";
            return session.createQuery(hql, Accrual.class)
                    .setParameter("payrollId", payrollId)
                    .getResultList();
        } finally {
            session.close();
        }
    }

    public String generatePayrollFile(Payroll payroll) {
        List<Accrual> accruals = getAccrualsForPayroll(payroll.getId());
        String fileName = "payroll_" + payroll.getId() + ".pdf";
        String filePath = "payrolls/" + fileName;
        
        try {
            File dir = new File("payrolls");
            dir.mkdirs();

            String title = "ВЕДОМОСТЬ НА ВЫПЛАТУ СТИПЕНДИЙ";

            File file = new File(filePath);
            createPdf(file, title, accruals);

            Status formedStatus = statusDao.findByCode("Сформирована");
            if (formedStatus == null) {
                throw new RuntimeException("Статус 'Сформирована' не найден в базе данных");
            }

            payroll.setFilePath(filePath);
            payroll.setStatus(formedStatus);
            payrollDao.update(payroll);
            
            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании PDF: " + e.getMessage());
        }
    }


    private void createPdf(File file, String title, List<Accrual> accruals) throws Exception {

        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
        

        com.itextpdf.text.pdf.BaseFont baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            "src/main/resources/fonts/arial.ttf", 
            com.itextpdf.text.pdf.BaseFont.IDENTITY_H, 
            com.itextpdf.text.pdf.BaseFont.EMBEDDED);
        
        com.itextpdf.text.Font font = new com.itextpdf.text.Font(baseFont, 10);
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont, 14, com.itextpdf.text.Font.BOLD);
        
        document.open();

        document.add(new com.itextpdf.text.Paragraph(title, titleFont));
        document.add(com.itextpdf.text.Chunk.NEWLINE);

        com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(5);
        table.setWidthPercentage(100);

        addCell(table, "№", font, true);
        addCell(table, "ФИО студента", font, true);
        addCell(table, "Группа", font, true);
        addCell(table, "Тип стипендии", font, true);
        addCell(table, "Сумма (руб.)", font, true);

        int num = 1;
        for (Accrual accrual : accruals) {
            addCell(table, String.valueOf(num++), font, false);
            addCell(table, accrual.getStudent().getFio(), font, false);
            addCell(table, accrual.getStudent().getGroupCode(), font, false);
            addCell(table, accrual.getType().getName(), font, false);
            addCell(table, String.format("%.2f", accrual.getAmount()), font, false);
        }
        
        document.add(table);
        document.close();
    }

    private void addCell(com.itextpdf.text.pdf.PdfPTable table, String text, 
                        com.itextpdf.text.Font font, boolean isHeader) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(text, font));
        cell.setPadding(5);
        
        if (isHeader) {
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        }
        
        table.addCell(cell);
    }

    public void deletePayroll(Integer payrollId) {
        payrollDao.deleteById(payrollId);
    }
}
