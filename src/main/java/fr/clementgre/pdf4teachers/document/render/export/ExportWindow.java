package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialog.AlreadyExistDialog;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExportWindow {

    private final Stage window = new Stage();
    private final List<File> files;

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
        Main.window.centerWindowIntoMe(window);
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
            CheckBox delEdit = new CheckBox(TR.tr("Supprimer les éditions aprés exportation"));
        settings.getChildren().addAll(delEdit);

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

        VBox.setMargin(delEdit, new Insets(20, 10, 0, 10));

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
                    false, delEdit.isSelected(), textElements.isSelected(),  gradesElements.isSelected(), drawElements.isSelected());
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
        CheckBox onlyEdited = new CheckBox(TR.tr("Exporter uniquement les documents édités"));
        onlyEdited.setSelected(true);
        CheckBox delEdit = new CheckBox(TR.tr("Supprimer les éditions aprés exportation"));
        settings.getChildren().addAll(onlyEdited, delEdit);

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

        VBox.setMargin(onlyEdited, new Insets(20, 10, 5, 10));
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
                onlyEdited.isSelected(), delEdit.isSelected(), textElements.isSelected(), gradesElements.isSelected(), drawElements.isSelected()));
        cancel.setOnAction(event -> window.close());

    }

    public void startExportation(File directory, String prefix, String suffix, String replaceText, String replaceByText, String customName,
                                 boolean onlyEdited, boolean deleteEdit, boolean textElements, boolean gradesElements, boolean drawElements){

        directory.mkdirs();

        AlreadyExistDialog alreadyExistDialog = new AlreadyExistDialog(customName.isEmpty());
        new TwoStepListAction<>(true, customName.isEmpty(), new TwoStepListInterface<File, Map.Entry<File, File>>() {
            @Override
            public List<File> prepare(boolean recursive){
                return files;
            }

            @Override
            public Map.Entry<Map.Entry<File, File>, Integer> sortData(File pdfFile, boolean recursive) {

                if(onlyEdited){ // Check only edited export
                    if(!Edition.getEditFile(pdfFile).exists()){
                        return Map.entry(Map.entry(new File(""), new File("")), 1);
                    }
                }

                String fileName = pdfFile.getName();
                if(recursive){

                    fileName = StringUtils.removeAfterLastRegexIgnoringCase(fileName, ".pdf");
                    fileName = fileName.replace(replaceText, replaceByText);
                    fileName = prefix + fileName + suffix + ".pdf";
                }else{
                    fileName = StringUtils.removeAfterLastRegexIgnoringCase(customName, ".pdf") + ".pdf";
                }

                File toFile = new File(directory.getAbsolutePath() + File.separator + fileName);

                if(toFile.exists()){ // Check Already Exist
                    AlreadyExistDialog.ResultType result = alreadyExistDialog.showAndWait(toFile);
                    if(result == AlreadyExistDialog.ResultType.SKIP) return Map.entry(Map.entry(new File(""), new File("")), 2);
                    else if(result == AlreadyExistDialog.ResultType.STOP) return Map.entry(Map.entry(new File(""), new File("")), TwoStepListAction.CODE_STOP);
                    else if(result == AlreadyExistDialog.ResultType.RENAME) toFile = AlreadyExistDialog.rename(toFile);
                }

                return Map.entry(Map.entry(pdfFile, toFile), TwoStepListAction.CODE_OK);
            }

            @Override
            public String getSortedDataName(Map.Entry<File, File> data, boolean recursive) {
                return data.getKey().getName();
            }

            @Override
            public TwoStepListAction.ProcessResult completeData(Map.Entry<File, File> data, boolean recursive) {
                try{
                    new ExportRenderer().exportFile(data.getKey(), data.getValue(), textElements, gradesElements, drawElements);
                    if(deleteEdit){
                        Platform.runLater(() -> {
                            if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(data.getKey().getAbsolutePath())){
                                MainWindow.mainScreen.document.edition.clearEdit(false);
                            }else{
                                Edition.getEditFile(data.getKey()).delete();
                            }
                        });
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    if(PlatformUtils.runAndWait(() -> DialogBuilder.showErrorAlert(TR.tr("Une erreur d'exportation s'est produite avec le document :") + " " + data.getKey().getName(), e.getMessage(), recursive))){
                        return TwoStepListAction.ProcessResult.STOP;
                    }
                    if(!recursive){
                        window.close();
                        return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                    }
                    return TwoStepListAction.ProcessResult.SKIPPED;
                }
                return TwoStepListAction.ProcessResult.OK;
            }

            @Override
            public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive){
                window.close();
                if(deleteEdit) MainWindow.filesTab.refresh();

                Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation terminée"));
                ButtonType open = new ButtonType(TR.tr("Ouvrir le dossier"), ButtonBar.ButtonData.YES);
                alert.getButtonTypes().add(open);

                if(completedSize == 0) alert.setHeaderText(TR.tr("Aucun document n'a été exporté !"));
                else if(completedSize == 1) alert.setHeaderText(TR.tr("Le document a bien été exporté !"));
                else alert.setHeaderText(completedSize + " " + TR.tr("documents ont été exportés !"));

                String noEditText = !excludedReasons.containsKey(1) ? "" : "\n(" + excludedReasons.get(1) + " " + TR.tr("documents ignorés car ils n'avaient pas d'édition") + ")";
                String alreadyExistText = !excludedReasons.containsKey(2) ? "" : "\n(" + excludedReasons.get(2) + " " + TR.tr("documents ignorés car ils existaient déjà") + ")";
                alert.setContentText(completedSize + "/" + originSize + " " + TR.tr("documents exportées") + noEditText + alreadyExistText);

                Optional<ButtonType> optionSelected = alert.showAndWait();
                if(optionSelected.get() == open){
                    PlatformUtils.openDirectory(directory.getAbsolutePath());
                }
            }
        });
    }
}
