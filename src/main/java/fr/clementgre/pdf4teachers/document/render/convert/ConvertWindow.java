package fr.clementgre.pdf4teachers.document.render.convert;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConvertWindow extends Stage{
    
    public static ObservableList<String> definitions = FXCollections.observableArrayList(
            TR.tr("convertWindow.options.format.fitToImage"),
            "0.501832Mpix (A4, 72 dpi, ~90kB)",
            "0.967000Mpix (A4, 100dpi, ~150kB)",
            "2.175750Mpix (A4, 150dpi, ~280kB)",
            "3.868000Mpix (A4, 200dpi, ~450kB)",
            "8.699840Mpix (A4, 300dpi, ~800kB)",
            "34.81200Mpix (A4, 600dpi, 1.2MB)");
    
    public static ObservableList<String> formats = FXCollections.observableArrayList(
            TR.tr("convertWindow.options.format.fitToImage"),
            "594:841 (A4 " + TR.tr("convertWindow.options.format.portrait") + ")",
            "841:594 (A4 " + TR.tr("convertWindow.options.format.landscape") + ")",
            "216:279 (US Letter " + TR.tr("convertWindow.options.format.portrait") + ")",
            "279:216 (US Letter " + TR.tr("convertWindow.options.format.landscape") + ")",
            "216:356 (US Legal " + TR.tr("convertWindow.options.format.portrait") + ")",
            "356:216 (US Legal " + TR.tr("convertWindow.options.format.landscape") + ")",
            "16:9",
            "9:16",
            "4:3",
            "3:4");
    
    DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    
    public TabPane tabPane = new TabPane();
    public ConvertPane convertDirs;
    public ConvertPane convertFiles;
    
    PDRectangle defaultSize;
    CallBackArg<ArrayList<ConvertedFile>> callBack;
    
    public ConvertWindow(PDRectangle defaultSize, CallBackArg<ArrayList<ConvertedFile>> callBack){
        this.defaultSize = defaultSize;
        this.callBack = callBack;
        
        df.setMaximumFractionDigits(340);
        
        VBox root = new VBox();
        Scene scene = new Scene(root);
        
        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        setResizable(false);
        setTitle(TR.tr("convertWindow.title"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);
        
        // HEADER
        
        Text info;
        if(defaultSize == null) info = new Text(TR.tr("convertWindow.convertMode.toPDF"));
        else{
            info = new Text(TR.tr("convertWindow.convertMode.toPDFPages"));
            int gcd = GCD((int) defaultSize.getWidth(), (int) defaultSize.getHeight());
            int heightFactor = (int) (gcd == 0 ? defaultSize.getHeight() : defaultSize.getHeight() / gcd);
            int widthFactor = (int) (gcd == 0 ? defaultSize.getWidth() : defaultSize.getWidth() / gcd);
            //definitions.add(0, df.format(defaultSize.getWidth() * defaultSize.getHeight() / 1000000) + "Mp (" + TR.tr("Ce document") + ")");
            formats.add(1, widthFactor + ":" + heightFactor + " (" + TR.tr("convertWindow.options.format.currentPDFFormat") + ")");
        }
        
        VBox.setMargin(info, new Insets(40, 0, 40, 10));
        
        // PANES
        
        convertDirs = new ConvertPane(this, TR.tr("convertWindow.convertMode.toPDF.convertDirs.tabName"), true);
        convertFiles = new ConvertPane(this, defaultSize == null ? TR.tr("convertWindow.convertMode.toPDF.convertFiles.tabName") : TR.tr("convertWindow.convertMode.toPDFPages.tabName"), false);
        
        if(defaultSize == null) tabPane.getTabs().add(convertDirs);
        tabPane.getTabs().add(convertFiles);
        root.getChildren().addAll(info, tabPane);
        
        // SHOW
        
        show();
        Main.window.centerWindowIntoMe(this);
    }
    
    public class ConvertPane extends Tab{
        
        public boolean convertDirs;
        ConvertWindow window;
        
        boolean convertToExistingDoc;
        
        VBox root = new VBox();
        
        public TextArea srcFiles;
        public TextField srcDir, docName, outDir;
        ComboBox<String> definition, format;
        
        public CheckBox convertAloneFiles = new CheckBox(TR.tr("convertWindow.convertMode.options.convertAloneFiles"));
        public CheckBox convertVoidFiles = new CheckBox(TR.tr("convertWindow.convertMode.options.convertVoidFiles"));
        
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
                desc.setText(TR.tr("convertWindow.convertMode.toPDF.convertDirs.title") + "\n" +
                        String.join("\n", Arrays.stream(TR.tr("convertWindow.convertMode.toPDF.convertDirs.description")
                                .split(Pattern.quote("\n")))
                                .map((str) -> "   " + str)
                                .toArray(String[]::new)));
            }else{
                if(defaultSize != null){
                    desc.setText(TR.tr("convertWindow.convertMode.toPDFPages.title"));
                }else{
                    desc.setText(TR.tr("convertWindow.convertMode.toPDF.convertFiles.title"));
                }
            }
            
            PaneUtils.setVBoxPosition(desc, 0, 0, 2.5);
            root.getChildren().add(desc);
        }
        
        public void setupSrcFilesForm(){
            
            VBox info;
            
            if(convertDirs){
                info = generateInfo(TR.tr("convertWindow.convertMode.options.source.directory"), true);
                
                HBox filePathBox = new HBox();
                
                srcDir = new TextField(MainWindow.userData.lastConvertSrcDir);
                PaneUtils.setHBoxPosition(srcDir, -1, 30, 0, 2.5);
                srcDir.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(new File(srcDir.getText()).exists()) MainWindow.userData.lastConvertSrcDir = srcDir.getText();
                });
                
                Button changePath = new Button(TR.tr("file.browse"));
                PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));
                
                filePathBox.getChildren().addAll(srcDir, changePath);
                
                root.getChildren().addAll(info, filePathBox);
                
                changePath.setOnAction(event -> {
                    
                    final DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle(TR.tr("dialog.file.selectFolder.title"));
                    chooser.setInitialDirectory((new File(srcDir.getText()).exists() ? new File(srcDir.getText()) : new File(MainWindow.userData.lastConvertSrcDir)));
                    
                    File file = chooser.showDialog(Main.window);
                    if(file != null) srcDir.setText(file.getAbsolutePath() + File.separator);
                });
                
            }else{
                info = generateInfo(TR.tr("convertWindow.convertMode.options.source.pictures"), true);
                
                HBox filePathBox = new HBox();
                
                srcFiles = new TextArea();
                PaneUtils.setHBoxPosition(srcFiles, -1, 0, 0, 2.5);
                srcFiles.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(!srcFiles.getText().isBlank() && new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).exists())
                        MainWindow.userData.lastConvertSrcDir = new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).getParentFile().getAbsolutePath() + File.separator;
                });
                
                Button changePath = new Button(TR.tr("file.browse"));
                PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));
                
                filePathBox.getChildren().addAll(srcFiles, changePath);
                
                root.getChildren().addAll(info, filePathBox);
                
                changePath.setOnAction(event -> {
                    
                    final FileChooser chooser = new FileChooser();
                    chooser.setTitle(TR.tr("dialog.file.selectFiles.title"));
                    chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(TR.tr("dialog.file.extensionType.image"), ImageUtils.ACCEPTED_EXTENSIONS.stream().map((s) -> "*." + s).collect(Collectors.toList())));
                    if(!srcFiles.getText().isBlank() && new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).exists()){
                        chooser.setInitialDirectory(new File(srcFiles.getText().split(Pattern.quote("\n"))[0]).getParentFile());
                    }else{
                        chooser.setInitialDirectory(new File(MainWindow.userData.lastConvertSrcDir).exists() ? new File(MainWindow.userData.lastConvertSrcDir) : FilesUtils.HOME_DIR);
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
                    VBox info = generateInfo(TR.tr("file.documentName") + " :", true);
                    
                    docName = new TextField(MainWindow.userData.lastConvertFileName);
                    docName.setPromptText(TR.tr("file.documentName"));
                    PaneUtils.setHBoxPosition(docName, -1, 30, 0, 2.5);
                    docName.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastConvertFileName = newValue);
                    root.getChildren().addAll(info, docName);
                }else{
                    docName = new TextField(MainWindow.mainScreen.document.getFileName());
                }
            }
            
        }
        
        public void setupOutDirForm(){
            
            if(defaultSize == null){
                VBox info = generateInfo(TR.tr("file.destinationFolder") + " :", true);
                
                HBox filePathBox = new HBox();
                
                outDir = new TextField(MainWindow.filesTab.getCurrentDir() != null ? MainWindow.filesTab.getCurrentDir().getAbsolutePath() : MainWindow.userData.lastConvertSrcDir);
                PaneUtils.setHBoxPosition(outDir, -1, 30, 0, 2.5);
                
                Button changePath = new Button(TR.tr("file.browse"));
                PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));
                
                filePathBox.getChildren().addAll(outDir, changePath);
                
                root.getChildren().addAll(info, filePathBox);
                
                changePath.setOnAction(event -> {
                    
                    final DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle(TR.tr("dialog.file.selectFolder.title"));
                    chooser.setInitialDirectory(new File(outDir.getText()).exists() ? new File(outDir.getText()) : ((MainWindow.filesTab.getCurrentDir() == null ? new File(MainWindow.userData.lastConvertSrcDir) : MainWindow.filesTab.getCurrentDir())));
                    
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
            PaneUtils.setVBoxPosition(separator, 0, 0, new Insets(5, -5, 0, -5));
            
            Label info = new Label(TR.tr("convertWindow.options.definition.header"));
            PaneUtils.setVBoxPosition(info, 0, 0, new Insets(5, 0, 0, 2.5));
            
            HBox columns = new HBox();
            columns.setSpacing(10);
            
            // Definition COLUMN
            VBox definitionColumn = generateInfo(TR.tr("convertWindow.options.definition.title"), false);
            definition = new ComboBox<>(definitions);
            definition.setEditable(true);
            PaneUtils.setHBoxPosition(definition, -1, 30, 2.5);
            definitionColumn.getChildren().add(definition);

            
            // Format COLUMN
            
            VBox formatColumn = generateInfo(TR.tr("convertWindow.options.format.title"), false);
            format = new ComboBox<>(formats);
            format.setEditable(true);
            PaneUtils.setHBoxPosition(format, -1, 30, 2.5);
            formatColumn.getChildren().add(format);

            // LISTENERS
            
            definition.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                updateDefaultValues();
                definition.applyCss(); // Prevent the black text on black bg bug

                String data = StringUtils.removeAfterLastRegex(newValue, "Mpix");
                Double mp = StringUtils.getDouble(data);
                if(mp != null){
                    this.mp = mp;
                    updateWidthAndHeight();
                }
            });
            format.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                updateDefaultValues();
                format.applyCss(); // Prevent the black text on black bg bug

                String data = StringUtils.removeAfterLastRegex(newValue, " (");
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
            root.getChildren().addAll(separator, info, columns);
            
        }
        
        private void updateWidthAndHeight(){
            this.width = (int) Math.sqrt((mp * 1000000) / (heightFactor / ((double) widthFactor)));
            this.height = (int) (width * (heightFactor / ((double) widthFactor)));
        }
        
        // unused since the width and height can't be changed by the user
        private void updateFormatAndDefinition(){
            double mp = width * height / 1000000D;
            int gcd = GCD(width, height);
            int heightFactor = gcd == 0 ? height : height / gcd;
            int widthFactor = gcd == 0 ? width : width / gcd;
            
            if(this.mp != mp){
                this.mp = mp;
                definition.getSelectionModel().select(df.format(mp) + "Mp");
            }
            if(this.heightFactor != heightFactor || this.widthFactor != widthFactor){
                this.widthFactor = widthFactor;
                this.heightFactor = heightFactor;
                format.getSelectionModel().select(widthFactor + ":" + heightFactor);
            }
        }
        
        private void setDefaultValues(){
            
            if(definitions.contains(MainWindow.userData.lastConvertDefinition))
                definition.getSelectionModel().select(MainWindow.userData.lastConvertDefinition);
            else{
                Double mp = StringUtils.getDouble(StringUtils.removeAfterLastRegex(MainWindow.userData.lastConvertDefinition, "Mp"));
                if(mp != null){
                    definition.getSelectionModel().select(MainWindow.userData.lastConvertDefinition);
                    
                }else definition.getSelectionModel().select(3);
            }
            
            if(formats.contains(MainWindow.userData.lastConvertFormat))
                format.getSelectionModel().select(MainWindow.userData.lastConvertFormat);
            else{
                String data = StringUtils.removeAfterLastRegex(MainWindow.userData.lastConvertFormat, " (");
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
            VBox info = generateInfo(TR.tr("convertWindow.options.title"), true);
            root.getChildren().add(info);
            if(convertDirs){
                
                PaneUtils.setHBoxPosition(convertAloneFiles, 0, 30, 0, 2.5);
                convertAloneFiles.setSelected(MainWindow.userData.settingsConvertAloneImages);
                
                root.getChildren().add(convertAloneFiles);
                convertAloneFiles.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsConvertAloneImages = newValue);
            }
            
            PaneUtils.setHBoxPosition(convertVoidFiles, 0, 30, 0, 2.5);
            convertVoidFiles.setSelected(MainWindow.userData.settingsConvertVoidFiles);
            
            root.getChildren().add(convertVoidFiles);
            convertVoidFiles.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsConvertVoidFiles = newValue);
        }
        
        public void setupBtns(){
            
            HBox btnBox = new HBox();
            
            Button cancel = new Button(TR.tr("actions.cancel"));
            Button export = new Button(TR.tr("actions.convert"));
            export.requestFocus();
            
            btnBox.getChildren().addAll(cancel, export);
            btnBox.setAlignment(Pos.CENTER_RIGHT);
            
            HBox.setMargin(cancel, new Insets(20, 5, 0, 10));
            HBox.setMargin(export, new Insets(20, 10, 0, 5));
            
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            
            export.setOnAction(event -> {
                if(mp > 0 && widthFactor > 0 && heightFactor > 0){
                    if(convertDirs || (!docName.getText().isEmpty() && !docName.getText().equalsIgnoreCase(".PDF"))){
                        startConversion();
                    }else{
                        Alert alert = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("convertWindow.dialog.incorrectOptions.title"));
                        alert.setHeaderText(TR.tr("convertWindow.dialog.incorrectOptions.fileWithoutName.header"));
                        alert.setContentText(TR.tr("convertWindow.dialog.incorrectOptions.fileWithoutName.details"));
                        alert.show();
                    }
                }else{
                    Alert alert = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("convertWindow.dialog.incorrectOptions.title"));
                    alert.setHeaderText(TR.tr("convertWindow.dialog.incorrectOptions.0pxPage.header"));
                    alert.setContentText(TR.tr("convertWindow.dialog.incorrectOptions.0pxPage.details"));
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
                PaneUtils.setVBoxPosition(separator, 0, 0, new Insets(5, -5, 0, -5));
                box.getChildren().add(separator);
            }
            
            Label info = new Label(text);
            PaneUtils.setVBoxPosition(info, 0, 0, 2.5);
            box.getChildren().add(info);
            
            return box;
        }
        
        
        Alert loadingAlert;
        private int converted;
        private int total;
        
        private void startConversion(){
            
            // Wait Dialog
            
            loadingAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("convertWindow.dialog.loading.title"));
            converted = 0;
            
            loadingAlert.setWidth(600);
            loadingAlert.setHeaderText(TR.tr("dialogs.asyncAction.header"));
            
            VBox pane = new VBox();
            Label currentDocument = new Label();
            ProgressBar loadingBar = new ProgressBar();
            loadingBar.setMinHeight(10);
            VBox.setMargin(loadingBar, new Insets(10, 0, 0, 0));
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
                            loadingBar.setProgress(converted / ((float) total));
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
                        Alert alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.tr("convertWindow.dialog.error.title"));
                        alert.setHeaderText(TR.tr("convertWindow.dialog.error.header"));
                        
                        TextArea textArea = new TextArea(e.getMessage());
                        textArea.setEditable(false);
                        textArea.setWrapText(true);
                        GridPane expContent = new GridPane();
                        expContent.setMaxWidth(Double.MAX_VALUE);
                        expContent.add(new Label(TR.tr("convertWindow.dialog.error.details")), 0, 0);
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
    
    public static int GCD(int a, int b){
        if(b == 0) return a;
        return GCD(b, a % b);
    }
}
