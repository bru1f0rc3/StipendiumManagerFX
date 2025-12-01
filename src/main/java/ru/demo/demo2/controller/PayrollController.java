package ru.demo.demo2.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.demo.demo2.model.Accrual;
import ru.demo.demo2.model.Payroll;
import ru.demo.demo2.model.ScholarshipType;
import ru.demo.demo2.service.PayrollGenerationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PayrollController {
    
    @FXML
    private DatePicker monthPicker;
    
    @FXML
    private TableView<Payroll> payrollsTable;
    
    @FXML
    private TableColumn<Payroll, String> idColumn;
    
    @FXML
    private TableColumn<Payroll, String> monthColumn;
    
    @FXML
    private TableColumn<Payroll, String> createdColumn;
    
    @FXML
    private TableColumn<Payroll, String> statusColumn;
    
    @FXML
    private TableColumn<Payroll, String> fileColumn;
    
    @FXML
    private TableView<Accrual> accrualsTable;
    
    @FXML
    private TableColumn<Accrual, String> studentColumn;
    
    @FXML
    private TableColumn<Accrual, String> typeColumn;
    
    @FXML
    private TableColumn<Accrual, String> amountColumn;
    
    @FXML
    private Label resultLabel;
    
    private PayrollGenerationService payrollService;
    private ObservableList<Payroll> payrollsList;
    private ObservableList<Accrual> accrualsList;
    
    @FXML
    public void initialize() {
        payrollService = new PayrollGenerationService();
        payrollsList = FXCollections.observableArrayList();
        accrualsList = FXCollections.observableArrayList();

        idColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getId().toString()));
        monthColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getForMonth().format(DateTimeFormatter.ofPattern("MM.yyyy"))));
        createdColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCreatedAt() != null ? cd.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : ""));
        statusColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus() != null ? cd.getValue().getStatus().toString() : ""));
        fileColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getFilePath() != null ? cd.getValue().getFilePath() : ""));
        
        payrollsTable.setItems(payrollsList);
        studentColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStudent().getFio()));
        typeColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getType().getName()));
        amountColumn.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", cd.getValue().getAmount())));
        accrualsTable.setItems(accrualsList);
        payrollsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> { if (n != null) loadAccrualsForPayroll(n.getId()); });

        monthPicker.setValue(LocalDate.now());

        loadPayrolls();
    }

    @FXML
    private void onCreatePayrollClick() {
        LocalDate month = monthPicker.getValue();
        if (month == null) { resultLabel.setText("Выберите месяц"); return; }
        Payroll p = payrollService.createPayroll(month);
        loadPayrolls();
        resultLabel.setText("Ведомость создана! ID: " + p.getId());
        resultLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onGenerateFileClick() {
        Payroll p = payrollsTable.getSelectionModel().getSelectedItem();
        if (p == null) { resultLabel.setText("Выберите ведомость в таблице"); return; }
        String file = payrollService.generatePayrollFile(p);
        loadPayrolls();
        resultLabel.setText("PDF создан: " + file);
        resultLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onDeletePayrollClick() {
        Payroll p = payrollsTable.getSelectionModel().getSelectedItem();
        if (p == null) { resultLabel.setText("Выберите ведомость в таблице"); return; }
        payrollService.deletePayroll(p.getId());
        loadPayrolls();
        accrualsList.clear();
        resultLabel.setText("Ведомость удалена");
        resultLabel.setStyle("-fx-text-fill: blue;");
    }
    
    public void refresh() {
        loadPayrolls();
    }
    
    private void loadPayrolls() {
        try {
            payrollsList.clear();
            List<Payroll> payrolls = payrollService.getAllPayrolls();
            payrollsList.addAll(payrolls);
        } catch (Exception e) {
            resultLabel.setText("Ошибка при загрузке ведомостей: " + e.getMessage());
            resultLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void loadAccrualsForPayroll(Integer payrollId) {
        try {
            accrualsList.clear();
            Map<Integer, List<Accrual>> studentMap = new LinkedHashMap<>();
            for (Accrual a : payrollService.getAccrualsForPayroll(payrollId))
                studentMap.computeIfAbsent(a.getStudent().getId(), k -> new ArrayList<>()).add(a);

            for (List<Accrual> group : studentMap.values()) {
                if (group.isEmpty()) continue;
                if (group.size() == 1) { accrualsList.add(group.get(0)); continue; }
                
                Accrual first = group.get(0), merged = new Accrual();
                merged.setId(first.getId());
                merged.setStudent(first.getStudent());
                merged.setForMonth(first.getForMonth());
                merged.setStatus(first.getStatus());
                merged.setAmount(group.stream().map(Accrual::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                
                ScholarshipType type = new ScholarshipType();
                type.setName(group.stream().map(a -> a.getType().getName()).collect(Collectors.joining(", ")));
                merged.setType(type);
                accrualsList.add(merged);
            }
        } catch (Exception e) {
            resultLabel.setText("Ошибка при загрузке начислений: " + e.getMessage());
            resultLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
