package ru.demo.demo2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.demo.demo2.model.Ground;
import ru.demo.demo2.model.ScholarshipType;
import ru.demo.demo2.model.Student;
import ru.demo.demo2.repository.GroundDao;
import ru.demo.demo2.repository.ScholarshipTypeDao;
import ru.demo.demo2.repository.StudentDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GroundsController {
    
    @FXML
    private TableView<Ground> groundsTable;
    
    @FXML
    private TableColumn<Ground, String> idColumn;
    
    @FXML
    private TableColumn<Ground, String> studentColumn;
    
    @FXML
    private TableColumn<Ground, String> typeColumn;
    
    @FXML
    private TableColumn<Ground, String> docTypeColumn;
    
    @FXML
    private TableColumn<Ground, String> issueDateColumn;
    
    @FXML
    private TableColumn<Ground, String> validUntilColumn;
    
    private GroundDao groundDao;
    private StudentDao studentDao;
    private ScholarshipTypeDao typeDao;
    private ObservableList<Ground> groundsList;
    
    @FXML
    public void initialize() {
        groundDao = new GroundDao();
        studentDao = new StudentDao();
        typeDao = new ScholarshipTypeDao();
        groundsList = FXCollections.observableArrayList();
        
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getId().toString()));
        
        studentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudent().getFio()));
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType().getName()));
        
        docTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDocType()));
        
        issueDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getIssueDate().toString()));
        
        validUntilColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getValidUntil() != null 
                ? cellData.getValue().getValidUntil().toString() : ""));
        
        groundsTable.setItems(groundsList);
        loadGrounds();
    }
    
    @FXML
    private void onAddClick() {
        showEditDialog(null);
    }
    
    @FXML
    private void onEditClick() {
        Ground selected = groundsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите основание для редактирования");
            return;
        }
        showEditDialog(selected);
    }
    
    @FXML
    private void onDeleteClick() {
        Ground selected = groundsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите основание для удаления");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить основание?");
        confirm.setContentText("Вы уверены, что хотите удалить это основание?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            groundDao.delete(selected);
            loadGrounds();
        }
    }
    
    @FXML
    private void onRefreshClick() {
        loadGrounds();
    }
    
    private void loadGrounds() {
        groundsList.clear();
        List<Ground> grounds = groundDao.findAll();
        groundsList.addAll(grounds);
    }
    
    private void showEditDialog(Ground ground) {
        Dialog<Ground> dialog = new Dialog<>();
        dialog.setTitle(ground == null ? "Добавить основание" : "Редактировать основание");
        dialog.setHeaderText(null);
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        ComboBox<Student> studentCombo = new ComboBox<>();
        studentCombo.setItems(FXCollections.observableArrayList(studentDao.findAll()));
        studentCombo.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student != null ? student.getFio() : "";
            }
            @Override
            public Student fromString(String string) {
                return null;
            }
        });
        if (ground != null) studentCombo.setValue(ground.getStudent());
        
        ComboBox<ScholarshipType> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(typeDao.findAll()));
        typeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ScholarshipType type) {
                return type != null ? type.getName() : "";
            }

            @Override
            public ScholarshipType fromString(String string) {
                return null;
            }
        });
        if (ground != null) typeCombo.setValue(ground.getType());
        
        TextField docTypeField = new TextField();
        docTypeField.setPromptText("Тип документа");
        if (ground != null) docTypeField.setText(ground.getDocType());
        
        DatePicker issueDatePicker = new DatePicker();
        if (ground != null) issueDatePicker.setValue(ground.getIssueDate());
        
        DatePicker validUntilPicker = new DatePicker();
        if (ground != null && ground.getValidUntil() != null) validUntilPicker.setValue(ground.getValidUntil());
        
        grid.add(new Label("Студент:"), 0, 0);
        grid.add(studentCombo, 1, 0);
        grid.add(new Label("Тип стипендии:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Тип документа:"), 0, 2);
        grid.add(docTypeField, 1, 2);
        grid.add(new Label("Дата выдачи:"), 0, 3);
        grid.add(issueDatePicker, 1, 3);
        grid.add(new Label("Действителен до:"), 0, 4);
        grid.add(validUntilPicker, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Ground g = ground != null ? ground : new Ground();
                    g.setStudent(studentCombo.getValue());
                    g.setType(typeCombo.getValue());
                    g.setDocType(docTypeField.getText());
                    g.setIssueDate(issueDatePicker.getValue());
                    g.setValidUntil(validUntilPicker.getValue());
                    return g;
                } catch (Exception e) {
                    showAlert("Ошибка: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<Ground> result = dialog.showAndWait();
        result.ifPresent(g -> {
            if (ground == null) {
                groundDao.save(g);
            } else {
                groundDao.update(g);
            }
            loadGrounds();
        });
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
