package ru.demo.demo2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.demo.demo2.model.Accrual;
import ru.demo.demo2.model.ScholarshipType;
import ru.demo.demo2.service.ScholarshipCalculationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalculationController {
    
    @FXML
    private DatePicker monthPicker;
    
    @FXML
    private Button calculateButton;
    
    @FXML
    private TableView<Accrual> accrualsTable;
    
    @FXML
    private TableColumn<Accrual, String> idColumn;
    
    @FXML
    private TableColumn<Accrual, String> studentColumn;
    
    @FXML
    private TableColumn<Accrual, String> typeColumn;
    
    @FXML
    private TableColumn<Accrual, String> amountColumn;
    
    @FXML
    private TableColumn<Accrual, String> monthColumn;
    
    @FXML
    private TableColumn<Accrual, String> statusColumn;
    
    @FXML
    private Label resultLabel;
    
    private ScholarshipCalculationService calculationService;
    private ObservableList<Accrual> accrualsList;
    
    @FXML
    public void initialize() {
        calculationService = new ScholarshipCalculationService();
        accrualsList = FXCollections.observableArrayList();

        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getId().toString()));
        
        studentColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudent().getFio()));
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType().getName()));
        
        amountColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.2f", cellData.getValue().getAmount())));
        
        monthColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getForMonth().format(DateTimeFormatter.ofPattern("MM.yyyy"))));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : ""));
        
        accrualsTable.setItems(accrualsList);
        monthPicker.setValue(LocalDate.now());
    }

    @FXML
    private void onCalculateClick() {
        LocalDate month = monthPicker.getValue();
        if (month == null) {
            resultLabel.setText("Выберите месяц");
            return;
        }

        List<Accrual> created = calculationService.calculateScholarships(month);

        loadAccrualsGrouped(month);

        resultLabel.setText("Расчёт выполнен успешно! Создано начислений: " + created.size());
        resultLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onRefreshClick() {
        LocalDate month = monthPicker.getValue();
        if (month == null) {
            return;
        }

        loadAccrualsGrouped(month);
        
        resultLabel.setText("Обновлено. Всего: " + accrualsList.size());
        resultLabel.setStyle("-fx-text-fill: blue;");
    }
    
    private void loadAccrualsGrouped(LocalDate month) {
        accrualsList.clear();
        List<Accrual> all = calculationService.getAccrualsForMonth(month);
        
        Map<Integer, List<Accrual>> studentMap = new LinkedHashMap<>();
        for (Accrual accrual : all) {
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
    }
}
