package ru.demo.demo2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;

public class MainMenuController {
    
    @FXML
    private BorderPane mainContainer;
    
    @FXML
    private void onCalculationModuleClick() {
        loadView("/ru/demo/demo2/calculation-view.fxml");
    }
    
    @FXML
    private void onPayrollModuleClick() {
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
