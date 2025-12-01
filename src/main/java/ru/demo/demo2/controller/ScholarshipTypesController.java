package ru.demo.demo2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.demo.demo2.model.ScholarshipType;
import ru.demo.demo2.repository.ScholarshipTypeDao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ScholarshipTypesController {
    
    @FXML
    private TableView<ScholarshipType> typesTable;
    
    @FXML
    private TableColumn<ScholarshipType, String> idColumn;
    
    @FXML
    private TableColumn<ScholarshipType, String> nameColumn;
    
    @FXML
    private TableColumn<ScholarshipType, String> baseAmountColumn;
    
    @FXML
    private TableColumn<ScholarshipType, String> requiresDocsColumn;
    
    private ScholarshipTypeDao typeDao;
    private ObservableList<ScholarshipType> typesList;
    
    @FXML
    public void initialize() {
        typeDao = new ScholarshipTypeDao();
        typesList = FXCollections.observableArrayList();
        
        idColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getId().toString()));
        nameColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        baseAmountColumn.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getBaseAmount())));
        requiresDocsColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRequiresDocs() ? "Да" : "Нет"));
        
        typesTable.setItems(typesList);
        loadTypes();
    }
    
    @FXML
    private void onAddClick() { showEditDialog(null); }
    
    @FXML
    private void onEditClick() {
        ScholarshipType t = typesTable.getSelectionModel().getSelectedItem();
        if (t == null) { showAlert("Выберите тип стипендии для редактирования"); return; }
        showEditDialog(t);
    }
    
    @FXML
    private void onDeleteClick() {
        ScholarshipType t = typesTable.getSelectionModel().getSelectedItem();
        if (t == null) { showAlert("Выберите тип стипендии для удаления"); return; }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Удалить " + t.getName() + "?");
        if (confirm.showAndWait().get() == ButtonType.OK) { typeDao.delete(t); loadTypes(); }
    }
    
    @FXML
    private void onRefreshClick() { loadTypes(); }
    
    private void loadTypes() {
        typesList.clear();
        typesList.addAll(typeDao.findAll());
    }
    
    private void showEditDialog(ScholarshipType type) {
        Dialog<ScholarshipType> dialog = new Dialog<>();
        dialog.setTitle(type == null ? "Добавить тип стипендии" : "Редактировать тип стипендии");
        dialog.setHeaderText(null);
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Название");
        if (type != null) nameField.setText(type.getName());
        
        TextField baseAmountField = new TextField();
        baseAmountField.setPromptText("Базовая сумма");
        if (type != null) baseAmountField.setText(type.getBaseAmount().toString());
        
        CheckBox requiresDocsCheck = new CheckBox();
        if (type != null) requiresDocsCheck.setSelected(type.getRequiresDocs());
        
        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Базовая сумма:"), 0, 1);
        grid.add(baseAmountField, 1, 1);
        grid.add(new Label("Требуются документы:"), 0, 2);
        grid.add(requiresDocsCheck, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    ScholarshipType t = type != null ? type : new ScholarshipType();
                    t.setName(nameField.getText());
                    t.setBaseAmount(new BigDecimal(baseAmountField.getText()));
                    t.setRequiresDocs(requiresDocsCheck.isSelected());
                    return t;
                } catch (Exception e) {
                    showAlert("Ошибка: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<ScholarshipType> result = dialog.showAndWait();
        result.ifPresent(t -> {
            if (type == null) {
                typeDao.save(t);
            } else {
                typeDao.update(t);
            }
            loadTypes();
        });
    }
    
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setContentText(msg);
        a.showAndWait();
    }
}
