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

        idColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId().toString()));
        
        monthColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getForMonth().format(DateTimeFormatter.ofPattern("MM.yyyy"))));
        
        createdColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null)
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            else
                return new javafx.beans.property.SimpleStringProperty("");
        });
        
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : ""));
        
        fileColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFilePath() != null)
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFilePath());
            else
                return new javafx.beans.property.SimpleStringProperty("");
        });
        
        payrollsTable.setItems(payrollsList);

        studentColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudent().getFio()));
        
        typeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().getName()));
        
        amountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("%.2f", cellData.getValue().getAmount())));
        
        accrualsTable.setItems(accrualsList);

        payrollsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAccrualsForPayroll(newVal.getId());
            }
        });

        monthPicker.setValue(LocalDate.now());

        loadPayrolls();
    }

    @FXML
    private void onCreatePayrollClick() {
        LocalDate month = monthPicker.getValue();
        if (month == null) {
            resultLabel.setText("Выберите месяц");
            return;
        }
 
        Payroll payroll = payrollService.createPayroll(month);
        loadPayrolls();
        
        resultLabel.setText("Ведомость создана! ID: " + payroll.getId());
        resultLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onGenerateFileClick() {
        Payroll payroll = payrollsTable.getSelectionModel().getSelectedItem();
        if (payroll == null) {
            resultLabel.setText("Выберите ведомость в таблице");
            return;
        }

        String file = payrollService.generatePayrollFile(payroll);
        loadPayrolls();
        
        resultLabel.setText("PDF создан: " + file);
        resultLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onDeletePayrollClick() {
        Payroll payroll = payrollsTable.getSelectionModel().getSelectedItem();
        if (payroll == null) {
            resultLabel.setText("Выберите ведомость в таблице");
            return;
        }

        payrollService.deletePayroll(payroll.getId());
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
            List<Accrual> accruals = payrollService.getAccrualsForPayroll(payrollId);

            Map<Integer, List<Accrual>> studentMap = new LinkedHashMap<>();
            for (Accrual accrual : accruals) {
                studentMap.computeIfAbsent(accrual.getStudent().getId(), k -> new ArrayList<>()).add(accrual);
            }

            for (List<Accrual> group : studentMap.values()) {
                if (group.isEmpty()) continue;

                Accrual first = group.get(0);

                if (group.size() == 1) {
                    accrualsList.add(first);
                } else {
                    Accrual merged = new Accrual();
                    merged.setId(first.getId());
                    merged.setStudent(first.getStudent());
                    merged.setForMonth(first.getForMonth());
                    merged.setStatus(first.getStatus());

                    String types = group.stream()
                        .map(a -> a.getType().getName())
                        .collect(Collectors.joining(", "));

                    BigDecimal total = group.stream()
                        .map(Accrual::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                    ScholarshipType tempType = new ScholarshipType();
                    tempType.setName(types);
                    merged.setType(tempType);
                    merged.setAmount(total);
                    
                    accrualsList.add(merged);
                }
            }
        } catch (Exception e) {
            resultLabel.setText("Ошибка при загрузке начислений: " + e.getMessage());
            resultLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
