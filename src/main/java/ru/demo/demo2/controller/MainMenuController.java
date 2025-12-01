package ru.demo.demo2.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.util.Duration;

public class MainMenuController {
    
    @FXML
    private BorderPane mainContainer;
    
    @FXML
    private VBox drawer;
    
    private boolean isMenuOpen = false;
    
    @FXML
    public void initialize() {
        loadView("/ru/demo/demo2/calculation-view.fxml");
    }
    
    @FXML
    public void toggleMenu() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }
    
    private void openMenu() {
        isMenuOpen = true;
        drawer.setManaged(true);
        drawer.toFront();
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), drawer);
        slideIn.setToX(0);
        slideIn.play();
    }
    
    private void closeMenu() {
        isMenuOpen = false;
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), drawer);
        slideOut.setToX(-200);
        slideOut.setOnFinished(e -> drawer.setManaged(false));
        slideOut.play();
    }
    
    @FXML
    private void onCalculationModuleClick() {
        closeMenu();
        loadView("/ru/demo/demo2/calculation-view.fxml");
    }
    
    @FXML
    private void onPayrollModuleClick() {
        closeMenu();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/demo/demo2/payroll-view.fxml"));
            Node view = loader.load();
            
            PayrollController controller = loader.getController();
            if (controller != null) {
                controller.refresh();
            }
            
            mainContainer.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onStudentsClick() {
        closeMenu();
        loadView("/ru/demo/demo2/students-view.fxml");
    }
    
    @FXML
    private void onGroupsClick() {
        closeMenu();
        loadView("/ru/demo/demo2/groups-view.fxml");
    }
    
    @FXML
    private void onScholarshipTypesClick() {
        closeMenu();
        loadView("/ru/demo/demo2/scholarship-types-view.fxml");
    }
    
    @FXML
    private void onGroundsClick() {
        closeMenu();
        loadView("/ru/demo/demo2/grounds-view.fxml");
    }
    
    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node view = loader.load();
            mainContainer.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
