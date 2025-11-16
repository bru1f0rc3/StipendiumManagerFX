package ru.demo.demo2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.demo.demo2.model.Accrual;
import ru.demo.demo2.service.ScholarshipCalculationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        accrualsList.clear();
        List<Accrual> all = calculationService.getAccrualsForMonth(month);
        accrualsList.addAll(all);

        resultLabel.setText("Расчёт выполнен успешно! Создано начислений: " + created.size());
        resultLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onRefreshClick() {
        LocalDate month = monthPicker.getValue();
        if (month == null) {
            return;
        }

        accrualsList.clear();
        List<Accrual> all = calculationService.getAccrualsForMonth(month);
        accrualsList.addAll(all);
        
        resultLabel.setText("Обновлено. Всего: " + all.size());
        resultLabel.setStyle("-fx-text-fill: blue;");
    }
}
