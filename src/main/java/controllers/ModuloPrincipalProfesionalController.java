package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

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
}
