package fr.themsou.document.render.convert;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.*;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConvertWindow extends Stage {

    public static ObservableList<String> definitions = FXCollections.observableArrayList(
            TR.tr("Adapter à l'image"),
            "0.501832Mp (A4 72 dpi)",
            "0.777600Mp (HD)",
            "0.967590Mp (A4 100dpi)",
            "2.073600Mp (Full HD)",
            "3.686400Mp (Quad HD)",
            "3.868706Mp (A4 200dpi)",
            "8.294400Mp (Ultra HD 4k)",
            "8.699840Mp (A4 300dpi)");

    public static ObservableList<String> formats = FXCollections.observableArrayList(
            TR.tr("Adapter à l'image"),
            "594:841 (A4 " + TR.tr("Portrait") + ")",
            "841:594 (A4 " + TR.tr("Paysage") + ")",
            "16:9",
            "9:16",
            "4:3",
            "3:4");

    DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    public TabPane tabPane = new TabPane();
    public ConvertPane convertDirs;
    public ConvertPane convertFiles;

    PDRectangle defaultSize;
    CallBack<ArrayList<ConvertedFile>> callBack;
    public ConvertWindow(PDRectangle defaultSize, CallBack<ArrayList<ConvertedFile>> callBack){
        this.defaultSize = defaultSize;
        this.callBack = callBack;

        df.setMaximumFractionDigits(340);

        VBox root = new VBox();
        Scene scene = new Scene(root);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setResizable(false);
        setTitle(TR.tr("PDF4Teachers - Convertir"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);

        // HEADER

        Text info;
        if(defaultSize == null) info = new Text(TR.tr("Convertir des images en documents PDF"));
        else{
            info = new Text(TR.tr("Convertir des images en pages de document PDF"));
            int gcd = GCD((int)defaultSize.getWidth(), (int)defaultSize.getHeight());
            int heightFactor = (int) (gcd == 0 ? defaultSize.getHeight() : defaultSize.getHeight()/gcd);
            int widthFactor = (int) (gcd == 0 ? defaultSize.getWidth() : defaultSize.getWidth()/gcd);
            //definitions.add(0, df.format(defaultSize.getWidth() * defaultSize.getHeight() / 1000000) + "Mp (" + TR.tr("Ce document") + ")");
            formats.add(1, widthFactor + ":" + heightFactor + " (" + TR.tr("Ce document") + ")");
        }

        VBox.setMargin(info, new Insets(40, 0, 40, 10));

        // PANES

        convertDirs = new ConvertPane(this, TR.tr("Convertir des dossiers en plusieurs documents"), true);
        convertFiles = new ConvertPane(this, defaultSize == null ? TR.tr("Convertir des fichiers en un document") : TR.tr("Convertir des fichiers en pages"), false);

        if(defaultSize == null) tabPane.getTabs().add(convertDirs);
        tabPane.getTabs().add(convertFiles);
        root.getChildren().addAll(info, tabPane);

        // SHOW

        show();
    }

    public class ConvertPane extends Tab {

        public boolean convertDirs;
        ConvertWindow window;

        boolean convertToExistingDoc;

        VBox root = new VBox();

        public TextArea srcFiles;
        public TextField srcDir, docName, outDir;
        ComboBox<String> definition, format;

        public CheckBox convertAloneFiles = new CheckBox(TR.tr("Convertir aussi les images du dossier source en documents (un document par image)"));
        public CheckBox convertVoidFiles = new CheckBox(TR.tr("Convertir les chemins de fichiers invalides (mauvais format ou non existant) en pages blanches"));

        public ConvertPane(ConvertWindow window, String tabName, boolean convertDirs){
            super(tabName);
            this.window = window;
            this.convertDirs = convertDirs;
            this.convertToExistingDoc = defaultSize != null;

            setClosable(false);
            setContent(root);
            root.setStyle("-fx-padding: 10;");

            setupDesc();
            setupSrcFilesForm();
            setupDocNameForm();
            setupOutDirForm();
            setupSizeForm();
            setupSettingsForm();
            setupBtns();

        }
        public void setupDesc(){

            Label desc = new Label();

            if(convertDirs){
                desc.setText(TR.tr("Convertir plusieurs dossiers en plusieurs documents PDF") + "\n   " +
                        TR.tr("Chaque dossier sera converti en un document, les images contenues dans le dossier représentent les pages") + "\n   " +
                        TR.tr("L'ordre des pages est pris en fonction de l'ordre alphabétique.") + "\n   " +
                        TR.tr("Il sera toujours possible de déplacer les pages après."));
            }else{
                if(defaultSize != null){
                    desc.setText(TR.tr("Convertir plusieurs images en pages à ajouter au document"));
                }else{
                    desc.setText(TR.tr("Convertir plusieurs images en un document PDF (Chaque image sera convertie en une page)"));
                }
            }

            Builders.setVBoxPosition(desc, 0, 0, 2.5);
            root.getChildren().add(desc);
        }
        public void setupSrcFilesForm(){

            VBox info;

            if(convertDirs){
                info = generateInfo(TR.tr("Dossier contenant les dossiers à convertir") + " :", true);

                HBox filePathBox = new HBox();

                srcDir = new TextField(MainWindow.userData.lastConvertSrcDir);
                Builders.setHBoxPosition(srcDir, -1, 30, 0, 2.5);
                srcDir.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(new File(srcDir.getText()).exists()) MainWindow.userData.lastConvertSrcDir = srcDir.getText();
                });

                Button changePath = new Button(TR.tr("Parcourir"));
                Builders.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));

                filePathBox.getChildren().addAll(srcDir, changePath);

                root.getChildren().addAll(info, filePathBox);

                changePath.setOnAction(event -> {

                    final DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle(TR.tr("Sélectionner un dossier"));
                    chooser.setInitialDirectory((new File(srcDir.getText()).exists() ? new File(srcDir.getText()) : new File(MainWindow.userData.lastConvertSrcDir)));

                    File file = chooser.showDialog(Main.window);
                    if(file != null) srcDir.setText(file.getAbsolutePath() + File.separator);
                });

            }else{
                info = generateInfo(TR.tr("Fichiers à convertir") + " :", true);

                HBox filePathBox = new HBox();

                srcFiles = new TextArea();
                Builders.setHBoxPosition(srcFiles, -1, 0, 0, 2.5);
                srcFiles.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(!srcFiles.getText().isBlank() && new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).exists())
                        MainWindow.userData.lastConvertSrcDir = new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).getParentFile().getAbsolutePath() + File.separator;
                });

                Button changePath = new Button(TR.tr("Sélectionner"));
                Builders.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));

                filePathBox.getChildren().addAll(srcFiles, changePath);

                root.getChildren().addAll(info, filePathBox);

                changePath.setOnAction(event -> {

                    final FileChooser chooser = new FileChooser();
                    chooser.setTitle(TR.tr("Sélectionner un ou plusieurs fichier"));
                    chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(TR.tr("Image"), "*.png", "*.jpeg", "*.jpg", "*.tiff", "*.gif", "*.bmp"));
                    if(!srcFiles.getText().isBlank()){
                        chooser.setInitialDirectory(new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).exists() ? new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).getParentFile() : new File(MainWindow.userData.lastConvertSrcDir));
                    }else{
                        chooser.setInitialDirectory(new File(MainWindow.userData.lastConvertSrcDir));
                    }

                    List<File> files = chooser.showOpenMultipleDialog(Main.window);
                    if(files != null){
                        for(File file : files) srcFiles.appendText(file.getAbsolutePath() + "\n");
                    }
                });

            }

        }
        public void setupDocNameForm(){

            if(!convertDirs){
                if(defaultSize == null){
                    VBox info = generateInfo(TR.tr("Nom du document") + " :", true);

                    docName = new TextField(MainWindow.userData.lastConvertFileName);
                    docName.setPromptText(TR.tr("Nom du document"));
                    Builders.setHBoxPosition(docName, -1, 30, 0, 2.5);
                    docName.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastConvertFileName = newValue);
                    root.getChildren().addAll(info, docName);
                }else{
                    docName = new TextField(MainWindow.mainScreen.document.getFileName());
                }
            }

        }
        public void setupOutDirForm(){

            if(defaultSize == null){
                VBox info = generateInfo(TR.tr("Dossier d'exportation") + " :", true);

                HBox filePathBox = new HBox();

                outDir = new TextField(MainWindow.lbFilesTab.getCurrentDir() != null ? MainWindow.lbFilesTab.getCurrentDir().getAbsolutePath() : MainWindow.userData.lastConvertSrcDir);
                Builders.setHBoxPosition(outDir, -1, 30, 0, 2.5);

                Button changePath = new Button(TR.tr("Parcourir"));
                Builders.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));

                filePathBox.getChildren().addAll(outDir, changePath);

                root.getChildren().addAll(info, filePathBox);

                changePath.setOnAction(event -> {

                    final DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle(TR.tr("Sélectionner un dossier"));
                    chooser.setInitialDirectory(new File(outDir.getText()).exists() ? new File(outDir.getText()) : ((MainWindow.lbFilesTab.getCurrentDir() == null ? new File(MainWindow.userData.lastConvertSrcDir) : MainWindow.lbFilesTab.getCurrentDir())));

                    File file = chooser.showDialog(Main.window);
                    if(file != null){
                        outDir.setText(file.getAbsolutePath() + File.separator);
                    }
                });
            }else{
                outDir = new TextField(System.getProperty("user.home"));
            }
        }
        public int widthFactor = 1;
        public int heightFactor = 1;
        public double mp = 1;
        public int width = 1;
        public int height = 1;
        public void setupSizeForm(){

            Separator separator = new Separator();
            Builders.setVBoxPosition(separator, 0, 0, new Insets(5, -5, 0, -5));

            HBox columns = new HBox();
            columns.setSpacing(10);

            // Definition COLUMN
            VBox definitionColumn = generateInfo(TR.tr("Définition des images") + " :", false);
            definition = new ComboBox<>(definitions);
            definition.setEditable(true);
            Builders.setHBoxPosition(definition, -1, 30, 2.5);
            definitionColumn.getChildren().add(definition);

            // Format COLUMN

            VBox formatColumn = generateInfo(TR.tr("Format des pages") + " :", false);
            format = new ComboBox<>(formats);
            format.setEditable(true);
            Builders.setHBoxPosition(format, -1, 30, 2.5);
            formatColumn.getChildren().add(format);

            // LISTENERS

            definition.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                updateDefaultValues();

                String data = StringUtils.removeAfterLastRejex(newValue, "Mp");
                Double mp = StringUtils.getDouble(data);
                if(mp != null){
                    this.mp = mp;
                    updateWidthAndHeight();
                }
            });
            format.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                updateDefaultValues();

                String data = StringUtils.removeAfterLastRejex(newValue, " (");
                if(data.split(":").length == 2){
                    Integer widthFactor = StringUtils.getInt(data.split(":")[0]);
                    Integer heightFactor = StringUtils.getInt(data.split(":")[1]);
                    if(widthFactor != null && heightFactor != null){
                        this.widthFactor = widthFactor;
                        this.heightFactor = heightFactor;
                        updateWidthAndHeight();
                    }
                }
            });

            setDefaultValues();

            /////////////////

            columns.getChildren().addAll(definitionColumn, formatColumn);
            root.getChildren().addAll(separator, columns);

        }
        private void updateWidthAndHeight(){
            this.width = (int) Math.sqrt((mp*1000000) / (heightFactor / ((double) widthFactor)));
            this.height = (int) (width * (heightFactor / ((double) widthFactor)));
        }
        // unused since the width and height can't be changed by the user
        private void updateFormatAndDefinition(){
            double mp = width * height / 1000000D;
            int gcd = GCD(width, height);
            int heightFactor = gcd == 0 ? height : height/gcd;
            int widthFactor = gcd == 0 ? width : width/gcd;

            if(this.mp != mp){
                this.mp = mp;
                definition.getSelectionModel().select(df.format(mp)+"Mp");
            }if(this.heightFactor != heightFactor || this.widthFactor != widthFactor){
                this.widthFactor = widthFactor;
                this.heightFactor = heightFactor;
                format.getSelectionModel().select(widthFactor + ":" + heightFactor);
            }
        }

        private void setDefaultValues(){

            if(definitions.contains(MainWindow.userData.lastConvertDefinition)) definition.getSelectionModel().select(MainWindow.userData.lastConvertDefinition);
            else{
                Double mp = StringUtils.getDouble(StringUtils.removeAfterLastRejex(MainWindow.userData.lastConvertDefinition, "Mp"));
                if(mp != null){
                    definition.getSelectionModel().select(MainWindow.userData.lastConvertDefinition);

                }else definition.getSelectionModel().select(4);
            }

            if(formats.contains(MainWindow.userData.lastConvertFormat)) format.getSelectionModel().select(MainWindow.userData.lastConvertFormat);
            else{
                String data = StringUtils.removeAfterLastRejex(MainWindow.userData.lastConvertFormat, " (");
                if(data.split(":").length == 2){
                    Integer widthFactor = StringUtils.getInt(data.split(":")[0]);
                    Integer heightFactor = StringUtils.getInt(data.split(":")[1]);
                    if(widthFactor != null && heightFactor != null){
                        format.getSelectionModel().select(MainWindow.userData.lastConvertFormat);

                    }else format.getSelectionModel().select(0);
                }else format.getSelectionModel().select(0);
            }
        }
        private void updateDefaultValues(){
            MainWindow.userData.lastConvertDefinition = definition.getEditor().getText();
            MainWindow.userData.lastConvertFormat = format.getEditor().getText();
        }

        public void setupSettingsForm(){
            VBox info = generateInfo(TR.tr("Paramètres") + " :", true);
            root.getChildren().add(info);
            if(convertDirs){

                Builders.setHBoxPosition(convertAloneFiles, 0, 30, 0, 2.5);
                convertAloneFiles.setSelected(MainWindow.userData.settingsConvertAloneImages);

                root.getChildren().add(convertAloneFiles);
                convertAloneFiles.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsConvertAloneImages = newValue);
            }

            Builders.setHBoxPosition(convertVoidFiles, 0, 30, 0, 2.5);
            convertVoidFiles.setSelected(MainWindow.userData.settingsConvertVoidFiles);

            root.getChildren().add(convertVoidFiles);
            convertVoidFiles.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsConvertVoidFiles = newValue);
        }

        public void setupBtns(){

            HBox btnBox = new HBox();

            Button cancel = new Button(TR.tr("Annuler"));
            Button export = new Button(TR.tr("Convertir"));
            export.requestFocus();

            btnBox.getChildren().addAll(cancel, export);
            btnBox.setAlignment(Pos.CENTER_RIGHT);

            HBox.setMargin(cancel, new Insets(50, 5, 0, 10));
            HBox.setMargin(export, new Insets(50, 10, 0, 5));

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            export.setOnAction(event -> {
                if(mp > 0 && widthFactor > 0 && heightFactor > 0){
                    if(convertDirs || !docName.getText().isEmpty()){
                        startConversion();
                    }else{
                        Alert alert = Builders.getAlert(Alert.AlertType.WARNING, TR.tr("Paramètres incorrects"));
                        alert.setHeaderText(TR.tr("Impossible de créer un fichier sans nom"));
                        alert.setContentText(TR.tr("Veuillez entrer le nom du document dans le champ prévus."));
                        alert.show();
                    }
                }else{
                    Alert alert = Builders.getAlert(Alert.AlertType.WARNING, TR.tr("Paramètres incorrects"));
                    alert.setHeaderText(TR.tr("Impossible de générer des images/pages de 0 pixels"));
                    alert.setContentText(TR.tr("Veuillez changer la définition des images ou le format des pages."));
                    alert.show();
                }
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



        Alert loadingAlert;
        private int converted;
        private int total;
        private void startConversion(){

            // Wait Dialog

            loadingAlert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Conversion..."));
            converted = 0;

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

            new Thread(() -> {
                try{
                    ConvertRenderer renderer = new ConvertRenderer(this);
                    total = renderer.getFilesLength();
                    ArrayList<ConvertedFile> convertedFiles = renderer.start(value -> {
                        Platform.runLater(() -> {
                            currentDocument.setText(value + " (" + converted + "/" + total + ")");
                            loadingBar.setProgress(converted/((float)total));
                            converted++;
                        });
                    });
                    Platform.runLater(() -> {
                        end(convertedFiles);
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        loadingAlert.close();
                        // Error dialog
                        Alert alert = Builders.getAlert(Alert.AlertType.ERROR, TR.tr("Erreur de conversion"));
                        alert.setHeaderText(TR.tr("Une erreur de conversion est survenue"));

                        TextArea textArea = new TextArea(e.getMessage());
                        textArea.setEditable(false);
                        textArea.setWrapText(true);
                        GridPane expContent = new GridPane();
                        expContent.setMaxWidth(Double.MAX_VALUE);
                        expContent.add(new Label(TR.tr("L'erreur survenue est la suivante :")), 0, 0);
                        expContent.add(textArea, 0, 1);
                        alert.getDialogPane().setExpandableContent(expContent);
                        alert.showAndWait();
                    });
                }
            }, "conversion").start();

        }

        private void end(ArrayList<ConvertedFile> files){
            loadingAlert.close();
            close();
            callBack.call(files);
        }

    }

    public int GCD(int a, int b){
        if(b==0) return a;
        return GCD(b,a%b);
    }
}
