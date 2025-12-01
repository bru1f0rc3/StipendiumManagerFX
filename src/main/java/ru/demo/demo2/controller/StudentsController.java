package ru.demo.demo2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.demo.demo2.model.Student;
import ru.demo.demo2.model.StudentGroup;
import ru.demo.demo2.repository.StudentDao;
import ru.demo.demo2.repository.StudentGroupDao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class StudentsController {
    
    @FXML
    private TableView<Student> studentsTable;
    
    @FXML
    private TableColumn<Student, String> idColumn;
    
    @FXML
    private TableColumn<Student, String> fioColumn;
    
    @FXML
    private TableColumn<Student, String> groupColumn;
    
    @FXML
    private TableColumn<Student, String> avgGradeColumn;
    
    @FXML
    private TableColumn<Student, String> socialStatusColumn;
    
    private StudentDao studentDao;
    private StudentGroupDao groupDao;
    private ObservableList<Student> studentsList;
    
    @FXML
    public void initialize() {
        studentDao = new StudentDao();
        groupDao = new StudentGroupDao();
        studentsList = FXCollections.observableArrayList();
        
        idColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getId().toString()));
        fioColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFio()));
        groupColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getGroupCode()));
        avgGradeColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAvgGrade().toString()));
        socialStatusColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getHasSocialStatus() ? "Да" : "Нет"));
        
        studentsTable.setItems(studentsList);
        loadStudents();
    }
    
    @FXML
    private void onAddClick() { showEditDialog(null); }
    
    @FXML
    private void onEditClick() {
        Student s = studentsTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("Выберите студента для редактирования"); return; }
        showEditDialog(s);
    }
    
    @FXML
    private void onDeleteClick() {
        Student s = studentsTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("Выберите студента для удаления"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setContentText("Удалить " + s.getFio() + "?");
        if (confirm.showAndWait().get() == ButtonType.OK) { studentDao.delete(s); loadStudents(); }
    }
    
    @FXML
    private void onRefreshClick() { loadStudents(); }
    
    private void loadStudents() {
        studentsList.clear();
        studentsList.addAll(studentDao.findAll());
    }
    
    private void showEditDialog(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Добавить студента" : "Редактировать студента");
        dialog.setHeaderText(null);
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField fioField = new TextField();
        fioField.setPromptText("ФИО");
        if (student != null) fioField.setText(student.getFio());
        
        ComboBox<StudentGroup> groupCombo = new ComboBox<>();
        groupCombo.setItems(FXCollections.observableArrayList(groupDao.findAll()));
        if (student != null) groupCombo.setValue(student.getGroup());
        
        TextField avgGradeField = new TextField();
        avgGradeField.setPromptText("Средний балл");
        if (student != null) avgGradeField.setText(student.getAvgGrade().toString());
        
        CheckBox socialStatusCheck = new CheckBox();
        if (student != null) socialStatusCheck.setSelected(student.getHasSocialStatus());
        
        grid.add(new Label("ФИО:"), 0, 0);
        grid.add(fioField, 1, 0);
        grid.add(new Label("Группа:"), 0, 1);
        grid.add(groupCombo, 1, 1);
        grid.add(new Label("Средний балл:"), 0, 2);
        grid.add(avgGradeField, 1, 2);
        grid.add(new Label("Социальный статус:"), 0, 3);
        grid.add(socialStatusCheck, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Student s = student != null ? student : new Student();
                    s.setFio(fioField.getText());
                    s.setGroup(groupCombo.getValue());
                    s.setAvgGrade(new BigDecimal(avgGradeField.getText()));
                    s.setHasSocialStatus(socialStatusCheck.isSelected());
                    return s;
                } catch (Exception e) {
                    showAlert("Ошибка: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if (student == null) {
                studentDao.save(s);
            } else {
                studentDao.update(s);
            }
            loadStudents();
        });
    }
    
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setContentText(msg);
        a.showAndWait();
    }
}
