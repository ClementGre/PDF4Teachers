package fr.themsou.document.render.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.PlatformTools;
import fr.themsou.utils.TR;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
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
        Scene scene = new Scene(root);

        window.initOwner(Main.window);
        window.initModality(Modality.WINDOW_MODAL);
        window.getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        window.setWidth(650);

        window.setMinWidth(500);
        window.setMaxWidth(800);
        window.setTitle("PDF4Teachers - " + TR.tr("Exporter") + " (" + files.size() + " " + TR.tr("documents)"));
        window.setScene(scene);
        window.setOnCloseRequest(e -> window.close());
        StyleManager.putStyle(root, Style.DEFAULT);

        if(files.size() == 1){
            setupSimplePanel(root);
        }else{
            setupComplexPanel(root);
        }

        window.show();
    }

    public void setupSimplePanel(VBox root){

        Text info = new Text(TR.tr("Vous allez exporter un document pour former un nouveau fichier PDF."));

        HBox name = new HBox();
            TextField fileName = new TextField(files.get(0).getName());
            fileName.setPromptText(TR.tr("Nom du document"));
            fileName.setMinWidth(1);
            HBox.setHgrow(fileName, Priority.ALWAYS);
            fileName.setMinHeight(30);
        name.getChildren().addAll(fileName);

        HBox path = new HBox();
            HBox filePathPane = new HBox();
                TextField filePath = new TextField(files.get(0).getParentFile().getPath() + File.separator);
                filePath.setPromptText(TR.tr("Chemin du dossier d'exportation"));
                filePath.setMinWidth(1);
                filePath.setMinHeight(30);
                HBox.setHgrow(filePath, Priority.ALWAYS);
            HBox.setHgrow(filePathPane, Priority.SOMETIMES);
            filePathPane.getChildren().add(filePath);
            Button changePath = new Button(TR.tr("Parcourir"));
        path.getChildren().addAll(filePathPane, changePath);

        HBox types = new HBox();
            CheckBox textElements = new CheckBox(TR.tr("Texte"));
            textElements.setSelected(true);
            CheckBox gradesElements = new CheckBox(TR.tr("Notes"));
            gradesElements.setSelected(true);
            CheckBox drawElements = new CheckBox(TR.tr("Dessins"));
            drawElements.setSelected(true);
        types.getChildren().addAll(textElements, gradesElements, drawElements);


        VBox settings = new VBox();
            CheckBox erase = new CheckBox(TR.tr("Toujours écraser"));
            CheckBox folders = new CheckBox(TR.tr("Créer les dossiers manquants"));
            folders.setSelected(true);
            CheckBox delEdit = new CheckBox(TR.tr("Supprimer les éditions aprés exportation"));
        settings.getChildren().addAll(erase, folders, delEdit);

        HBox btns = new HBox();
            Button export = new Button(TR.tr("Exporter"));
            export.requestFocus();
            Button cancel = new Button(TR.tr("Annuler"));
        btns.getChildren().addAll(cancel, export);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(info, name, path, types, settings, btns);

        VBox.setMargin(info, new Insets(40, 10, 40, 10));

        HBox.setMargin(fileName, new Insets(0, 10, 0, 10));
        HBox.setMargin(filePathPane, new Insets(5, 5, 0, 10));
        HBox.setMargin(changePath, new Insets(5, 10, 0, 5));

        HBox.setMargin(textElements, new Insets(20, 10, 0, 10));
        HBox.setMargin(gradesElements, new Insets(20, 10, 0, 10));
        HBox.setMargin(drawElements, new Insets(20, 10, 0, 10));

        VBox.setMargin(erase, new Insets(20, 10, 5, 10));
        VBox.setMargin(folders, new Insets(0, 10, 5, 10));
        VBox.setMargin(delEdit, new Insets(0, 10, 0, 10));

        HBox.setMargin(cancel, new Insets(50, 5, 10, 10));
        HBox.setMargin(export, new Insets(50, 10, 10, 5));

        changePath.setOnAction(event -> {

            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Sélectionner un dossier"));
            chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) : new File(files.get(0).getParentFile().getPath())));

            File file = chooser.showDialog(Main.window);
            if(file != null){
                filePath.setText(file.getAbsolutePath() + File.separator);
            }
        });

        export.setOnAction(event -> {

            if(!fileName.getText().endsWith(".pdf")) fileName.setText(fileName.getText() + ".pdf");

            startExportation(new File(filePath.getText()), "", "", "", "", fileName.getText(),
                    erase.isSelected(), folders.isSelected(), false, delEdit.isSelected(), textElements.isSelected(),  gradesElements.isSelected(), drawElements.isSelected());
        });
        cancel.setOnAction(event -> window.close());

    }
    public void setupComplexPanel(VBox root){

        Text info = new Text(TR.tr("Vous allez exporter un document pour former un nouveau fichier PDF."));

        HBox name = new HBox();

            TextField prefix = new TextField(MainWindow.userData.lastExportFileNamePrefix);
            prefix.setPromptText(TR.tr("Préfixe"));
            prefix.setMinWidth(1);
            //prefix.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(prefix, Priority.ALWAYS);
            prefix.setMinHeight(30);
            prefix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNamePrefix = newValue);

            TextField fileName = new TextField(TR.tr("Nom du document"));
            fileName.setDisable(true);
            fileName.setAlignment(Pos.CENTER);
            fileName.setMinHeight(30);

            TextField suffix = new TextField(MainWindow.userData.lastExportFileNameSuffix);
            suffix.setPromptText(TR.tr("Suffixe"));
            suffix.setMinWidth(1);
            HBox.setHgrow(suffix, Priority.ALWAYS);
            suffix.setMinHeight(30);
            suffix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameSuffix = newValue);

        name.getChildren().addAll(prefix, fileName, suffix);

        HBox replace = new HBox();

            Label replaceText = new Label(TR.tr("Remplacer"));

            TextField replaceInput = new TextField(MainWindow.userData.lastExportFileNameReplace);
            replaceInput.setMinWidth(1);
            HBox.setHgrow(replaceInput, Priority.ALWAYS);
            replaceInput.setMinHeight(30);
            replaceInput.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameReplace = newValue);

            Label byText = new Label(TR.tr("par"));

            TextField byInput = new TextField(MainWindow.userData.lastExportFileNameBy);
            byInput.setMinWidth(1);
            HBox.setHgrow(byInput, Priority.ALWAYS);
            byInput.setMinHeight(30);
            byInput.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameBy = newValue);

        replace.getChildren().addAll(replaceText, replaceInput, byText, byInput);

        HBox path = new HBox();
        HBox filePathPane = new HBox();
        TextField filePath = new TextField(files.get(0).getParentFile().getPath() + File.separator);
        filePath.setMinWidth(1);
        filePath.setMinHeight(30);
        HBox.setHgrow(filePath, Priority.ALWAYS);
        HBox.setHgrow(filePathPane, Priority.SOMETIMES);
        filePathPane.getChildren().add(filePath);
        Button changePath = new Button(TR.tr("Parcourir"));
        path.getChildren().addAll(filePathPane, changePath);

        HBox types = new HBox();
        CheckBox textElements = new CheckBox(TR.tr("Texte"));
        textElements.setSelected(true);
        CheckBox gradesElements = new CheckBox(TR.tr("Notes"));
        gradesElements.setSelected(true);
        CheckBox drawElements = new CheckBox(TR.tr("Dessins"));
        drawElements.setSelected(true);
        types.getChildren().addAll(textElements, gradesElements, drawElements);


        VBox settings = new VBox();
        CheckBox erase = new CheckBox(TR.tr("Toujours écraser"));
        CheckBox folders = new CheckBox(TR.tr("Créer les dossiers manquants"));
        folders.setSelected(true);
        CheckBox onlyEdited = new CheckBox(TR.tr("Exporter uniquement les documents édités"));
        onlyEdited.setSelected(true);
        CheckBox delEdit = new CheckBox(TR.tr("Supprimer les éditions aprés exportation"));
        settings.getChildren().addAll(erase, folders, onlyEdited, delEdit);

        HBox btns = new HBox();
        Button export = new Button(TR.tr("Exporter"));
        export.requestFocus();
        Button cancel = new Button(TR.tr("Annuler"));
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
        HBox.setMargin(gradesElements, new Insets(20, 10, 0, 10));
        HBox.setMargin(drawElements, new Insets(20, 10, 0, 10));

        VBox.setMargin(erase, new Insets(20, 10, 5, 10));
        VBox.setMargin(folders, new Insets(0, 10, 5, 10));
        VBox.setMargin(onlyEdited, new Insets(0, 10, 5, 10));
        VBox.setMargin(delEdit, new Insets(0, 10, 0, 10));

        HBox.setMargin(cancel, new Insets(50, 5, 10, 10));
        HBox.setMargin(export, new Insets(50, 10, 10, 5));

        changePath.setOnAction(event -> {

            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Sélectionner un dossier"));
            chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) : new File(files.get(0).getParentFile().getPath())));

            File file = chooser.showDialog(Main.window);
            if(file != null){
                filePath.setText(file.getAbsolutePath() + File.separator);
            }
        });

        export.setOnAction(event -> startExportation(new File(filePath.getText()), prefix.getText(), suffix.getText(), replaceInput.getText(), byInput.getText(), "",
                erase.isSelected(), folders.isSelected(), onlyEdited.isSelected(), delEdit.isSelected(), textElements.isSelected(), gradesElements.isSelected(), drawElements.isSelected()));
        cancel.setOnAction(event -> window.close());

    }

    Alert loadingAlert;
    private int exported;
    private int total;
    public void startExportation(File directory, String prefix, String suffix, String replace, String by, String customName,
                                 boolean eraseFile, boolean mkdirs, boolean onlyEdited, boolean deleteEdit, boolean textElements, boolean gradesElements, boolean drawElements){

        erase = eraseFile;
        loadingAlert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation..."));
        exported = 0;
        total = 0;

        // Wait Dialog

        loadingAlert.setWidth(600);
        loadingAlert.setHeaderText(TR.tr("PDF4Teachers génère vos documents..."));

        VBox pane = new VBox();
        Label currentDocument = new Label();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setMinHeight(10);
        VBox.setMargin(loadingBar, new Insets(10, 0, 0,0));
        pane.getChildren().addAll(currentDocument, loadingBar);
        loadingAlert.getDialogPane().setContent(pane);
        loadingAlert.show();

        // Export Thread

        new Thread(() -> {
            for(File file : files){

                // Update Wait dialog
                Platform.runLater(() -> {
                    currentDocument.setText(file.getName() + "(" + total + "/" + files.size() + ")");
                    loadingBar.setProgress(total/((float)files.size()));
                });
                total++;


                try{
                    // Export the file
                    int result = new ExportRenderer().exportFile(file, directory.getPath(), prefix, suffix, replace, by, customName, erase, mkdirs, onlyEdited, textElements, gradesElements, drawElements);
                    // Exportation canceled, return
                    if(result == 0){ Platform.runLater(() -> loadingAlert.close()); return; }
                    // GOOD !
                    else if(result == 1) exported++;
                    // Delete edition option
                    if(deleteEdit) Edition.getEditFile(file).delete();
                }catch(Exception e){
                    e.printStackTrace();

                    // Error dialog
                    if(PlatformTools.runAndWait(() -> {
                        Alert alert = Builders.getAlert(Alert.AlertType.ERROR, TR.tr("Erreur d'exportation"));
                        alert.setHeaderText(TR.tr("Une erreur d'exportation s'est produite avec le document :") + " " + file.getName());
                        alert.setContentText(TR.tr("Choisissez une action."));

                        TextArea textArea = new TextArea(e.getMessage());
                        textArea.setEditable(false);
                        textArea.setWrapText(true);
                        GridPane expContent = new GridPane();
                        expContent.setMaxWidth(Double.MAX_VALUE);
                        expContent.add(new Label(TR.tr("L'erreur survenue est la suivante :")), 0, 0);
                        expContent.add(textArea, 0, 1);
                        alert.getDialogPane().setExpandableContent(expContent);

                        ButtonType stopAll = new ButtonType(TR.tr("Arreter tout"), ButtonBar.ButtonData.CANCEL_CLOSE);
                        ButtonType continueRender = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.NEXT_FORWARD);
                        alert.getButtonTypes().setAll(stopAll, continueRender);

                        Optional<ButtonType> option = alert.showAndWait();
                        if(option.get() == stopAll){
                            window.close();
                            return true; // Return true to ask to cancel exportation
                        }
                        return false;
                    })){
                        // If callback return true (cancel exportation), return
                        Platform.runLater(() -> loadingAlert.close());
                        return;
                    }
                }
            }
            // Open the end window
            Platform.runLater(this::endExportations);
            return;
        }, "exportation").start();
    }
    private void endExportations(){
        loadingAlert.close();
        window.close();

        Alert alert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation terminée"));

        if(exported == 0) alert.setHeaderText(TR.tr("Aucun document n'a été exporté !"));
        else if(exported == 1) alert.setHeaderText(TR.tr("Le document a bien été exporté !"));
        else alert.setHeaderText(exported + " " + TR.tr("documents ont été exportés !"));

        alert.setContentText(TR.tr("Les documents exportés se trouvent dans le dossier choisi"));
        alert.show();
        return;
    }
}
