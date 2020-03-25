package fr.themsou.document.render.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class ExportWindow {

    Stage window = new Stage();
    List<File> files;

    public static boolean erase = false;

    public ExportWindow(List<File> files){

        this.files = files;

        VBox root = new VBox();
        Scene scene = new Scene(root, 650, 450);

        window.initOwner(Main.window);
        window.initModality(Modality.WINDOW_MODAL);
        window.getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        window.setWidth(650);
        window.setHeight(450);

        window.setMinWidth(500);
        window.setMinHeight(450);
        window.setMaxWidth(800);
        window.setMaxHeight(450);
        window.setTitle("PDF Teacher - Exporter (" + files.size() + " documents)");
        window.setScene(scene);
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(javafx.stage.WindowEvent e){ window.close(); }
        });
        new JMetro(root, Style.LIGHT);

        if(files.size() == 1){
            setupSimplePanel(root);
        }else{
            setupComplexPanel(root);
        }

        window.show();
    }

    public void setupSimplePanel(VBox root){

        Text info = new Text("Vous allez exporter un document pour former un nouveau fichier PDF.");

        HBox name = new HBox();
            TextField fileName = new TextField(files.get(0).getName());
            fileName.setPromptText("Nom du document");
            fileName.setMinWidth(1);
            HBox.setHgrow(fileName, Priority.ALWAYS);
            fileName.setMinHeight(30);
        name.getChildren().addAll(fileName);

        HBox path = new HBox();
            HBox filePathPane = new HBox();
                TextField filePath = new TextField(files.get(0).getParentFile().getPath() + File.separator);
                filePath.setPromptText("Chemin du dossier d'exportation");
                filePath.setMinWidth(1);
                filePath.setMinHeight(30);
                HBox.setHgrow(filePath, Priority.ALWAYS);
            HBox.setHgrow(filePathPane, Priority.SOMETIMES);
            filePathPane.getChildren().add(filePath);
            Button changePath = new Button("Parcourir");
        path.getChildren().addAll(filePathPane, changePath);

        HBox types = new HBox();
            CheckBox textElements = new CheckBox("Texte");
            textElements.setSelected(true);
            CheckBox notesElements = new CheckBox("Notes");
            notesElements.setSelected(true);
            CheckBox drawElements = new CheckBox("Dessins");
            drawElements.setSelected(true);
        types.getChildren().addAll(textElements, notesElements, drawElements);


        VBox settings = new VBox();
            CheckBox erase = new CheckBox("Toujours écraser");
            CheckBox folders = new CheckBox("Créer les dossiers manquants");
            folders.setSelected(true);
            CheckBox delEdit = new CheckBox("Supprimer les éditions aprés le rendu");
        settings.getChildren().addAll(erase, folders, delEdit);

        HBox btns = new HBox();
            Button export = new Button("Exporter");
            export.requestFocus();
            Button cancel = new Button("Annuler");
        btns.getChildren().addAll(cancel, export);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(info, name, path, types, settings, btns);

        VBox.setMargin(info, new Insets(40, 10, 40, 10));

        HBox.setMargin(fileName, new Insets(0, 10, 0, 10));
        HBox.setMargin(filePathPane, new Insets(5, 5, 0, 10));
        HBox.setMargin(changePath, new Insets(5, 10, 0, 5));

        HBox.setMargin(textElements, new Insets(20, 10, 0, 10));
        HBox.setMargin(notesElements, new Insets(20, 10, 0, 10));
        HBox.setMargin(drawElements, new Insets(20, 10, 0, 10));

        VBox.setMargin(erase, new Insets(20, 10, 5, 10));
        VBox.setMargin(folders, new Insets(0, 10, 5, 10));
        VBox.setMargin(delEdit, new Insets(0, 10, 0, 10));

        HBox.setMargin(cancel, new Insets(50, 5, 10, 10));
        HBox.setMargin(export, new Insets(50, 10, 10, 5));

        changePath.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {

                final DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Selexionnez un dossier");
                chooser.setInitialDirectory(files.get(0).getParentFile());

                File file = chooser.showDialog(Main.window);
                if(file != null){
                    filePath.setText(file.getAbsolutePath() + File.separator);
                }
            }
        });

        export.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {

                if(!fileName.getText().endsWith(".pdf")) fileName.setText(fileName.getText() + ".pdf");

                startExportation(new File(filePath.getText()), "", "", "", "", fileName.getText(),
                        erase.isSelected(), folders.isSelected(), delEdit.isSelected(), textElements.isSelected(), notesElements.isSelected(), drawElements.isSelected());
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>(){
            @Override public void handle(ActionEvent event){
                window.close();
            }
        });

    }
    public void setupComplexPanel(VBox root){

        Text info = new Text("Vous allez exporter un document pour former un nouveau fichier PDF.");

        HBox name = new HBox();

            TextField prefix = new TextField();
            prefix.setPromptText("Préfixe");
            prefix.setMinWidth(1);
            //prefix.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(prefix, Priority.ALWAYS);
            prefix.setMinHeight(30);

            TextField fileName = new TextField("Nom du document");
            fileName.setDisable(true);
            fileName.setAlignment(Pos.CENTER);
            fileName.setMinHeight(30);

            TextField suffix = new TextField();
            suffix.setPromptText("Suffixe");
            suffix.setMinWidth(1);
            HBox.setHgrow(suffix, Priority.ALWAYS);
            suffix.setMinHeight(30);

        name.getChildren().addAll(prefix, fileName, suffix);

        HBox replace = new HBox();

            Label replaceText = new Label("Remplacer");
            replaceText.setFont(new Font(14));

            TextField replaceInput = new TextField();
            replaceInput.setMinWidth(1);
            HBox.setHgrow(replaceInput, Priority.ALWAYS);
            replaceInput.setMinHeight(30);

            Label byText = new Label("par");
            byText.setFont(new Font(14));

            TextField byInput = new TextField();
            byInput.setMinWidth(1);
            HBox.setHgrow(byInput, Priority.ALWAYS);
            byInput.setMinHeight(30);

        replace.getChildren().addAll(replaceText, replaceInput, byText, byInput);

        HBox path = new HBox();
        HBox filePathPane = new HBox();
        TextField filePath = new TextField(files.get(0).getParentFile().getPath() + File.separator);
        filePath.setMinWidth(1);
        filePath.setMinHeight(30);
        HBox.setHgrow(filePath, Priority.ALWAYS);
        HBox.setHgrow(filePathPane, Priority.SOMETIMES);
        filePathPane.getChildren().add(filePath);
        Button changePath = new Button("Parcourir");
        path.getChildren().addAll(filePathPane, changePath);

        HBox types = new HBox();
        CheckBox textElements = new CheckBox("Texte");
        textElements.setSelected(true);
        CheckBox notesElements = new CheckBox("Notes");
        notesElements.setSelected(true);
        CheckBox drawElements = new CheckBox("Dessins");
        drawElements.setSelected(true);
        types.getChildren().addAll(textElements, notesElements, drawElements);


        VBox settings = new VBox();
        CheckBox erase = new CheckBox("Toujours écraser");
        CheckBox folders = new CheckBox("Créer les dossiers manquants");
        folders.setSelected(true);
        CheckBox delEdit = new CheckBox("Supprimer les éditions aprés le rendu");
        settings.getChildren().addAll(erase, folders, delEdit);

        HBox btns = new HBox();
        Button export = new Button("Exporter");
        export.requestFocus();
        Button cancel = new Button("Annuler");
        btns.getChildren().addAll(cancel, export);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(info, name, replace, path, types, settings, btns);

        VBox.setMargin(info, new Insets(40, 10, 40, 10));

        HBox.setMargin(fileName, new Insets(0, 0, 0, 0));
        HBox.setMargin(prefix, new Insets(0, 0, 0, 10));
        HBox.setMargin(suffix, new Insets(0, 10, 0, 0));

        HBox.setMargin(replaceText, new Insets(10, 5, 0, 10));
        HBox.setMargin(replaceInput, new Insets(5, 0, 0, 0));
        HBox.setMargin(byText, new Insets(10, 5, 0, 5));
        HBox.setMargin(byInput, new Insets(5, 10, 0, 0));

        HBox.setMargin(filePathPane, new Insets(5, 5, 0, 10));
        HBox.setMargin(changePath, new Insets(5, 10, 0, 5));

        HBox.setMargin(textElements, new Insets(20, 10, 0, 10));
        HBox.setMargin(notesElements, new Insets(20, 10, 0, 10));
        HBox.setMargin(drawElements, new Insets(20, 10, 0, 10));

        VBox.setMargin(erase, new Insets(20, 10, 5, 10));
        VBox.setMargin(folders, new Insets(0, 10, 5, 10));
        VBox.setMargin(delEdit, new Insets(0, 10, 0, 10));

        HBox.setMargin(cancel, new Insets(50, 5, 10, 10));
        HBox.setMargin(export, new Insets(50, 10, 10, 5));

        changePath.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {

                final DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Selexionnez un dossier");
                chooser.setInitialDirectory(files.get(0).getParentFile());

                File file = chooser.showDialog(Main.window);
                if(file != null){
                    filePath.setText(file.getAbsolutePath() + File.separator);
                }
            }
        });

        export.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {

                startExportation(new File(filePath.getText()), prefix.getText(), suffix.getText(), replaceInput.getText(), byInput.getText(), "",
                        erase.isSelected(), folders.isSelected(), delEdit.isSelected(), textElements.isSelected(), notesElements.isSelected(), drawElements.isSelected());
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>(){
            @Override public void handle(ActionEvent event){
                window.close();
            }
        });

    }

    public void startExportation(File directory, String prefix, String suffix, String replace, String by, String customName,
                                 boolean eraseFile, boolean mkdirs, boolean deleteEdit, boolean textElements, boolean notesElements, boolean drawElements){
        erase = eraseFile;

        for(File file : files){
            try{
                int result = new ExportRenderer().exportFile(file, directory.getPath(), prefix, suffix, replace, by, customName, erase, mkdirs, textElements, notesElements, drawElements);
                if(result == 0){
                    return;
                }

                if(deleteEdit){
                    Edition.getEditFile(file).delete();
                }

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
                }
            }
        }
        window.close();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        new JMetro(alert.getDialogPane(), Style.LIGHT);
        Builders.secureAlert(alert);
        alert.setTitle("Exportation terminée");
        alert.setHeaderText("Vos documents ont bien été exportés !");
        alert.setContentText("Vous pouvez les retrouver dans le dossier choisi.");
        alert.show();

    }

}
