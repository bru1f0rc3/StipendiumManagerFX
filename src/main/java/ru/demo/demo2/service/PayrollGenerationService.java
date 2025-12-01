package ru.demo.demo2.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));


        BaseFont baseFont = BaseFont.createFont(
            "src/main/resources/fonts/arial.ttf",
            BaseFont.IDENTITY_H,
            BaseFont.EMBEDDED);

        Font font = new Font(baseFont, 10);
        Font titleFont = new Font(baseFont, 14, Font.BOLD);

        document.open();

        document.add(new Paragraph(title, titleFont));

        Font dateFont = new Font(baseFont, 10);
        String dateText = "Дата формирования: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        document.add(new Paragraph(dateText, dateFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        addCell(table, "№", font, true);
        addCell(table, "ФИО студента", font, true);
        addCell(table, "Группа", font, true);
        addCell(table, "Тип стипендии", font, true);
        addCell(table, "Сумма (руб.)", font, true);

        int num = 1;
        BigDecimal totalAmount = BigDecimal.ZERO;

        Map<String, List<Accrual>> studentAccruals = new LinkedHashMap<>();
        for (Accrual accrual : accruals) {
            String key = accrual.getStudent().getFio() + "|" + accrual.getStudent().getGroupCode();
            studentAccruals.computeIfAbsent(key, k -> new ArrayList<>()).add(accrual);
        }

        for (Map.Entry<String, List<Accrual>> entry : studentAccruals.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String fio = parts[0];
            String groupCode = parts[1];
            
            List<Accrual> studentList = entry.getValue();

            String types = studentList.stream()
                .map(a -> a.getType().getName())
                .collect(Collectors.joining(", "));

            BigDecimal studentTotal = studentList.stream()
                .map(Accrual::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            addCell(table, String.valueOf(num++), font, false);
            addCell(table, fio, font, false);
            addCell(table, groupCode, font, false);
            addCell(table, types, font, false);
            addCell(table, String.format("%.2f", studentTotal), font, false);

            totalAmount = totalAmount.add(studentTotal);
        }

        Font boldFont = new Font(baseFont, 10, Font.BOLD);
        PdfPCell totalLabelCell = new PdfPCell(
            new Phrase("Итого:", boldFont));
        totalLabelCell.setColspan(4);
        totalLabelCell.setPadding(5);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalLabelCell);

        PdfPCell totalAmountCell = new PdfPCell(
            new Phrase(String.format("%.2f", totalAmount), boldFont));
        totalAmountCell.setPadding(5);
        table.addCell(totalAmountCell);

        document.add(table);
        document.close();
    }

    private void addCell(PdfPTable table, String text,
                        Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(
            new Phrase(text, font));
        cell.setPadding(5);

        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        
        table.addCell(cell);
    }

    public void deletePayroll(Integer payrollId) {
        payrollDao.deleteById(payrollId);
    }
}
