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

        idColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getId().toString()));
        studentColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStudent().getFio()));
        typeColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getType().getName()));
        amountColumn.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getAmount())));
        monthColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getForMonth().format(DateTimeFormatter.ofPattern("MM.yyyy"))));
        statusColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus() != null ? cd.getValue().getStatus().toString() : ""));
        
        accrualsTable.setItems(accrualsList);
        monthPicker.setValue(LocalDate.now());
    }

    @FXML
    private void onCalculateClick() {
        LocalDate month = monthPicker.getValue();
        if (month == null) { resultLabel.setText("Выберите месяц"); return; }
        
        calculationService.calculateScholarships(month);
        loadAccrualsGrouped(month);
        resultLabel.setText("Расчёт выполнен успешно!");
        resultLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onRefreshClick() {
        LocalDate month = monthPicker.getValue();
        if (month != null) {
            loadAccrualsGrouped(month);
            resultLabel.setText("Обновлено. Всего: " + accrualsList.size());
            resultLabel.setStyle("-fx-text-fill: blue;");
        }
    }
    
    private void loadAccrualsGrouped(LocalDate month) {
        accrualsList.clear();
        Map<Integer, List<Accrual>> studentMap = new LinkedHashMap<>();
        for (Accrual a : calculationService.getAccrualsForMonth(month))
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
    }
}
