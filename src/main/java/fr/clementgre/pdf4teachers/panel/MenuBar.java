package fr.clementgre.pdf4teachers.panel;

import de.jangassen.MenuToolkit;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.datasaving.settings.Setting;
import fr.clementgre.pdf4teachers.datasaving.settings.SettingObject;
import fr.clementgre.pdf4teachers.datasaving.settings.Settings;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.EditionExporter;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.display.PageEditPane;
import fr.clementgre.pdf4teachers.document.render.export.ExportWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.LogWindow;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar{
    
    ////////// ICONS COLOR //////////
    
    public static ColorAdjust colorAdjust = new ColorAdjust();
    
    static{
        if(StyleManager.ACCENT_STYLE == jfxtras.styles.jmetro.Style.DARK) colorAdjust.setBrightness(-0.5);
        else colorAdjust.setBrightness(-1);
    }
    
    ////////// FILE //////////
    
    Menu file = new Menu(TR.tr("menuBar.file"));
    public MenuItem file1Open = createMenuItem(TR.tr("menuBar.file.openFiles"), SVGPathIcons.PDF_FILE, new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.openFiles.tooltip"));
    
    public MenuItem file2OpenDir = createMenuItem(TR.tr("menuBar.file.openDir"), SVGPathIcons.FOLDER, new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.openDir.tooltip"));
    
    MenuItem file3Clear = createMenuItem(TR.tr("menuBar.file.clearList"), SVGPathIcons.LIST, new KeyCodeCombination(KeyCode.W, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.clearList.tooltip"), false, true, false);
    
    MenuItem file4Save = createMenuItem(TR.tr("menuBar.file.saveEdit"), SVGPathIcons.SAVE_LITE, new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.saveEdit.tooltip"), true, false, false);
    
    MenuItem file5Delete = createMenuItem(TR.tr("menuBar.file.deleteEdit"), SVGPathIcons.TRASH, null,
            TR.tr("menuBar.file.deleteEdit.tooltip"), true, false, false);
    
    MenuItem file6Close = createMenuItem(TR.tr("menuBar.file.closeDocument"), SVGPathIcons.CROSS, new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.closeDocument.tooltip"), true, false, false);
    
    MenuItem file7Export = createMenuItem(TR.tr("menuBar.file.export"), SVGPathIcons.EXPORT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.export.tooltip"), true, false, false);
    
    MenuItem file8ExportAll = createMenuItem(TR.tr("menuBar.file.exportAll"), SVGPathIcons.EXPORT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.exportAll.tooltip"), false, true, false);
    
    
    ////////// TOOLS //////////
    
    public Menu tools = new Menu(TR.tr("menuBar.tools"));
    
    MenuItem tools1Convert = createMenuItem(TR.tr("menuBar.tools.convertImages"), SVGPathIcons.PICTURES, new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.tools.convertImages.tooltip"), false, false, false);
    
    Menu tools3AddPages = createSubMenu(TR.tr("menuBar.tools.addPages"), SVGPathIcons.PLUS,
            TR.tr("menuBar.tools.addPages.tooltip"), true);
    
    MenuItem tools4DeleteAllEdits = createMenuItem(TR.tr("menuBar.tools.deleteAllEdits"), SVGPathIcons.TRASH, null,
            TR.tr("menuBar.tools.deleteAllEdits.tooltip"));
    
    Menu tools5SameNameEditions = createSubMenu(TR.tr("menuBar.tools.sameNameEdits"), SVGPathIcons.EXCHANGE,
            TR.tr("menuBar.tools.sameNameEdits.tooltip"), true);
    MenuItem tools5SameNameEditionsNull = new MenuItem(TR.tr("menuBar.tools.sameNameEdits.noEditFounded"));
    
    Menu tools6ExportImportEdition = createSubMenu(TR.tr("menuBar.tools.exportOrImportEditOrGradeScale"), SVGPathIcons.EXPORT,
            TR.tr("menuBar.tools.exportOrImportEditOrGradeScale.tooltip"), true);
    
    MenuItem tools6ExportEdition1All = createMenuItem(TR.tr("menuBar.tools.exportEdit"), null, null,
            TR.tr("menuBar.tools.exportEdit.tooltip"), true, false, false, false);
    MenuItem tools6ExportEdition2Grades = createMenuItem(TR.tr("menuBar.tools.exportGradeScale"), null, null,
            TR.tr("menuBar.tools.exportGradeScale.tooltip"), true, false, false, false);
    
    MenuItem tools6ImportEdition1All = createMenuItem(TR.tr("menuBar.tools.importEdit"), null, null,
            TR.tr("menuBar.tools.importEdit.tooltip"), true, false, false, false);
    MenuItem tools6ImportEdition2Grades = createMenuItem(TR.tr("menuBar.tools.importGradeScale"), null, null,
            TR.tr("menuBar.tools.importGradeScale.tooltip"), true, false, false, false);
    
    MenuItem tools8FullScreen = createMenuItem(TR.tr("menuBar.tools.fullScreenMode"), SVGPathIcons.FULL_SCREEN, null,
            TR.tr("menuBar.tools.fullScreenMode.tooltip"));
    
    Menu tools9Debug = createSubMenu(TR.tr("menuBar.tools.debug"), SVGPathIcons.COMMAND_PROMPT,
            TR.tr("menuBar.tools.debug.tooltip"), false);
    
    MenuItem tools9Debug1OpenConsole = createMenuItem(TR.tr("menuBar.tools.debug.openPrintStream") + " (" + (Main.COPY_CONSOLE ? "Activée" : "Désactivée") + ")", null, new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.tools.debug.openPrintStream.tooltip"), false, false, false, false);
    MenuItem tools9Debug2OpenAppFolder = createMenuItem(TR.tr("menuBar.tools.debug.openDataFolder"), null, null,
            TR.tr("menuBar.tools.debug.openDataFolder.tooltip"), false, false, false, false);
    MenuItem tools9Debug3OpenEditionFile = createMenuItem(TR.tr("menuBar.tools.debug.openCurrentEditFile"), null, null,
            TR.tr("menuBar.tools.debug.openCurrentEditFile.tooltip"), true, false, false, false);
    
    ////////// SETTINGS //////////
    
    public Menu settings = new Menu(TR.tr("menuBar.settings"));
    
    
    ////////// ABOUT / HELP //////////
    
    public Menu about = new Menu();
    
    Menu help = new Menu(TR.tr("menuBar.help"));
    MenuItem help1LoadDoc = new MenuItem(TR.tr("menuBar.help.loadDocumentation"));
    MenuItem help2GitHubIssue = new MenuItem(TR.tr("menuBar.help.gitHubIssue"));
    MenuItem help3Twitter = new MenuItem(TR.tr("menuBar.help.twitter"));
    MenuItem help4Website = new MenuItem(TR.tr("menuBar.help.website"));
    
    public MenuBar(){
        setup();
    }
    
    public static boolean isSystemMenuBarSupported(){
        return Main.isOSX();
    }
    
    public void setup(){
        if(isSystemMenuBarSupported()) setUseSystemMenuBar(true);
        
        ////////// FILE //////////
        
        file.getItems().addAll(file1Open, file2OpenDir, file3Clear, new SeparatorMenuItem(), file4Save, file5Delete, file6Close, new SeparatorMenuItem(), file7Export, file8ExportAll);
        
        ////////// TOOLS //////////
        
        tools3AddPages.getItems().add(new MenuItem());
        tools6ExportImportEdition.getItems().addAll(tools6ExportEdition1All, tools6ExportEdition2Grades, tools6ImportEdition1All, tools6ImportEdition2Grades);
        tools5SameNameEditions.getItems().add(tools5SameNameEditionsNull);
        tools9Debug.getItems().addAll(tools9Debug1OpenConsole, tools9Debug2OpenAppFolder, tools9Debug3OpenEditionFile);
        
        tools.getItems().addAll(tools1Convert, /*tools2QRCode,*/ tools3AddPages, new SeparatorMenuItem(), tools4DeleteAllEdits, tools5SameNameEditions, tools6ExportImportEdition, new SeparatorMenuItem(), tools8FullScreen, new SeparatorMenuItem(), tools9Debug);
        
        ////////// SETTINGS //////////
        
        Settings s = Main.settings;
        for(Field field : s.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(SettingObject.class)){
                try{
                    ((Setting) field.get(s)).setupMenuItem();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        settings.getItems().addAll(
                s.language.getMenuItem(), s.checkUpdates.getMenuItem(), s.sendStats.getMenuItem(),
                new SeparatorMenuItem(), s.restoreLastSession.getMenuItem(), s.defaultZoom.getMenuItem(), s.zoomAnimations.getMenuItem(), s.darkTheme.getMenuItem(),
                new SeparatorMenuItem(), s.autoSave.getMenuItem(), s.regularSave.getMenuItem(),
                new SeparatorMenuItem(), s.textAutoRemove.getMenuItem(), s.textOnlyStart.getMenuItem(), s.textSmall.getMenuItem(),
                new SeparatorMenuItem(), s.allowAutoTips.getMenuItem());
        
        ////////// HELP //////////
        
        if(!isSystemMenuBarSupported()){
            help1LoadDoc.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.INFO, "white", 0, 15, 15, colorAdjust));
            help2GitHubIssue.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.GITHUB, "white", 0, 15, 15, colorAdjust));
            help3Twitter.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TWITTER, "white", 0, 15, 15, colorAdjust));
            help4Website.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.GLOBE, "white", 0, 15, 15, colorAdjust));
        }
        help.getItems().addAll(help1LoadDoc, help2GitHubIssue, help3Twitter, help4Website);
        
        ////////// SETUP ITEMS WIDTH ///////////
        
        NodeMenuItem.setupMenu(file);
        NodeMenuItem.setupMenu(tools);
        NodeMenuItem.setupMenu(settings);
        
        ////////// FILE //////////
        
        file1Open.setOnAction((ActionEvent actionEvent) -> {
            
            File[] files = DialogBuilder.showPDFFilesDialog(true);
            if(files != null){
                MainWindow.filesTab.openFiles(files);
                if(files.length == 1){
                    MainWindow.mainScreen.openFile(files[0]);
                }
            }
        });
        file2OpenDir.setOnAction((ActionEvent actionEvent) -> {
            
            File directory = DialogBuilder.showDirectoryDialog(true);
            if(directory != null){
                MainWindow.filesTab.openFiles(new File[]{directory});
            }
        });
        file3Clear.setOnAction((ActionEvent actionEvent) -> {
            MainWindow.filesTab.clearFiles();
        });
        file4Save.setOnAction((ActionEvent actionEvent) -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                MainWindow.mainScreen.document.edition.save();
            }
        });
        file5Delete.setOnAction((ActionEvent e) -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                MainWindow.mainScreen.document.edition.clearEdit(true);
            }
        });
        file6Close.setOnAction((ActionEvent e) -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                MainWindow.mainScreen.closeFile(true);
            }
        });
        file7Export.setOnAction((ActionEvent actionEvent) -> {
            
            MainWindow.mainScreen.document.save();
            new ExportWindow(Collections.singletonList(MainWindow.mainScreen.document.getFile()));
            
        });
        file8ExportAll.setOnAction((ActionEvent actionEvent) -> {
            
            if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
            new ExportWindow(MainWindow.filesTab.files.getItems());
            
        });
        
        ////////// TOOLS //////////
        
        tools1Convert.setOnAction(e -> {
            new ConvertDocument();
        });
        
        tools3AddPages.setOnShowing(e -> {
            tools3AddPages.getItems().setAll(PageEditPane.getNewPageMenu(0, MainWindow.mainScreen.document.totalPages, isSystemMenuBarSupported()));
            PaneUtils.setMenuSize(tools3AddPages);
        });
        
        tools4DeleteAllEdits.setOnAction((ActionEvent e) -> {
            Alert dialog = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("dialog.deleteEdits.confirmation.title"));
            dialog.setHeaderText(TR.tr("dialog.deleteEdits.confirmation.header"));
            
            float yesButSize = FilesUtils.convertOctetToMo(FilesUtils.getSize(new File(Main.dataFolder + "editions")));
            float yesSize = 0L;
            for(File file : MainWindow.filesTab.files.getItems()){
                File editFile = Edition.getEditFile(file);
                yesSize += FilesUtils.getSize(editFile);
            }
            yesSize = FilesUtils.convertOctetToMo((long) yesSize);
            
            ButtonType cancel = new ButtonType(TR.tr("actions.no"), ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType yes = new ButtonType(TR.tr("actions.yes") + " (" + yesSize + "Mi" + TR.tr("data.byte") + ")", ButtonBar.ButtonData.OK_DONE);
            ButtonType yesBut = new ButtonType(TR.tr("dialog.deleteEdits.confirmation.buttons.deleteAll") + " (" + yesButSize + "Mi" + TR.tr("data.byte") + ")", ButtonBar.ButtonData.OTHER);
            dialog.getButtonTypes().setAll(yesBut, cancel, yes);
            
            Optional<ButtonType> option = dialog.showAndWait();
            float size = 0L;
            if(option.get() == yes){
                if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.edition.clearEdit(false);
                for(File file : MainWindow.filesTab.files.getItems()) Edition.getEditFile(file).delete();
                size = yesSize;
            }else if(option.get() == yesBut){
                if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.edition.clearEdit(false);
                for(File file : Objects.requireNonNull(new File(Main.dataFolder + "editions").listFiles()))
                    file.delete();
                size = yesButSize;
            }else return;
            
            Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("dialog.deleteEdits.completed.title"));
            alert.setHeaderText(TR.tr("dialog.deleteEdits.completed.header"));
            alert.setContentText(TR.tr("dialog.deleteEdits.completed.details", String.valueOf(size)));
            alert.show();
        });
        tools5SameNameEditions.setOnShowing((Event event) -> {
            tools5SameNameEditions.getItems().clear();
            int i = 0;
            for(Map.Entry<File, File> files : Edition.getEditFilesWithSameName(MainWindow.mainScreen.document.getFile()).entrySet()){
                
                MenuItem item = new MenuItem(files.getValue().getAbsolutePath());
                if(files.getValue().getParentFile() != null){
                    item.setText(files.getValue().getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + File.separator);
                }
                
                
                tools5SameNameEditions.getItems().add(item);
                item.setOnAction((ActionEvent actionEvent) -> {
                    Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.importEdit.confirm.title"));
                    dialog.setHeaderText(TR.tr("dialog.loadSameNameEdit.confirmation.header"));
                    
                    ButtonType cancel = new ButtonType(TR.tr("actions.no"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType yes = new ButtonType(TR.tr("actions.yes"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType yesAll = new ButtonType(TR.tr("dialog.loadSameNameEdit.confirmation.buttons.yesForAllSameFolder"), ButtonBar.ButtonData.OTHER);
                    dialog.getButtonTypes().setAll(cancel, yes, yesAll);
                    
                    Optional<ButtonType> option = dialog.showAndWait();
                    if(option.get() == yes){
                        if(MainWindow.mainScreen.hasDocument(true)){
                            
                            MainWindow.mainScreen.document.edition.clearEdit(false);
                            Edition.mergeEditFileWithEditFile(files.getKey(), Edition.getEditFile(MainWindow.mainScreen.document.getFile()));
                            MainWindow.mainScreen.document.loadEdition();
                        }
                    }else if(option.get() == yesAll){
                        if(MainWindow.mainScreen.hasDocument(true)){
                            
                            MainWindow.mainScreen.document.edition.clearEdit(false);
                            Edition.mergeEditFileWithEditFile(files.getKey(), Edition.getEditFile(MainWindow.mainScreen.document.getFile()));
                            MainWindow.mainScreen.document.loadEdition();
                            
                            for(File otherFileDest : MainWindow.filesTab.files.getItems()){
                                if(otherFileDest.getParentFile().getAbsolutePath().equals(MainWindow.mainScreen.document.getFile().getParentFile().getAbsolutePath()) && !otherFileDest.equals(MainWindow.mainScreen.document.getFile())){
                                    File fromEditFile = Edition.getEditFile(new File(files.getValue().getParentFile().getAbsolutePath() + "/" + otherFileDest.getName()));
                                    
                                    if(fromEditFile.exists()){
                                        Edition.mergeEditFileWithEditFile(fromEditFile, Edition.getEditFile(otherFileDest));
                                    }else{
                                        Alert alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.tr("dialog.loadSameNameEdit.fileNotFound.title"));
                                        alert.setHeaderText(TR.tr("dialog.loadSameNameEdit.fileNotFound.header", otherFileDest.getName(), FilesUtils.getPathReplacingUserHome(files.getValue().getParentFile())));
                                        ButtonType ok = new ButtonType(TR.tr("dialog.actionError.skip"), ButtonBar.ButtonData.OK_DONE);
                                        ButtonType cancelAll = new ButtonType(TR.tr("dialog.actionError.stopAll"), ButtonBar.ButtonData.CANCEL_CLOSE);
                                        alert.getButtonTypes().setAll(ok, cancelAll);
                                        
                                        Optional<ButtonType> option2 = alert.showAndWait();
                                        if(option2.get() == cancelAll) return;
                                    }
                                }
                            }
                        }
                    }
                });
                i++;
            }
            if(i == 0) tools5SameNameEditions.getItems().add(tools5SameNameEditionsNull);
            PaneUtils.setMenuSize(tools5SameNameEditions);
        });
        
        tools6ExportEdition1All.setOnAction((e) -> EditionExporter.showExportDialog(false));
        tools6ExportEdition2Grades.setOnAction((e) -> EditionExporter.showExportDialog(true));
        tools6ImportEdition1All.setOnAction((e) -> EditionExporter.showImportDialog(false));
        tools6ImportEdition2Grades.setOnAction((e) -> EditionExporter.showImportDialog(true));
        
        tools8FullScreen.setOnAction((e) -> {
            Main.window.setFullScreen(!Main.window.isFullScreen());
        });
        
        tools9Debug1OpenConsole.setOnAction((e) -> new LogWindow());
        tools9Debug2OpenAppFolder.setOnAction((e) -> PlatformUtils.openDirectory(Main.dataFolder));
        tools9Debug3OpenEditionFile.setOnAction((e) -> PlatformUtils.openFile(Edition.getEditFile(MainWindow.mainScreen.document.getFile()).getAbsolutePath()));
        
        
        ////////// SETTINGS //////////
        
        s.language.getMenuItem().setOnAction(e -> {
            Main.showLanguageWindow(false);
        });
        s.defaultZoom.getMenuItem().setOnAction((ActionEvent actionEvent) -> {
            
            List<Integer> choices = new ArrayList<>(Arrays.asList(50, 70, 80, 90, 100, 110, 120, 140, 160, 180, 200, 230, 250, 280, 300));
            ChoiceDialog<Integer> dialog = DialogBuilder.getChoiceDialog(Main.settings.defaultZoom.getValue(), choices);
            
            dialog.setTitle(TR.tr("dialog.defaultZoomSetting.title"));
            dialog.setHeaderText(TR.tr("dialog.defaultZoomSetting.header"));
            dialog.setContentText(TR.tr("dialog.defaultZoomSetting.details"));
            
            Optional<Integer> newZoom = dialog.showAndWait();
            if(!newZoom.isEmpty()) Main.settings.defaultZoom.setValue(newZoom.get());
        });
        s.regularSave.getMenuItem().setOnAction((ActionEvent actionEvent) -> {
            
            Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.regularSaving.title"));
            
            HBox pane = new HBox();
            ComboBox<Integer> combo = new ComboBox<>(FXCollections.observableArrayList(1, 5, 10, 15, 20, 30, 45, 60));
            combo.getSelectionModel().select(Main.settings.regularSave.getValue() == -1 ? (Integer) 5 : Main.settings.regularSave.getValue());
            combo.setStyle("-fx-padding-left: 20px;");
            CheckBox activated = new CheckBox(TR.tr("actions.enable"));
            activated.setSelected(Main.settings.regularSave.getValue() != -1);
            pane.getChildren().add(0, activated);
            pane.getChildren().add(1, combo);
            HBox.setMargin(activated, new Insets(5, 0, 0, 10));
            HBox.setMargin(combo, new Insets(0, 0, 0, 30));
            
            combo.disableProperty().bind(activated.selectedProperty().not());
            dialog.setHeaderText(TR.tr("dialog.regularSaving.header"));
            dialog.getDialogPane().setContent(pane);
            
            Optional<ButtonType> option = dialog.showAndWait();
            if(option.get() == ButtonType.OK){
                s.regularSave.setRadioSelected(activated.isSelected());
                if(activated.isSelected()) s.regularSave.setValue(combo.getSelectionModel().getSelectedItem());
                else s.regularSave.setValue(-1);
            }
        });
        
        ////////// ABOUT / HELP //////////
        
        if(isSystemMenuBarSupported()){
            about.setText(TR.tr("menuBar.about"));
            MenuItem triggerItem = new MenuItem(TR.tr("menuBar.about.openAboutWindow"));
            about.getItems().add(triggerItem);
            triggerItem.setOnAction(e -> Main.showAboutWindow());
        }else{
            Label name = new Label(TR.tr("menuBar.about"));
            name.setAlignment(Pos.CENTER_LEFT);
            name.setOnMouseClicked(e -> Main.showAboutWindow());
            about.setGraphic(name);
        }
        
        help1LoadDoc.setOnAction((ActionEvent actionEvent) -> MainWindow.mainScreen.openFile(TR.getDocFile()));
        help2GitHubIssue.setOnAction((ActionEvent actionEvent) -> {
            try{
                Desktop.getDesktop().browse(new URI("https://github.com/themsou/PDF4Teachers/issues/new"));
            }catch(IOException | URISyntaxException e){
                e.printStackTrace();
            }
        });
        help3Twitter.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://twitter.com/PDF4Teachers"));
        help4Website.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://pdf4teachers.org"));
        
        ////////// ABOUT / HELP //////////
        
        // UI Style
        setStyle("");
        StyleManager.putStyle(this, Style.ACCENT);
        getMenus().addAll(file, tools, settings, help, about);
        
        if(!isSystemMenuBarSupported()){
            for(Menu menu : getMenus()){
                menu.setStyle("-fx-padding: 5 7 5 7;");
            }
        }

        if(Main.isOSX()){
            MenuToolkit tk = MenuToolkit.toolkit(TR.locale);
            Menu defaultApplicationMenu = new Menu(Main.APP_NAME, null,
                    tk.createAboutMenuItem(Main.APP_NAME, e -> Main.showAboutWindow()), new SeparatorMenuItem(),
                    tk.createHideMenuItem(Main.APP_NAME),
                    tk.createHideOthersMenuItem(),
                    tk.createUnhideAllMenuItem(), new SeparatorMenuItem(),
                    tk.createQuitMenuItem(Main.APP_NAME));
            tk.setApplicationMenu(defaultApplicationMenu);

            // TODO : check this works
            Menu docMenu = new Menu("test");
            docMenu.getItems().addAll(new MenuItem("item"));
            tk.setDockIconMenu(docMenu);
        }
        
    }
    
    public static Menu createSubMenu(String name, String image, String toolTip, boolean disableIfNoDoc){
        
        if(isSystemMenuBarSupported()){
            Menu menu = new Menu(name);
            //if(imgName != null)menu.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/" + imgName + ".png")+"", 0, 0));
            
            if(disableIfNoDoc){
                menu.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
            }
            
            return menu;
        }else{
            Menu menu = new Menu();
            HBox pane = new HBox();
            
            Label text = new Label(name);
            
            if(image != null){
                if(image.length() >= 30){
                    pane.getChildren().add(SVGPathIcons.generateImage(image, "white", 0, 16, 16, colorAdjust));
                }else{
                    if(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") == null)
                        System.err.println("MenuBar image " + image + " does not exist");
                    else
                        pane.getChildren().add(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") + "", 0, 0, colorAdjust));
                }
                
                text.setStyle("-fx-font-size: 13; -fx-padding: 0 0 0 8;"); // top - right - bottom - left
            }else{
                text.setStyle("-fx-font-size: 13; -fx-padding: 2 0 2 0;");
            }
            pane.getChildren().add(text);
            
            if(disableIfNoDoc){
                menu.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
            }
            
            Tooltip toolTipUI = PaneUtils.genWrappedToolTip(toolTip);
            toolTipUI.setShowDuration(Duration.INDEFINITE);
            Tooltip.install(pane, toolTipUI);
            
            menu.setGraphic(pane);
            return menu;
        }
    }
    
    public static MenuItem createRadioMenuItem(String text, String image, String toolTip, boolean autoUpdate){
        
        if(isSystemMenuBarSupported()){
            RadioMenuItem menuItem = new RadioMenuItem(text);
            //if(imgName != null) menuItem.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));
            
            //OSX selects radioMenuItems upon click, but doesn't unselect it on click :
            AtomicBoolean selected = new AtomicBoolean(false);
            menuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    selected.set(newValue);
                });
            });
            menuItem.setOnAction((e) -> {
                menuItem.setSelected(!selected.get());
            });
            
            return menuItem;
            
        }else{
            NodeRadioMenuItem menuItem = new NodeRadioMenuItem(new HBox(), text + "      ", true, autoUpdate);
            
            
            if(image != null){
                if(image.length() >= 30){
                    menuItem.setImage(SVGPathIcons.generateImage(image, "white", 0, 16, 16, colorAdjust));
                }else{
                    if(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") == null)
                        System.err.println("MenuBar image " + image + " does not exist");
                    else
                        menuItem.setImage(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") + "", 0, 0, colorAdjust));
                }
                
            }
            if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);
            
            return menuItem;
        }
        
        
    }
    
    public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, boolean leftMargin){
        return createMenuItem(text, imgName, keyCombinaison, toolTip, disableIfNoDoc, disableIfNoList, leftMargin, true);
    }
    
    public static MenuItem createMenuItem(String text, String image, KeyCombination keyCombinaison, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, boolean leftMargin, boolean fat){
        if(isSystemMenuBarSupported()){
            MenuItem menuItem = new MenuItem(text);
            //if(imgName != null) menuItem.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));
            if(keyCombinaison != null) menuItem.setAccelerator(keyCombinaison);
            if(disableIfNoDoc){
                menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
            }
            if(disableIfNoList){
                menuItem.disableProperty().bind(Bindings.size(MainWindow.filesTab.getOpenedFiles()).isEqualTo(0));
            }
            return menuItem;
        }else{
            NodeMenuItem menuItem = new NodeMenuItem(new HBox(), text + "         ", fat);
            
            if(image != null){
                if(image.length() >= 30){
                    menuItem.setImage(SVGPathIcons.generateImage(image, "white", 0, 16, 16, colorAdjust));
                }else{
                    if(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") == null)
                        System.err.println("MenuBar image " + image + " does not exist");
                    else
                        menuItem.setImage(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") + "", 0, 0, colorAdjust));
                }
                
            }
            if(keyCombinaison != null) menuItem.setKeyCombinaison(keyCombinaison);
            if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);
            if(leftMargin) menuItem.setFalseLeftData();
            
            if(disableIfNoDoc){
                menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
            }
            if(disableIfNoList){
                menuItem.disableProperty().bind(Bindings.size(MainWindow.filesTab.getOpenedFiles()).isEqualTo(0));
            }
            
            return menuItem;
        }
    }
    
    public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip){
        return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, false);
    }
    
    public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean leftMargin){
        return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, leftMargin);
    }
}