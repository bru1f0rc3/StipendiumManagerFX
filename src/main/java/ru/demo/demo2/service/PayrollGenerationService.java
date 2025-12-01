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
        Payroll existing = payrollDao.findByMonth(forMonth);
        if (existing != null) throw new RuntimeException("Ведомость за " + forMonth + " уже существует");

        Session session = HibernateSession.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Status draft = statusDao.findByCode("Черновик");
            if (draft == null) throw new RuntimeException("Статус 'Черновик' не найден");

            Payroll payroll = new Payroll();
            payroll.setForMonth(forMonth);
            payroll.setCreatedAt(LocalDateTime.now());
            payroll.setStatus(draft);
            payrollDao.save(payroll);

            for (Accrual a : session.createQuery("FROM Accrual WHERE forMonth = :m AND (payroll IS NULL OR payroll.id = :id)", Accrual.class)
                    .setParameter("m", forMonth).setParameter("id", payroll.getId()).getResultList()) {
                a.setPayroll(payroll);
                accrualDao.update(a);
            }
            tx.commit();
            return payroll;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
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
        String path = "payrolls/payroll_" + payroll.getId() + ".pdf";
        try {
            new File("payrolls").mkdirs();
            createPdf(new File(path), "ВЕДОМОСТЬ НА ВЫПЛАТУ СТИПЕНДИЙ", getAccrualsForPayroll(payroll.getId()));
            
            Status formed = statusDao.findByCode("Сформирована");
            if (formed == null) throw new RuntimeException("Статус 'Сформирована' не найден");
            payroll.setFilePath(path);
            payroll.setStatus(formed);
            payrollDao.update(payroll);
            return path;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании PDF: " + e.getMessage());
        }
    }


    private void createPdf(File file, String title, List<Accrual> accruals) throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        BaseFont bf = BaseFont.createFont("src/main/resources/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(bf, 10), titleFont = new Font(bf, 14, Font.BOLD), bold = new Font(bf, 10, Font.BOLD);

        doc.open();
        doc.add(new Paragraph(title, titleFont));
        doc.add(new Paragraph("Дата формирования: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), font));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        addCell(table, "№", font, true);
        addCell(table, "ФИО студента", font, true);
        addCell(table, "Группа", font, true);
        addCell(table, "Тип стипендии", font, true);
        addCell(table, "Сумма (руб.)", font, true);

        int num = 1;
        BigDecimal total = BigDecimal.ZERO;
        Map<String, List<Accrual>> studentMap = new LinkedHashMap<>();
        for (Accrual a : accruals)
            studentMap.computeIfAbsent(a.getStudent().getFio() + "|" + a.getStudent().getGroupCode(), k -> new ArrayList<>()).add(a);

        for (Map.Entry<String, List<Accrual>> e : studentMap.entrySet()) {
            String[] p = e.getKey().split("\\|");
            List<Accrual> list = e.getValue();
            BigDecimal sum = list.stream().map(Accrual::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            addCell(table, String.valueOf(num++), font, false);
            addCell(table, p[0], font, false);
            addCell(table, p[1], font, false);
            addCell(table, list.stream().map(a -> a.getType().getName()).collect(Collectors.joining(", ")), font, false);
            addCell(table, String.format("%.2f", sum), font, false);
            total = total.add(sum);
        }

        PdfPCell labelCell = new PdfPCell(new Phrase("Итого:", bold));
        labelCell.setColspan(4);
        labelCell.setPadding(5);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);
        
        PdfPCell amtCell = new PdfPCell(new Phrase(String.format("%.2f", total), bold));
        amtCell.setPadding(5);
        table.addCell(amtCell);

        doc.add(table);
        doc.close();
    }

    private void addCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
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
