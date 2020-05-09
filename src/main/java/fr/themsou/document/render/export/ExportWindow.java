package fr.themsou.document.render.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
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
        Scene scene = new Scene(root, 650, 470);

        window.initOwner(Main.window);
        window.initModality(Modality.WINDOW_MODAL);
        window.getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        window.setWidth(650);
        window.setHeight(470);

        window.setMinWidth(500);
        window.setMinHeight(470);
        window.setMaxWidth(800);
        window.setMaxHeight(470);
        window.setTitle("PDF4Teachers - " + TR.tr("Exporter") + " (" + files.size() + " " + TR.tr("documents)"));
        window.setScene(scene);
        window.setOnCloseRequest(e -> window.close());
        new JMetro(root, Style.LIGHT);

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
            CheckBox notesElements = new CheckBox(TR.tr("Notes"));
            notesElements.setSelected(true);
            CheckBox drawElements = new CheckBox(TR.tr("Dessins"));
            drawElements.setSelected(true);
        types.getChildren().addAll(textElements, notesElements, drawElements);


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
        HBox.setMargin(notesElements, new Insets(20, 10, 0, 10));
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
                    erase.isSelected(), folders.isSelected(), false, delEdit.isSelected(), textElements.isSelected(),  notesElements.isSelected(), drawElements.isSelected());
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
            replaceText.setFont(new Font(14));

            TextField replaceInput = new TextField(MainWindow.userData.lastExportFileNameReplace);
            replaceInput.setMinWidth(1);
            HBox.setHgrow(replaceInput, Priority.ALWAYS);
            replaceInput.setMinHeight(30);
            replaceInput.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameReplace = newValue);

            Label byText = new Label(TR.tr("par"));
            byText.setFont(new Font(14));

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
        CheckBox notesElements = new CheckBox(TR.tr("Notes"));
        notesElements.setSelected(true);
        CheckBox drawElements = new CheckBox(TR.tr("Dessins"));
        drawElements.setSelected(true);
        types.getChildren().addAll(textElements, notesElements, drawElements);


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
        HBox.setMargin(notesElements, new Insets(20, 10, 0, 10));
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
                erase.isSelected(), folders.isSelected(), onlyEdited.isSelected(), delEdit.isSelected(), textElements.isSelected(), notesElements.isSelected(), drawElements.isSelected()));
        cancel.setOnAction(event -> window.close());

    }

    public void startExportation(File directory, String prefix, String suffix, String replace, String by, String customName,
                                 boolean eraseFile, boolean mkdirs, boolean onlyEdited, boolean deleteEdit, boolean textElements, boolean notesElements, boolean drawElements){
        erase = eraseFile;
        int exported = 0;

        for(File file : files){
            try{
                int result = new ExportRenderer().exportFile(file, directory.getPath(), prefix, suffix, replace, by, customName, erase, mkdirs, onlyEdited, textElements, notesElements, drawElements);
                if(result == 0){
                    return;
                }else if(result == 1){
                    exported++;
                }

                if(deleteEdit){
                    Edition.getEditFile(file).delete();
                }

            }catch(Exception e){
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(alert);
                alert.setTitle(TR.tr("Erreur d'exportation"));
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
                    return;
                }
            }
        }
        window.close();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        new JMetro(alert.getDialogPane(), Style.LIGHT);
        Builders.secureAlert(alert);
        alert.setTitle(TR.tr("Exportation terminée"));

        if(exported == 0) alert.setHeaderText(TR.tr("Aucun document n'a été exporté !"));
        else if(exported == 1) alert.setHeaderText(TR.tr("Le document a bien été exporté !"));
        else alert.setHeaderText(exported + " " + TR.tr("documents ont été exportés !"));

        alert.setContentText(TR.tr("Les documents exportés se trouvent dans le dossier choisi"));
        alert.show();

    }

}
