package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilidades.AlertaUtil;

import java.io.IOException;

public class ModuloPrincipalProfesionalController {

    @FXML
    private AnchorPane panelContenidoProfesional;

    @FXML
    private void cargarSubModulo(javafx.event.ActionEvent event) {

        try {

            javafx.scene.control.Button boton =
                    (javafx.scene.control.Button) event.getSource();

            String ruta = (String) boton.getUserData();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(ruta)
            );

            Node vista = loader.load();

            panelContenidoProfesional.getChildren().clear();
            panelContenidoProfesional.getChildren().add(vista);

            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // PRODUCTIVIDAD
    // ============================================================
    @FXML
    private void verProductividad() {

        try {
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/interface/ProductividadProfesional.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Productividad por profesional");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    null,
                    "No se pudo abrir la consulta de productividad."
            );
        }
    }

}
