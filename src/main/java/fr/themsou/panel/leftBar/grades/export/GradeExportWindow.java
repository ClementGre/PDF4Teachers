package fr.themsou.panel.leftBar.grades.export;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
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

import java.io.File;

public class GradeExportWindow extends Stage {

    TabPane tabPane = new TabPane();

    ExportPane exportAllTab = new ExportPane(this, TR.tr("Tout exporter ensemble"), 0, false, true, true, false);
    ExportPane exportAllSplitTab = new ExportPane(this, TR.tr("Tout exporter séparément"), 1, true, true, true, true);
    ExportPane exportThisTab = new ExportPane(this, TR.tr("Exporter uniquement ce fichier"), 2, false, false, false, true);

    public GradeExportWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setWidth(650);
        setResizable(false);
        setTitle(TR.tr("PDF4Teachers - Exporter les notes"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);

        Text info = new Text(TR.tr("Exporter les notes des fichiers ouverts dans un tableau CSV") + "\n" + TR.tr("Lors de l'importation dans un tableur, la langue du fichier sera à régler sur Anglais"));
        VBox.setMargin(info, new Insets(40, 0, 40, 10));

        tabPane.getTabs().addAll(exportAllTab, exportAllSplitTab, exportThisTab);
        root.getChildren().addAll(info, tabPane);

        show();
    }

    class ExportPane extends Tab{

        public int type;
        GradeExportWindow window;

        boolean fileNameCustom, studentNameCustom, multipleFilesCustom, canExportTextElements;

        VBox root = new VBox();

        public TextField fileNameSimple, fileNamePrefix, fileNameSuffix, fileNameReplace, fileNameBy;
        public TextField studentNameSimple, studentNameReplace, studentNameBy;
        public TextField filePath;

        public CheckBox settingsOnlySameGradeScale = new CheckBox(TR.tr("Exporter uniquement les notes des documents avec le même barème"));
        public CheckBox settingsOnlyCompleted = new CheckBox(TR.tr("Exporter uniquement les notes des documents avec toutes les notes remplies"));
        public CheckBox settingsOnlySameDir = new CheckBox(TR.tr("Exporter uniquement les notes des documents du même dossier"));
        public CheckBox settingsAttributeTotalLine = new CheckBox(TR.tr("Ajouter une ligne pour le barème"));
        public CheckBox settingsAttributeMoyLine = new CheckBox(TR.tr("Ajouter une ligne pour la moyenne des notes"));
        public CheckBox settingsWithTxtElements = new CheckBox(TR.tr("Ajouter des lignes pour les commentaires (Éléments textuels)."));
        public Slider settingsTiersExportSlider = new Slider(1, 5, MainWindow.userData.settingsTiersExportSlider);


        public ExportPane(GradeExportWindow window, String tabName, int type, boolean fileNameCustom, boolean studentNameCustom, boolean multipleFilesCustom, boolean canExportTextElements){

            super(tabName);
            this.window = window;
            this.type = type;
            this.fileNameCustom = fileNameCustom;
            this.studentNameCustom = studentNameCustom;
            this.multipleFilesCustom = multipleFilesCustom;
            this.canExportTextElements = canExportTextElements;

            setClosable(false);
            setContent(root);
            root.setStyle("-fx-padding: 10;");

            setupFileNameForm();
            setupStudentNameForm();
            setupPathForm();
            setupSettingsForm();
            setupBtns();

        }

        public void setupFileNameForm(){

            VBox info = generateInfo(TR.tr("Nom du document") + " :", false);

            if(fileNameCustom){
                HBox fileNamePrefixSuffixBox = new HBox();
                HBox fileNameReplaceBox = new HBox();

                fileNamePrefix = new TextField(MainWindow.userData.lastExportFileNamePrefix);
                fileNamePrefix.setPromptText(TR.tr("Préfixe"));
                Builders.setHBoxPosition(fileNamePrefix, -1, 30, 0, 2.5);
                fileNamePrefix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNamePrefix = newValue);

                TextField fileName = new TextField(TR.tr("Nom du document"));
                fileName.setDisable(true); fileName.setAlignment(Pos.CENTER);
                Builders.setHBoxPosition(fileName, 0, 30, 0, 2.5);

                fileNameSuffix = new TextField(MainWindow.userData.lastExportFileNameSuffix);
                fileNameSuffix.setPromptText(TR.tr("Suffixe"));
                Builders.setHBoxPosition(fileNameSuffix, -1, 30, 0, 2.5);
                fileNameSuffix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameSuffix = newValue);

