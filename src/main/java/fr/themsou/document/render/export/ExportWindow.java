package fr.themsou.document.render.export;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ExportWindow {

    public void export(List<File> files){

        Stage window = new Stage();

        Pane root = new Pane();
        Scene scene = new Scene(root, 650, 365);

        window.initOwner(Main.window);
        window.initModality(Modality.WINDOW_MODAL);
        window.setWidth(650);
        window.setHeight(365);
        window.setTitle("PDF Teacher - Exporter");
        window.setScene(scene);
        window.setResizable(false);
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(javafx.stage.WindowEvent e){ window.close(); }
        });
        new JMetro(root, Style.LIGHT);

        Button okButton = new Button("Exporter");
        root.getChildren().add(okButton);

        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                for(File file : files){
                    try{
                        new ExportRenderer().exportFile(file);
                    }catch(Exception e){
                        e.printStackTrace();

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        new JMetro(alert.getDialogPane(), Style.LIGHT);
                        Builders.secureAlert(alert);
                        alert.setTitle("Erreur de rendu");
                        alert.setHeaderText("Une erreur de rendu s'est produite avec le document : " + file.getName());
                        alert.setContentText("Choisissez une action.");

                        TextArea textArea = new TextArea(e.getMessage());
                        textArea.setEditable(false);
                        textArea.setWrapText(true);
                        GridPane expContent = new GridPane();
                        expContent.setMaxWidth(Double.MAX_VALUE);
                        expContent.add(new Label("L'erreur survenue est la suivante :"), 0, 0);
                        expContent.add(textArea, 0, 1);
                        alert.getDialogPane().setExpandableContent(expContent);

                        ButtonType stopAll = new ButtonType("Arreter tout", ButtonBar.ButtonData.CANCEL_CLOSE);
                        ButtonType continueRender = new ButtonType("Continuer", ButtonBar.ButtonData.NEXT_FORWARD);
                        alert.getButtonTypes().setAll(stopAll, continueRender);

                        Optional<ButtonType> option = alert.showAndWait();
                        if(option.get() == stopAll){
                            window.close();
                            return;
                        }// else continue;
                    }
                }
                window.close();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(alert);
                alert.setTitle("Exportation terminée");
                alert.setHeaderText("Vos documents ont bien étés exportés !");
                alert.setContentText("Vous pouvez les retrouver dans le dossier choisit.");
                alert.show();
            }
        });

        window.show();
    }

}
