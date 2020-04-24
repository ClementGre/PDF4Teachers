package fr.themsou.panel.leftBar.notes;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.TR;
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

public class NoteExportWindow extends Stage {

    TabPane tabPane = new TabPane();

    Tab exportAllTab = new Tab(TR.tr("Tout exporter ensemble"));
    Tab exportAllSplitTab = new Tab(TR.tr("Tout exporter séparément"));
    Tab exportThisTab = new Tab(TR.tr("Exporter uniquement ce fichier"));

    VBox exportAll = new VBox();
    VBox exportAllSplit = new VBox();
    VBox exportThis = new VBox();

    public NoteExportWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setWidth(650);
        setHeight(470);
        setResizable(false);
        setTitle(TR.tr("PDF4Teachers - Exporter les notes"));
        setScene(scene);
        new JMetro(root, Style.LIGHT);

        Text info = new Text(TR.tr("Exporter les notes dans un tableau CSV"));
        VBox.setMargin(info, new Insets(40, 0, 40, 10));

        tabPane.getTabs().addAll(exportAllTab, exportAllSplitTab, exportThisTab);

        root.getChildren().addAll(info, tabPane);

        setupExportAllPanel();
        setupExportAllSplitPanel();
        setupExportThisPanel();

        show();
    }

    public void setupExportAllPanel(){
        exportAllTab.setClosable(false);
        exportAllTab.setContent(exportAll);
        exportAll.setStyle("-fx-padding: 10;");

        HBox name = new HBox();

        TextField prefix = new TextField();
        prefix.setPromptText(TR.tr("Préfixe"));
        prefix.setMinWidth(1);
        //prefix.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(prefix, Priority.ALWAYS);
        prefix.setMinHeight(30);

        TextField fileName = new TextField(TR.tr("Nom du document"));
        fileName.setDisable(true);
        fileName.setAlignment(Pos.CENTER);
        fileName.setMinHeight(30);

        TextField suffix = new TextField();
        suffix.setPromptText(TR.tr("Suffixe"));
        suffix.setMinWidth(1);
        HBox.setHgrow(suffix, Priority.ALWAYS);
        suffix.setMinHeight(30);

        name.getChildren().addAll(prefix, fileName, suffix);

        HBox replace = new HBox();

        Label replaceText = new Label(TR.tr("Remplacer"));
        replaceText.setFont(new Font(14));

        TextField replaceInput = new TextField();
        replaceInput.setMinWidth(1);
        HBox.setHgrow(replaceInput, Priority.ALWAYS);
        replaceInput.setMinHeight(30);

        Label byText = new Label(TR.tr("par"));
        byText.setFont(new Font(14));

        TextField byInput = new TextField();
        byInput.setMinWidth(1);
        HBox.setHgrow(byInput, Priority.ALWAYS);
        byInput.setMinHeight(30);

        replace.getChildren().addAll(replaceText, replaceInput, byText, byInput);

        HBox path = new HBox();
        HBox filePathPane = new HBox();
        TextField filePath = new TextField((UserData.lastExportDirNotes.exists() ? UserData.lastExportDirNotes.getAbsolutePath() : System.getProperty("user.home") + File.separator));
        filePath.setMinWidth(1);
        filePath.setMinHeight(30);
        HBox.setHgrow(filePath, Priority.ALWAYS);
        HBox.setHgrow(filePathPane, Priority.SOMETIMES);
        filePathPane.getChildren().add(filePath);
        Button changePath = new Button(TR.tr("Parcourir"));
        path.getChildren().addAll(filePathPane, changePath);

        VBox settings = new VBox();
        CheckBox onlyRatingScaled = new CheckBox(TR.tr("Exporter uniquement les documents avec un barème."));
        CheckBox onlyCompleted = new CheckBox(TR.tr("Exporter uniquement les documents avec toutes les notes remplies."));

        HBox tiersExport = new HBox();
        Label tiersExportLabel = new Label(TR.tr("Niveaux de note exportés"));
        Slider tiersExportSlider = new Slider(1, 5, 2);
        tiersExport.getChildren().addAll(tiersExportLabel, tiersExportSlider);

        settings.getChildren().addAll(onlyRatingScaled, onlyCompleted, tiersExport);

        HBox.setMargin(fileName, new Insets(0, 0, 0, 0));
        HBox.setMargin(prefix, new Insets(0, 0, 0, 10));
        HBox.setMargin(suffix, new Insets(0, 10, 0, 0));

        HBox.setMargin(replaceText, new Insets(10, 5, 0, 10));
        HBox.setMargin(replaceInput, new Insets(5, 0, 0, 0));
        HBox.setMargin(byText, new Insets(10, 5, 0, 5));
        HBox.setMargin(byInput, new Insets(5, 10, 0, 0));

        HBox.setMargin(filePathPane, new Insets(5, 5, 0, 10));
        HBox.setMargin(changePath, new Insets(5, 10, 0, 5));

        VBox.setMargin(onlyRatingScaled, new Insets(20, 10, 5, 10));
        VBox.setMargin(onlyCompleted, new Insets(0, 10, 5, 10));
        VBox.setMargin(tiersExport, new Insets(0, 10, 5, 10));
        HBox.setMargin(tiersExportLabel, new Insets(3, 5, 0, 0));

        exportAll.getChildren().addAll(name, replace, path, settings);

        changePath.setOnAction(event -> {

            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Selexionnez un dossier"));
            chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) :
                    (UserData.lastExportDirNotes.exists() ? UserData.lastExportDirNotes : new File(System.getProperty("user.home")))));

            File file = chooser.showDialog(Main.window);
            if(file != null){
                filePath.setText(file.getAbsolutePath() + File.separator);
            }
        });


    }
    public void setupExportAllSplitPanel(){
        exportAllSplitTab.setClosable(false);
        exportAllSplitTab.setContent(exportAllSplit);
        exportAllSplit.setStyle("-fx-padding: 10;");

        HBox name = new HBox();

        TextField prefix = new TextField();
        prefix.setPromptText(TR.tr("Préfixe"));
        prefix.setMinWidth(1);
        //prefix.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(prefix, Priority.ALWAYS);
        prefix.setMinHeight(30);

        TextField fileName = new TextField(TR.tr("Nom du document"));
        fileName.setDisable(true);
        fileName.setAlignment(Pos.CENTER);
        fileName.setMinHeight(30);

        TextField suffix = new TextField();
        suffix.setPromptText(TR.tr("Suffixe"));
        suffix.setMinWidth(1);
        HBox.setHgrow(suffix, Priority.ALWAYS);
        suffix.setMinHeight(30);

        name.getChildren().addAll(prefix, fileName, suffix);

        HBox replace = new HBox();

        Label replaceText = new Label(TR.tr("Remplacer"));
        replaceText.setFont(new Font(14));

        TextField replaceInput = new TextField();
        replaceInput.setMinWidth(1);
        HBox.setHgrow(replaceInput, Priority.ALWAYS);
        replaceInput.setMinHeight(30);

        Label byText = new Label(TR.tr("par"));
        byText.setFont(new Font(14));

        TextField byInput = new TextField();
        byInput.setMinWidth(1);
        HBox.setHgrow(byInput, Priority.ALWAYS);
        byInput.setMinHeight(30);

        replace.getChildren().addAll(replaceText, replaceInput, byText, byInput);

        HBox path = new HBox();
        HBox filePathPane = new HBox();
        TextField filePath = new TextField((UserData.lastExportDirNotes.exists() ? UserData.lastExportDirNotes.getAbsolutePath() : System.getProperty("user.home") + File.separator));
        filePath.setMinWidth(1);
        filePath.setMinHeight(30);
        HBox.setHgrow(filePath, Priority.ALWAYS);
        HBox.setHgrow(filePathPane, Priority.SOMETIMES);
        filePathPane.getChildren().add(filePath);
        Button changePath = new Button(TR.tr("Parcourir"));
        path.getChildren().addAll(filePathPane, changePath);

        VBox settings = new VBox();
        CheckBox onlyRatingScaled = new CheckBox(TR.tr("Exporter uniquement les documents avec un barème."));
        CheckBox onlyCompleted = new CheckBox(TR.tr("Exporter uniquement les documents avec toutes les notes remplies."));
        CheckBox withTxtElements = new CheckBox(TR.tr("Ajouter les commentaires dans le fichier"));

        HBox tiersExport = new HBox();
        Label tiersExportLabel = new Label(TR.tr("Niveaux de note exportés"));
        Slider tiersExportSlider = new Slider(1, 5, 2);
        tiersExport.getChildren().addAll(tiersExportLabel, tiersExportSlider);

        settings.getChildren().addAll(onlyRatingScaled, onlyCompleted, withTxtElements, tiersExport);

        HBox.setMargin(fileName, new Insets(0, 0, 0, 0));
        HBox.setMargin(prefix, new Insets(0, 0, 0, 10));
        HBox.setMargin(suffix, new Insets(0, 10, 0, 0));

        HBox.setMargin(replaceText, new Insets(10, 5, 0, 10));
        HBox.setMargin(replaceInput, new Insets(5, 0, 0, 0));
        HBox.setMargin(byText, new Insets(10, 5, 0, 5));
        HBox.setMargin(byInput, new Insets(5, 10, 0, 0));

        HBox.setMargin(filePathPane, new Insets(5, 5, 0, 10));
        HBox.setMargin(changePath, new Insets(5, 10, 0, 5));

        VBox.setMargin(onlyRatingScaled, new Insets(20, 10, 5, 10));
        VBox.setMargin(onlyCompleted, new Insets(0, 10, 5, 10));
        VBox.setMargin(withTxtElements, new Insets(0, 10, 5, 10));
        VBox.setMargin(tiersExport, new Insets(0, 10, 5, 10));
        HBox.setMargin(tiersExportLabel, new Insets(3, 5, 0, 0));

        exportAllSplit.getChildren().addAll(name, replace, path, settings);

        changePath.setOnAction(event -> {

            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Selexionnez un dossier"));
            chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) :
                    (UserData.lastExportDirNotes.exists() ? UserData.lastExportDirNotes : new File(System.getProperty("user.home")))));

            File file = chooser.showDialog(Main.window);
            if(file != null){
                filePath.setText(file.getAbsolutePath() + File.separator);
            }
        });

    }
    public void setupExportThisPanel(){
        exportThisTab.setClosable(false);
        exportThisTab.setContent(exportThis);
        exportThis.setStyle("-fx-padding: 10;");

        HBox name = new HBox();
        TextField fileName = new TextField(Main.mainScreen.document.getFileName());
        fileName.setPromptText(TR.tr("Nom du document"));
        fileName.setMinWidth(1);
        HBox.setHgrow(fileName, Priority.ALWAYS);
        fileName.setMinHeight(30);
        name.getChildren().addAll(fileName);

        HBox path = new HBox();
        HBox filePathPane = new HBox();
        TextField filePath = new TextField((UserData.lastExportDirNotes.exists() ? UserData.lastExportDirNotes.getAbsolutePath() : System.getProperty("user.home") + File.separator));
        filePath.setPromptText(TR.tr("Chemin du dossier d'exportation"));
        filePath.setMinWidth(1);
        filePath.setMinHeight(30);
        HBox.setHgrow(filePath, Priority.ALWAYS);
        HBox.setHgrow(filePathPane, Priority.SOMETIMES);
        filePathPane.getChildren().add(filePath);
        Button changePath = new Button(TR.tr("Parcourir"));
        path.getChildren().addAll(filePathPane, changePath);

        VBox settings = new VBox();
        CheckBox withTxtElements = new CheckBox(TR.tr("Ajouter les commentaires dans le fichier"));
        HBox tiersExport = new HBox();
        Label tiersExportLabel = new Label(TR.tr("Niveaux de note exportés"));
        Slider tiersExportSlider = new Slider(1, 5, 2);
        tiersExport.getChildren().addAll(tiersExportLabel, tiersExportSlider);

        settings.getChildren().addAll(withTxtElements, tiersExport);

        HBox.setMargin(fileName, new Insets(0, 10, 0, 10));
        HBox.setMargin(filePathPane, new Insets(5, 5, 0, 10));
        HBox.setMargin(changePath, new Insets(5, 10, 0, 5));

        VBox.setMargin(withTxtElements, new Insets(20, 10, 5, 10));
        VBox.setMargin(tiersExport, new Insets(0, 10, 5, 10));
        HBox.setMargin(tiersExportLabel, new Insets(3, 5, 0, 0));

        exportThis.getChildren().addAll(name, path, settings);

        changePath.setOnAction(event -> {

            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Selexionnez un dossier"));

            chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) :
                    (UserData.lastExportDirNotes.exists() ? UserData.lastExportDirNotes : new File(System.getProperty("user.home")))));

            File file = chooser.showDialog(Main.window);
            if(file != null){
                filePath.setText(file.getAbsolutePath() + File.separator);
            }
        });

    }
}