                fileNamePrefixSuffixBox.getChildren().addAll(fileNamePrefix, fileName, fileNameSuffix);


                Label replaceText = new Label(TR.tr("Remplacer"));
                Builders.setHBoxPosition(replaceText, 0, 30, 2.5);

                fileNameReplace = new TextField(MainWindow.userData.lastExportFileNameReplace);
                Builders.setHBoxPosition(fileNameReplace, -1, 30, 0, 2.5);
                fileNameReplace.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameReplace = newValue);

                Label byText = new Label(TR.tr("par"));
                Builders.setHBoxPosition(byText, 0, 30, 2.5);

                fileNameBy = new TextField(MainWindow.userData.lastExportFileNameBy);
                Builders.setHBoxPosition(fileNameBy, -1, 30, 0, 2.5);
                fileNameBy.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameBy = newValue);

                fileNameReplaceBox.getChildren().addAll(replaceText, fileNameReplace, byText, fileNameBy);

                root.getChildren().addAll(info, fileNamePrefixSuffixBox, fileNameReplaceBox);

            }else{

                fileNameSimple = new TextField(MainWindow.userData.lastExportFileName.isEmpty() || type == 2 ? StringUtils.removeAfterLastRejex(MainWindow.mainScreen.document.getFileName(), ".pdf") + ".csv" : MainWindow.userData.lastExportFileName);
                fileNameSimple.setPromptText(TR.tr("Nom du document"));
                Builders.setHBoxPosition(fileNameSimple, 0, 30, 0, 2.5);
                if(type != 2) fileNameSimple.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileName = newValue);
                root.getChildren().addAll(info, fileNameSimple);

            }

        }
        public void setupStudentNameForm(){

            VBox info = generateInfo(TR.tr("Nom de l'élève (Basé sur le nom du document)") + " :", true);

            if(studentNameCustom){
                HBox studentNameReplaceBox = new HBox();

                Label replaceText = new Label(TR.tr("Remplacer"));
                Builders.setHBoxPosition(replaceText, 0, 30, 2.5);

                studentNameReplace = new TextField(MainWindow.userData.lastExportStudentNameReplace);
                Builders.setHBoxPosition(studentNameReplace, -1, 30, 0, 2.5);
                studentNameReplace.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportStudentNameReplace = newValue);

                Label byText = new Label(TR.tr("par"));
                Builders.setHBoxPosition(byText, 0, 30, 2.5);

                studentNameBy = new TextField(MainWindow.userData.lastExportStudentNameBy);
                Builders.setHBoxPosition(studentNameBy, -1, 30, 0, 2.5);
                studentNameBy.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportStudentNameBy = newValue);

                studentNameReplaceBox.getChildren().addAll(replaceText, studentNameReplace, byText, studentNameBy);

                root.getChildren().addAll(info, studentNameReplaceBox);

            }else{

                studentNameSimple = new TextField(StringUtils.removeAfterLastRejex(MainWindow.mainScreen.document.getFileName(), ".pdf"));
                studentNameSimple.setPromptText(TR.tr("Nom de l'élève"));
                Builders.setHBoxPosition(studentNameSimple, 0, 30, 0, 2.5);

                root.getChildren().addAll(info, studentNameSimple);

            }

        }
        public void setupPathForm(){

            VBox info = generateInfo(TR.tr("Dossier d'exportation") + " :", true);

            HBox filePathBox = new HBox();

            filePath = new TextField(MainWindow.mainScreen.document.getFile().getParentFile().getPath() + File.separator);
            Builders.setHBoxPosition(filePath, -1, 30, 0, 2.5);

            Button changePath = new Button(TR.tr("Parcourir"));
            Builders.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));

            filePathBox.getChildren().addAll(filePath, changePath);

            root.getChildren().addAll(info, filePathBox);

            changePath.setOnAction(event -> {

                final DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle(TR.tr("Sélectionner un dossier"));
                chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) : new File(MainWindow.mainScreen.document.getFile().getParentFile().getPath() + File.separator)));

                File file = chooser.showDialog(Main.window);
                if(file != null) filePath.setText(file.getAbsolutePath() + File.separator);
            });

        }
        public void setupSettingsForm(){

            VBox info = generateInfo(TR.tr("Paramètres") + " :", true);

            HBox tiersExport = new HBox();
            Label tiersExportLabel = new Label(TR.tr("Niveaux de note exportés :"));
            tiersExport.getChildren().addAll(tiersExportLabel, settingsTiersExportSlider);
            settingsTiersExportSlider.valueProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsTiersExportSlider = newValue.intValue());

            settingsTiersExportSlider.setSnapToTicks(true);
            settingsTiersExportSlider.setMajorTickUnit(1);
            settingsTiersExportSlider.setMinorTickCount(0);

            Builders.setHBoxPosition(settingsOnlySameGradeScale, 0, 30, 0, 2.5);
            Builders.setHBoxPosition(settingsOnlyCompleted, 0, 30, 0, 2.5);
            Builders.setHBoxPosition(settingsOnlySameDir, 0, 30, 0, 2.5);
            Builders.setHBoxPosition(settingsAttributeTotalLine, 0, 30, 0, 2.5);
            Builders.setHBoxPosition(settingsAttributeMoyLine, 0, 30, 0, 2.5);
            Builders.setHBoxPosition(settingsWithTxtElements, 0, 30, 0, 2.5);
            Builders.setHBoxPosition(settingsTiersExportSlider, 0, 30, 0, 2.5);
            Builders.setHBoxPosition(tiersExportLabel, 0, 30, 2.5, 2.5);

            root.getChildren().add(info);

            settingsAttributeTotalLine.setSelected(MainWindow.userData.settingsAttributeTotalLine);
            if(multipleFilesCustom){
                settingsOnlySameGradeScale.setSelected(MainWindow.userData.settingsOnlySameGradeScale);
                settingsOnlyCompleted.setSelected(MainWindow.userData.settingsOnlyCompleted);
                settingsOnlySameDir.setSelected(MainWindow.userData.settingsOnlySameDir);
                settingsAttributeMoyLine.setSelected(MainWindow.userData.settingsAttributeMoyLine);
                root.getChildren().addAll(settingsOnlySameGradeScale, settingsOnlyCompleted, settingsOnlySameDir, settingsAttributeTotalLine, settingsAttributeMoyLine);
            }
            else root.getChildren().add(settingsAttributeTotalLine);

            if(canExportTextElements){
                settingsWithTxtElements.setSelected(MainWindow.userData.settingsWithTxtElements);
                root.getChildren().add(settingsWithTxtElements);
            }
            root.getChildren().add(tiersExport);

            settingsOnlySameGradeScale.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsOnlySameGradeScale = newValue);
            settingsOnlyCompleted.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsOnlyCompleted = newValue);
            settingsOnlySameDir.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsOnlySameDir = newValue);
            settingsAttributeTotalLine.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsAttributeTotalLine = newValue);
            settingsAttributeMoyLine.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsAttributeMoyLine = newValue);
            settingsWithTxtElements.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsWithTxtElements = newValue);

        }

        public void setupBtns(){

            HBox btnBox = new HBox();

            Button cancel = new Button(TR.tr("Annuler"));
            Button export = new Button(TR.tr("Exporter"));
            export.requestFocus();

            btnBox.getChildren().addAll(cancel, export);
            btnBox.setAlignment(Pos.CENTER_RIGHT);

            HBox.setMargin(cancel, new Insets(50, 5, 0, 10));
            HBox.setMargin(export, new Insets(50, 10, 0, 5));

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            export.setOnAction(event -> {
                end(new GradeExportRenderer(this).start());
            });
            cancel.setOnAction(event -> {
                window.close();
            });

            root.getChildren().addAll(spacer, btnBox);
        }

        public VBox generateInfo(String text, boolean topBar){

            VBox box = new VBox();

            if(topBar){
                Separator separator = new Separator();
                Builders.setVBoxPosition(separator, 0, 0, new Insets(5, -5, 0, -5));
                box.getChildren().add(separator);
            }

            Label info = new Label(text);
            Builders.setVBoxPosition(info, 0, 0, 2.5);
            box.getChildren().add(info);

            return box;
        }

        private void end(int exported){

            close();

            Alert alert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation terminée"));

            if(exported == 0) alert.setHeaderText(TR.tr("Aucun document n'a été exporté !"));
            else if(exported == 1) alert.setHeaderText(TR.tr("Le document a bien été exporté !"));
            else alert.setHeaderText(exported + " " + TR.tr("documents ont été exportés !"));

            if(exported != 0){
                if(type == 1) alert.setContentText(TR.tr("Vous pouvez retrouver les fichiers CSV dans le dossier choisi."));
                else alert.setContentText(TR.tr("Vous pouvez retrouver le fichier CSV dans le dossier choisi."));
            }

            alert.show();

        }

    }
}