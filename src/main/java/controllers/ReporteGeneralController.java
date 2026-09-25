package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import utilidades.AlertaUtil;
import claseslogicas.ExportadorExcel;
import javafx.stage.FileChooser;
import java.io.File;
public class ReporteGeneralController {
    private final ExportadorExcel exportadorExcel = new ExportadorExcel();

    @FXML
    public  void exportarTodoEnExcel() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar datos para Power BI");
        fileChooser.setInitialFileName("PeluqPro_Datos_PowerBI.xlsx");

        FileChooser.ExtensionFilter filtro =
                new FileChooser.ExtensionFilter("Archivo Excel (*.xlsx)", "*.xlsx");

        fileChooser.getExtensionFilters().add(filtro);

        File archivo = fileChooser.showSaveDialog(null);

        if (archivo == null) {
            return;
        }

        try {
            exportadorExcel.exportarTodo(archivo);

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Exportación completada",
                    null,
                    "Los datos fueron exportados correctamente."
            );

        } catch (Exception e) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de exportación",
                    null,
                    "No se pudieron exportar los datos: " + e.getMessage()
            );
        }
    }
}