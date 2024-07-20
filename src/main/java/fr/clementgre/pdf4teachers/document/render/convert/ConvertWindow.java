/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.convert;

import fr.clementgre.pdf4teachers.components.ScaledComboBox;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.LoadingAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.WrongAlert;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConvertWindow extends AlternativeWindow<TabPane> {
    
    public static ObservableList<String> definitions;
    
    public static ObservableList<String> formats;
    
    public static void setupTranslations(){
        definitions = FXCollections.observableArrayList(
                TR.tr("convertWindow.options.format.fitToImage"),
                "0.501832Mpix (A4, 72 dpi, ~90kB)",
                "0.967000Mpix (A4, 100dpi, ~150kB)",
                "2.175750Mpix (A4, 150dpi, ~280kB)",
                "3.868000Mpix (A4, 200dpi, ~450kB)",
                "8.699840Mpix (A4, 300dpi, ~800kB)",
                "34.81200Mpix (A4, 600dpi, 1.2MB)");
        
        formats = FXCollections.observableArrayList(
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
    }
    
    DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    
    public ConvertPane convertDirs;
    public ConvertPane convertFiles;
    
    PDRectangle defaultSize;
    CallBackArg<ArrayList<ConvertedFile>> callBack;
    
    public ConvertWindow(PDRectangle defaultSize, CallBackArg<ArrayList<ConvertedFile>> callBack){
        super(new TabPane(), StageWidth.LARGE, TR.tr("actions.convert"));
        this.defaultSize = defaultSize;
        this.callBack = callBack;
    }
    
    @Override
    public void setupSubClass(){
        root.setStyle("-fx-padding: 0;");
        df.setMaximumFractionDigits(340);
        
        // HEADER
        
        if(defaultSize == null) setSubHeaderText(TR.tr("convertWindow.convertMode.toPDF"));
        else{
            setSubHeaderText(TR.tr("convertWindow.convertMode.toPDFPages"));
            int gcd = GCD((int) defaultSize.getWidth(), (int) defaultSize.getHeight());
            int heightFactor = (int) (gcd == 0 ? defaultSize.getHeight() : defaultSize.getHeight() / gcd);
            int widthFactor = (int) (gcd == 0 ? defaultSize.getWidth() : defaultSize.getWidth() / gcd);
            //definitions.add(0, df.format(defaultSize.getWidth() * defaultSize.getHeight() / 1000000) + "Mp (" + TR.tr("Ce document") + ")");
            formats.add(1, widthFactor + ":" + heightFactor + " (" + TR.tr("convertWindow.options.format.currentPDFFormat") + ")");
        }
        
        // PANES
        
        convertDirs = new ConvertPane(this, TR.tr("convertWindow.convertMode.toPDF.convertDirs.tabName"), true);
        convertFiles = new ConvertPane(this, defaultSize == null ? TR.tr("convertWindow.convertMode.toPDF.convertFiles.tabName") : TR.tr("convertWindow.convertMode.toPDFPages.tabName"), false);
        
        if(defaultSize == null) root.getTabs().add(convertDirs);
        root.getTabs().add(convertFiles);
        setupBtns();
    }
    
    @Override
    public void afterShown(){
    }
    
    public void setupBtns(){
        Button cancel = new Button(TR.tr("actions.cancel"));
        Button export = new Button(TR.tr("actions.convert"));
        
        export.setOnAction(event -> {
            if(convertDirs.isSelected()) convertDirs.export();
            else if(convertFiles.isSelected()) convertFiles.export();
        });
        cancel.setOnAction(event -> {
            close();
        });
        
        setButtons(cancel, export);
    }
    
    public class ConvertPane extends Tab {
        
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
            root.setStyle("-fx-padding: 15;");
            
            setupDesc();
            setupSrcFilesForm();
            setupDocNameForm();
            setupOutDirForm();
            setupSizeForm();
            setupSettingsForm();
            
        }
        
        public void setupDesc(){
            
            Label desc = new Label();
            desc.setWrapText(true);
            
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
                
                srcDir = new TextField(FilesChooserManager.pathToExistingPath(null, FilesChooserManager.SyncVar.LAST_CONVERT_SRC_DIR,
                        MainWindow.filesTab.getCurrentDirAlways().getAbsolutePath()));
                
                PaneUtils.setHBoxPosition(srcDir, -1, 30, 0, 2.5);
                srcDir.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(new File(srcDir.getText()).exists()) MainWindow.userData.lastConvertSrcDir = srcDir.getText();
                });
                
                Button changePath = new Button(TR.tr("file.browse"));
                PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));
                changePath.setPadding(new Insets(0, 5, 0, 5));
                
                filePathBox.getChildren().addAll(srcDir, changePath);
                
                root.getChildren().addAll(info, filePathBox);
                
                changePath.setOnAction(event -> {
                    File file = FilesChooserManager.showDirectoryDialog(FilesChooserManager.SyncVar.LAST_CONVERT_SRC_DIR);
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
                changePath.setPadding(new Insets(0, 5, 0, 5));
                
                filePathBox.getChildren().addAll(srcFiles, changePath);
                root.getChildren().addAll(info, filePathBox);
                
                changePath.setOnAction(event -> {
                    File[] files = FilesChooserManager.showFilesDialog(FilesChooserManager.SyncVar.LAST_CONVERT_SRC_DIR,
                            TR.tr("dialog.file.extensionType.image"), ImageUtils.ACCEPTED_EXTENSIONS.stream().map((s) -> "*." + s).toList().toArray(new String[0]));
                    
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
                
                outDir = new TextField(FilesChooserManager.pathToExistingPath(MainWindow.filesTab.getCurrentDirAlways().getAbsolutePath(), MainWindow.userData.lastConvertSrcDir));
                
                PaneUtils.setHBoxPosition(outDir, -1, 30, 0, 2.5);
                
                Button changePath = new Button(TR.tr("file.browse"));
                PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));
                changePath.setPadding(new Insets(0, 5, 0, 5));
                filePathBox.getChildren().addAll(outDir, changePath);
                
                root.getChildren().addAll(info, filePathBox);
                
                changePath.setOnAction(event -> {
                    File file = FilesChooserManager.showDirectoryDialog(outDir.getText(), MainWindow.userData.lastConvertSrcDir);
                    if(file != null) outDir.setText(file.getAbsolutePath() + File.separator);
                });
            }else{
                // Not used with document conversion since the pages are directly added to the current document
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
            info.setWrapText(true);
            PaneUtils.setVBoxPosition(info, 0, 0, new Insets(5, 0, 0, 2.5));
            
            HBox columns = new HBox();
            columns.setSpacing(10);
            
            // Definition COLUMN
            VBox definitionColumn = generateInfo(TR.tr("convertWindow.options.definition.title"), false);
            definition = new ScaledComboBox<>(definitions, false);
            definition.setEditable(true);
            PaneUtils.setHBoxPosition(definition, -1, 30, 2.5);
            definitionColumn.getChildren().add(definition);
            
            
            // Format COLUMN
            
            VBox formatColumn = generateInfo(TR.tr("convertWindow.options.format.title"), false);
            format = new ScaledComboBox<>(formats, false);
            format.setEditable(true);
            PaneUtils.setHBoxPosition(format, -1, 30, 2.5);
            formatColumn.getChildren().add(format);
            
            // LISTENERS
            
            definition.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                updateDefaultValues();
                definition.applyCss(); // Prevent the black text on black bg bug
                
                String data = StringUtils.removeAfterLastOccurrence(newValue, "Mp");
                Double mp = MathUtils.parseDoubleOrNull(data);
                if(mp != null){
                    this.mp = mp;
                    updateWidthAndHeight();
                }
            });
            format.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                updateDefaultValues();
                format.applyCss(); // Prevent the black text on black bg bug
                
                String data = StringUtils.removeAfterLastOccurrence(newValue, " (");
                if(data.split(":").length == 2){
                    Integer widthFactor = MathUtils.parseIntOrNull(data.split(":")[0]);
                    Integer heightFactor = MathUtils.parseIntOrNull(data.split(":")[1]);
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
            width = (int) Math.sqrt((mp * 1000000) / (heightFactor / ((double) widthFactor)));
            height = (int) (width * (heightFactor / ((double) widthFactor)));
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
                Double mp = MathUtils.parseDoubleOrNull(StringUtils.removeAfterLastOccurrence(MainWindow.userData.lastConvertDefinition, "Mp"));
                if(mp != null){
                    definition.getSelectionModel().select(MainWindow.userData.lastConvertDefinition);
                    
                }else definition.getSelectionModel().select(3);
            }
            
            if(formats.contains(MainWindow.userData.lastConvertFormat))
                format.getSelectionModel().select(MainWindow.userData.lastConvertFormat);
            else{
                String data = StringUtils.removeAfterLastOccurrence(MainWindow.userData.lastConvertFormat, " (");
                if(data.split(":").length == 2){
                    Integer widthFactor = MathUtils.parseIntOrNull(data.split(":")[0]);
                    Integer heightFactor = MathUtils.parseIntOrNull(data.split(":")[1]);
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
            VBox info = generateInfo(TR.tr("options.title"), true);
            root.getChildren().add(info);
            if(convertDirs){
                
                PaneUtils.setHBoxPosition(convertAloneFiles, 0, 30, 0, 2.5);
                convertAloneFiles.setSelected(MainWindow.userData.settingsConvertAloneImages);
                convertAloneFiles.setWrapText(true);
                
                root.getChildren().add(convertAloneFiles);
                convertAloneFiles.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsConvertAloneImages = newValue);
                convertAloneFiles.setWrapText(true);
            }
            
            PaneUtils.setHBoxPosition(convertVoidFiles, 0, 30, 0, 2.5);
            convertVoidFiles.setSelected(MainWindow.userData.settingsConvertVoidFiles);
            convertVoidFiles.setWrapText(true);
            
            root.getChildren().add(convertVoidFiles);
            convertVoidFiles.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsConvertVoidFiles = newValue);
        }
        
        
        public VBox generateInfo(String text, boolean topBar){
            
            VBox box = new VBox();
            
            if(topBar){
                Separator separator = new Separator();
                PaneUtils.setVBoxPosition(separator, 0, 0, new Insets(5, -5, 0, -5));
                box.getChildren().add(separator);
            }
            
            Label info = new Label(text);
            info.setWrapText(true);
            PaneUtils.setVBoxPosition(info, 0, 0, 2.5);
            box.getChildren().add(info);
            
            return box;
        }
        
        public void export(){
            if(mp > 0 && widthFactor > 0 && heightFactor > 0){
                if(convertDirs || (!docName.getText().isEmpty() && !docName.getText().equalsIgnoreCase(".PDF"))){
                    startConversion();
                }else{
                    new WrongAlert(TR.tr("convertWindow.dialog.incorrectOptions.title"), TR.tr("convertWindow.dialog.incorrectOptions.fileWithoutName.header"),
                            TR.tr("convertWindow.dialog.incorrectOptions.fileWithoutName.details"), false).show();
                }
            }else{
                new WrongAlert(TR.tr("convertWindow.dialog.incorrectOptions.title"), TR.tr("convertWindow.dialog.incorrectOptions.0pxPage.header"),
                        TR.tr("convertWindow.dialog.incorrectOptions.0pxPage.details"), false).show();
            }
        }
        
        private LoadingAlert loadingAlert;
        private int converted;
        private boolean shouldStop;
        
        private void startConversion(){
            
            // Wait Dialog
            
            loadingAlert = new LoadingAlert(true, TR.tr("convertWindow.dialog.loading.title"), TR.tr("convertWindow.dialog.loading.title"));
            converted = 0;
            
            ConvertRenderer renderer = new ConvertRenderer(this);
            loadingAlert.showAsync(() -> {
                shouldStop = true;
                renderer.stop();
            });
            
            new Thread(() -> {
                try{
                    loadingAlert.setTotal(renderer.getFilesLength());
                    
                    // entry : String current document name | Double document internal advancement (range 0 ; 1)
                    ArrayList<ConvertedFile> convertedFiles = renderer.start(documentAndAdvancement -> {
                        Platform.runLater(() -> {
                            if(documentAndAdvancement.getKey().isBlank()){
                                loadingAlert.setProgress(-1);
                                loadingAlert.setCurrentTaskText("");
                            }else{
                                loadingAlert.setProgress(converted + Math.max(0, documentAndAdvancement.getValue()));
                                loadingAlert.setCurrentTaskText(documentAndAdvancement.getKey());
                            }
                            if(documentAndAdvancement.getValue() == -1) converted++;
                        });
                    });
                    Platform.runLater(() -> {
                        end(convertedFiles);
                    });
                }catch(Exception e){
                    Log.e(e);
                    Platform.runLater(() -> {
                        loadingAlert.close();
                        new ErrorAlert(TR.tr("convertWindow.dialog.error.header"), e.getMessage(), false).showAndWait();
                    });
                }
            }, "conversion").start();
            
        }
        
        private void end(ArrayList<ConvertedFile> files){
            loadingAlert.close();
            close();
            if(!shouldStop) callBack.call(files);
        }
        
    }
    
    public static int GCD(int a, int b){
        if(b == 0) return a;
        return GCD(b, a % b);
    }
}
