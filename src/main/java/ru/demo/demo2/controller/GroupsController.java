package ru.demo.demo2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.demo.demo2.model.StudentGroup;
import ru.demo.demo2.repository.StudentGroupDao;

import java.util.List;
import java.util.Optional;

public class GroupsController {
    
    @FXML
    private TableView<StudentGroup> groupsTable;
    
    @FXML
    private TableColumn<StudentGroup, String> idColumn;
    
    @FXML
    private TableColumn<StudentGroup, String> groupCodeColumn;
    
    @FXML
    private TableColumn<StudentGroup, String> courseColumn;
    
    @FXML
    private TableColumn<StudentGroup, String> facultyColumn;
    
    private StudentGroupDao groupDao;
    private ObservableList<StudentGroup> groupsList;
    
    @FXML
    public void initialize() {
        groupDao = new StudentGroupDao();
        groupsList = FXCollections.observableArrayList();
        
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getId().toString()));
        
        groupCodeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getGroupCode()));
        
        courseColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCourse().toString()));
        
        facultyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFaculty()));
        
        groupsTable.setItems(groupsList);
        loadGroups();
    }
    
    @FXML
    private void onAddClick() {
        showEditDialog(null);
    }
    
    @FXML
    private void onEditClick() {
        StudentGroup selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите группу для редактирования");
            return;
        }
        showEditDialog(selected);
    }
    
    @FXML
    private void onDeleteClick() {
        StudentGroup selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите группу для удаления");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить группу?");
        confirm.setContentText("Вы уверены, что хотите удалить группу " + selected.getGroupCode() + "?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            groupDao.delete(selected);
            loadGroups();
        }
    }
    
    @FXML
    private void onRefreshClick() {
        loadGroups();
    }
    
    private void loadGroups() {
        groupsList.clear();
        List<StudentGroup> groups = groupDao.findAll();
        groupsList.addAll(groups);
    }
    
    private void showEditDialog(StudentGroup group) {
        Dialog<StudentGroup> dialog = new Dialog<>();
        dialog.setTitle(group == null ? "Добавить группу" : "Редактировать группу");
        dialog.setHeaderText(null);
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField groupCodeField = new TextField();
        groupCodeField.setPromptText("Код группы");
        if (group != null) groupCodeField.setText(group.getGroupCode());
        
        TextField courseField = new TextField();
        courseField.setPromptText("Курс");
        if (group != null) courseField.setText(group.getCourse().toString());
        
        TextField facultyField = new TextField();
        facultyField.setPromptText("Факультет");
        if (group != null) facultyField.setText(group.getFaculty());
        
        grid.add(new Label("Код группы:"), 0, 0);
        grid.add(groupCodeField, 1, 0);
        grid.add(new Label("Курс:"), 0, 1);
        grid.add(courseField, 1, 1);
        grid.add(new Label("Факультет:"), 0, 2);
        grid.add(facultyField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    StudentGroup g = group != null ? group : new StudentGroup();
                    g.setGroupCode(groupCodeField.getText());
                    g.setCourse(Integer.parseInt(courseField.getText()));
                    g.setFaculty(facultyField.getText());
                    return g;
                } catch (Exception e) {
                    showAlert("Ошибка: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<StudentGroup> result = dialog.showAndWait();
        result.ifPresent(g -> {
            if (group == null) {
                groupDao.save(g);
            } else {
                groupDao.update(g);
            }
            loadGroups();
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
