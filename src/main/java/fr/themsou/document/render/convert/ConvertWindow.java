package fr.themsou.document.render.convert;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.Builders;
import fr.themsou.utils.CallBack;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.MainWindow;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ConvertWindow extends Stage {

    public static ObservableList<String> definitions = FXCollections.observableArrayList(
            "0.50Mp (A4 72 dpi)", "0.77Mp (HD / A4 96dpi)", "2.07Mp (Full HD / A4 150dpi)", "3.69Mp (Quad HD)", "8.29Mp (4k / A4 300dpi)");
    public static ObservableList<String> formats = FXCollections.observableArrayList(
            "594:841 (A4 " + TR.tr("Portrait") + ")", "841:594 (A4 " + TR.tr("Paysage") + ")", "16:9", "9:16", "4:3", "3:4");

    PDRectangle defaultSize;
    CallBack<ArrayList<ConvertedFile>> callBack;
    public ConvertWindow(PDRectangle defaultSize, CallBack<ArrayList<ConvertedFile>> callBack){
        this.defaultSize = defaultSize;
        this.callBack = callBack;

        TabPane tabPane = new TabPane();

        ExportPane convertDirs = new ExportPane(this, TR.tr("Convertir des dossiers en plusieurs documents"), true);
        ExportPane convertFiles = new ExportPane(this, TR.tr("Convertir des fichiers en un document"), false);

        VBox root = new VBox();
        Scene scene = new Scene(root);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setResizable(false);
        setTitle(TR.tr("PDF4Teachers - Convertir"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);

        Text info;

        if(defaultSize == null) info = new Text(TR.tr("Convertir des images en documents PDF") + "\n\n" +
                TR.tr("Il est possible de :") + "\n" +
                TR.tr("- convertir plusieurs images en un document PDF (Chaque image sera convertie en une page)") + "\n" +
                TR.tr("- convertir plusieurs dossiers en plusieurs documents PDF\n   (Chaque dossier sera convertis en un document, les images contenus dans le dossier représentent les pages)"));
        else info = new Text(TR.tr("Convertir des images en documents PDF") + "\n\n" +
                TR.tr("Chaque image sera convertie en une page"));

        VBox.setMargin(info, new Insets(40, 0, 40, 10));

        if(defaultSize == null) tabPane.getTabs().add(convertDirs);
        tabPane.getTabs().add(convertFiles);

        root.getChildren().addAll(info, tabPane);

        show();
    }

    class ExportPane extends Tab {

        public boolean convertDirs;
        ConvertWindow window;

        VBox root = new VBox();

        public TextArea srcFiles;
        public TextField srcDir, docName, outDir, width, height;
        ComboBox<String> definition, format;

        public CheckBox convertAloneFiles = new CheckBox(TR.tr("Convertir aussi les images du dossier source en documents (un document par image)"));

        public ExportPane(ConvertWindow window, String tabName, boolean convertDirs){
            super(tabName);
            this.window = window;
            this.convertDirs = convertDirs;

            setClosable(false);
            setContent(root);
            root.setStyle("-fx-padding: 10;");

            setupSrcFilesForm();
            setupDocNameForm();
            setupOutDirForm();
            setupSizeForm();
            setupSettingsForm();
            setupBtns();

        }
        public void setupSrcFilesForm(){

            VBox info;

            if(convertDirs){
                info = generateInfo(TR.tr("Dossier contenant les dossiers à convertir") + " :", false);

                HBox filePathBox = new HBox();

                srcDir = new TextField(MainWindow.userData.lastConvertSrcDir);
                Builders.setHBoxPosition(srcDir, -1, 30, 0, 2.5);
                srcDir.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(new File(srcDir.getText()).exists()) MainWindow.userData.lastConvertSrcDir = srcDir.getText() + File.separator;
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
                info = generateInfo(TR.tr("Fichiers à convertir") + " :", false);

                HBox filePathBox = new HBox();

                srcFiles = new TextArea();
                Builders.setHBoxPosition(srcFiles, -1, 0, 0, 2.5);
                srcFiles.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).exists())
                        MainWindow.userData.lastConvertSrcDir = new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).getParentFile().getAbsolutePath() + File.separator;
                });

                Button changePath = new Button(TR.tr("Sélectionner"));
                Builders.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));

                filePathBox.getChildren().addAll(srcFiles, changePath);

                root.getChildren().addAll(info, filePathBox);

                changePath.setOnAction(event -> {

                    final FileChooser chooser = new FileChooser();
                    chooser.setTitle(TR.tr("Sélectionner un dossier"));
                    chooser.setInitialDirectory(new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).exists() ? new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).getParentFile() : new File(MainWindow.userData.lastConvertSrcDir));

                    chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(TR.tr("Image"), "*.png", "*.jpeg", "*.jpg"));
                    chooser.setTitle(TR.tr("Sélectionner un ou plusieurs fichier"));
                    chooser.setInitialDirectory((UserData.lastOpenDir.exists() ? UserData.lastOpenDir : new File(System.getProperty("user.home"))));

                    List<File> files = chooser.showOpenMultipleDialog(Main.window);
                    if(files != null){
                        srcFiles.setText("");
                        for(File file : files) srcFiles.appendText(file.getAbsolutePath() + "\n");
                    }
                });

            }

        }
        public void setupDocNameForm(){

            if(!convertDirs){
                VBox info = generateInfo(TR.tr("Nom du document") + " :", true);

                docName = new TextField(MainWindow.userData.lastConvertFileName);
                docName.setPromptText(TR.tr("Nom du document"));
                Builders.setHBoxPosition(docName, -1, 30, 0, 2.5);
                docName.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastConvertFileName = newValue);
                root.getChildren().addAll(info, docName);
            }

        }
        public void setupOutDirForm(){

            VBox info = generateInfo(TR.tr("Dossier d'exportation") + " :", true);

            HBox filePathBox = new HBox();

            outDir = new TextField(MainWindow.lbFilesTab.getCurrentDir() == null ? MainWindow.userData.lastConvertSrcDir : MainWindow.lbFilesTab.getCurrentDir().getAbsolutePath());
            Builders.setHBoxPosition(outDir, -1, 30, 0, 2.5);
            outDir.textProperty().addListener((observable, oldValue, newValue) -> {

            });

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

        }
        private int widthFactor;
        private int heightFactor;
        private double mp;
        private int widthPixels;
        private int heightPixels;
        public void setupSizeForm(){

            Separator separator = new Separator();
            Builders.setVBoxPosition(separator, 0, 0, new Insets(5, -5, 0, -5));

            HBox columns = new HBox();
            columns.setSpacing(10);

            // Definition COLUMN
            VBox definitionColumn = generateInfo(TR.tr("Définition") + " :", false);
            definition = new ComboBox<>(definitions);
            definition.setEditable(true);
            Builders.setHBoxPosition(definition, -1, 30, 2.5);
            definition.getSelectionModel().select("2.07Mp (Full HD / A4 150dpi)");
            definitionColumn.getChildren().add(definition);

            // Format COLUMN

            VBox formatColumn = generateInfo(TR.tr("Format") + " :", false);
            format = new ComboBox<>(formats);
            format.setEditable(true);
            Builders.setHBoxPosition(format, -1, 30, 2.5);
            format.getSelectionModel().select("594:841 (A4 " + TR.tr("Portrait") + ")");
            formatColumn.getChildren().add(format);

            // SIZE COLUMN

            GridPane sizeColumn = new GridPane();
            HBox.setMargin(sizeColumn, new Insets(4, 0, 0, 0));
            VBox widthInfo = generateInfo(TR.tr("Largeur") + " :", false);
            width = new TextField();
            sizeColumn.add(widthInfo, 0, 0);
            sizeColumn.add(width, 1, 0);

            VBox heightInfo = generateInfo(TR.tr("Hauteur") + " :", false);
            height = new TextField();
            sizeColumn.add(heightInfo, 0, 1);
            sizeColumn.add(height, 1, 1);

            // LISTENERS

            definition.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                String data = StringUtils.removeAfterLastRejex(newValue, "Mp");
                Double mp = StringUtils.getDouble(data);
                if(mp != null){
                    if(this.mp != mp){
                        this.mp = mp;
                        updateWidthAndHeight();
                    }
                }
            });
            format.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                String data = StringUtils.removeAfterLastRejex(newValue, " (");
                if(data.split(":").length == 2){
                    Integer widthFactor = StringUtils.getInt(data.split(":")[0]);
                    Integer heightFactor = StringUtils.getInt(data.split(":")[1]);
                    if(widthFactor != null && heightFactor != null){
                        if(this.widthFactor != widthFactor || this.heightFactor != heightFactor){
                            this.widthFactor = widthFactor;
                            this.heightFactor = heightFactor;
                            updateWidthAndHeight();
                        }
                    }
                }
            });
            width.textProperty().addListener((observable, oldValue, newValue) -> {
                Integer data = StringUtils.getInt(newValue);
                if(data != null){
                    if(widthPixels != data){
                        widthPixels = data;
                        updateFormatAndDefinition();
                    }
                }
            });
            height.textProperty().addListener((observable, oldValue, newValue) -> {
                Integer data = StringUtils.getInt(newValue);
                if(data != null){
                    if(heightPixels != data){
                        heightPixels = data;
                        updateFormatAndDefinition();
                    }
                }
            });

            /////////////////

            columns.getChildren().addAll(definitionColumn, formatColumn, sizeColumn);
            root.getChildren().addAll(separator, columns);

        }
        private void updateWidthAndHeight(){
            int width = (int) Math.sqrt((mp*1000000) / (heightFactor / ((double) widthFactor)));
            int height = (int) (width * (heightFactor / ((double) widthFactor)));

            if(widthPixels != width){
                widthPixels = width;
                this.width.setText(width + "");
            }if(heightPixels != height){
                heightPixels = height;
                this.height.setText(height+"");
            }
        }
        private void updateFormatAndDefinition(){
            double mp = widthPixels * heightPixels / 1000000D;
            int gcd = GCD(widthPixels, heightPixels);
            int heightFactor = gcd == 0 ? heightPixels : heightPixels/gcd;
            int widthFactor = gcd == 0 ? widthPixels : widthPixels/gcd;

            if(this.mp != mp){
                this.mp = mp;
                definition.getEditor().setText(mp+"Mp");
            }if(this.heightFactor != heightFactor || this.widthFactor != widthFactor){
                this.widthFactor = widthFactor;
                this.heightFactor = heightFactor;
                format.getEditor().setText(widthFactor + ":" + heightFactor);
            }
        }
        public int GCD(int a, int b){
            if(b==0) return a;
            return GCD(b,a%b);
        }


        public void setupSettingsForm(){
            if(convertDirs){
                VBox info = generateInfo(TR.tr("Paramètres") + " :", true);

                Builders.setHBoxPosition(convertAloneFiles, 0, 30, 0, 2.5);
                convertAloneFiles.setSelected(MainWindow.userData.settingsConvertAloneImages);

                root.getChildren().addAll(info, convertAloneFiles);

                convertAloneFiles.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsConvertAloneImages = newValue);
            }
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
                end(new ConvertRenderer(this).start());
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

        private void end(ArrayList<ConvertedFile> files){
            close();
            callBack.call(files);
        }

    }
}
