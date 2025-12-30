/*
 * Copyright (c) 2019-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertWindow;
import fr.clementgre.pdf4teachers.interfaces.AutoHideNotificationPane;
import fr.clementgre.pdf4teachers.interfaces.KeyboardShortcuts;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguagesUpdater;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.FooterBar;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.files.FileTab;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.SkillsTab;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTab;
import fr.clementgre.pdf4teachers.panel.sidebar.toc.TocTab;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.locking.LockManager;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import jfxtras.styles.jmetro.JMetro;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainWindow extends Stage {
    
    public static UserData userData;
    
    public static AutoHideNotificationPane notificationPane;
    public static BorderPane root;
    public static SplitPane mainPane;
    
    public static MainScreen mainScreen;
    public static FooterBar footerBar;
    public static MenuBar menuBar;
    
    public static SideBar leftBar;
    public static SideBar rightBar;
    
    public static FileTab filesTab;
    public static TextTab textTab;
    public static GradeTab gradeTab;
    public static SkillsTab skillsTab;
    public static PaintTab paintTab;
    public static TocTab tocTab;
    
    public static KeyboardShortcuts keyboardShortcuts;
    
    public static DecimalFormat fourDigFormat;
    public static DecimalFormat gradesDigFormat;
    public static DecimalFormat twoDigFormat;
    
    public JMetro jMetro;
    
    public MainWindow(){
        
        root = new BorderPane();
        notificationPane = new AutoHideNotificationPane(root);
        
        Scene scene = new Scene(notificationPane);
        scene.setFill(Color.TRANSPARENT);
        setupDecimalFormat();
        setTitle(TR.tr("mainWindow.title.noDocument"));
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        
        setMinWidth(700 * Main.settings.zoom.getValue());
        setMinHeight(393 * Main.settings.zoom.getValue());
        setResizable(true);
        setScene(scene);
        
        keyboardShortcuts = new KeyboardShortcuts(scene);
        
        setOnCloseRequest(e -> {
            if(!requestCloseApp()) e.consume();
        });
    }
    
    public static boolean requestCloseApp(){
        Log.i("Received close request");
    
        userData.save();
        if(!mainScreen.closeFile(!Main.settings.autoSave.getValue(), false, false)) return false;
        
        // At this point, it is sure the app will close.
        LockManager.onCloseApp();
        Main.window.close();
        if(paintTab.galleryWindow != null) paintTab.galleryWindow.close();
        AutoTipsManager.hideAll();
    
        Log.i("Sending Statistics...");
        LanguagesUpdater.backgroundStats(() -> {
            Log.i("Closing PDF4Teachers");
            Platform.exit();
            System.exit(0);
        });
        return true;
    }
    
    public boolean doOpenDocumentation; // Will be used by userData to auto load the documentation.
    public void setup(boolean openDocumentation){
        doOpenDocumentation = openDocumentation;
        // WINDOW DIMENSIONS

        loadDimensions();
        widthProperty().addListener((observable, oldValue, newValue) -> saveDimensions());
        heightProperty().addListener((observable, oldValue, newValue) -> saveDimensions());
        xProperty().addListener((observable, oldValue, newValue) -> saveDimensions());
        yProperty().addListener((observable, oldValue, newValue) -> saveDimensions());

        //      TRANSLATIONS

        ShapesGridView.setupTranslations();
        ConvertWindow.setupTranslations();
        
        //		SETUPS
    
        mainPane = new SplitPane();
        mainPane.setPadding(new Insets(0));
        
        leftBar = new SideBar(true);
        rightBar = new SideBar(false);
        
        mainScreen = new MainScreen();
        footerBar = new FooterBar();
        
        filesTab = new FileTab();
        textTab = new TextTab();
        gradeTab = new GradeTab();
        skillsTab = new SkillsTab();
        tocTab = new TocTab();
        try{
            FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/PaintTab.fxml")));
        }catch(IOException e){
            Log.eNotified(e);
        }
        
        menuBar = new MenuBar();
        Main.settings.zoom.valueProperty().addListener((observable, oldValue, newValue) -> {
            menuBar = new MenuBar();
            root.setTop(menuBar);
        });
        mainScreen.repaint();
        
        //		PANELS
        
        show();
        requestFocus();
        
        mainPane.getItems().addAll(leftBar, mainScreen, rightBar);
        
        root.setCenter(mainPane);
        root.setTop(menuBar);
        root.setBottom(footerBar);
        
        //		SHOWING
        
        updateStyle();
        mainScreen.repaint();
        
        //      LOAD TABS
        SideBar.loadBarsOrganization();
        
        //      CHECK UPDATES
        new Thread(() -> {
            userData = new UserData();
            
            if(UpdateWindow.checkVersion()){
                Platform.runLater(() -> {
                    if(UpdateWindow.newVersion){
                        if(!MenuBar.isSystemMenuBarSupported()){
                            menuBar.about.setEmptyMenuStyle("-fx-background-color: #d6a600;");
                            Tooltip.install(menuBar.about.getGraphic(), PaneUtils.genToolTip(TR.tr("aboutWindow.version.update.available")));
                        }
                        
                        if(Main.settings.checkUpdates.getValue()){
                            new UpdateWindow();
                        }
                    }
                });
            }
        }).start();
        
        // Notifications
    
        if(PlatformUtils.isMacAArch64() && !PlatformUtils.isJDKMacAArch64()){
            showNotification(AlertIconType.WARNING, TR.tr("macos.aarch64.warning"), -1);
        }
        if(Main.mode == Main.Mode.PRE_RELEASE){
            showNotification(AlertIconType.INFORMATION, TR.tr("pre-release.startInfo"), 30);
        }
        
    }
    
    public void updateStyle(){
        jMetro = StyleManager.putStyle(getScene(), Style.DEFAULT, jMetro);
    }
    
    // When replaceDoc == true, the loaded file will be replaced by the new language doc,
    // if the loaded file path is docFileAbsolutePath
    public void restart(boolean replaceDoc, String docFileAbsolutePath){
        userData.save();
        
        boolean openDoc = replaceDoc
                && mainScreen.hasDocument(false)
                && mainScreen.document.getFile().getAbsolutePath().equals(docFileAbsolutePath);
        
        if(MainWindow.mainScreen.closeFile(true, false, false)){
            if(paintTab.galleryWindow != null) paintTab.galleryWindow.close();
            Main.params = new ArrayList<>();
            TR.updateLocale();
            close();
            Platform.runLater(() -> Main.startMainWindow(openDoc));
        }
    }
    
    public void loadDimensions(){
        
        setMaximized(Main.syncUserData.mainWindowMaximized);
        
        if(Main.syncUserData.mainWindowX != -1) setX(Main.syncUserData.mainWindowX);
        if(Main.syncUserData.mainWindowY != -1) setY(Main.syncUserData.mainWindowY);
        setWidth(Main.syncUserData.mainWindowWidth);
        setHeight(Main.syncUserData.mainWindowHeight);
        
        preventStageOverflowScreen(this);
    }
    
    private boolean saveDimensionRunning;
    public void saveDimensions(){
        if(saveDimensionRunning) return;
        
        saveDimensionRunning = true;
        PlatformUtils.runLaterOnUIThread(1000, () -> {
            saveDimensionRunning = false;
            Main.syncUserData.mainWindowWidth = (long) getWidth();
            Main.syncUserData.mainWindowHeight = (long) getHeight();
            Main.syncUserData.mainWindowX = (long) getX();
            Main.syncUserData.mainWindowY = (long) getY();
            Main.syncUserData.mainWindowMaximized = isMaximized();
        });
        
    }
    
    public void centerWindowIntoMe(Window window){
        centerWindowIntoMe(window, window.getWidth(), window.getHeight());
    }
    public void centerWindowIntoMe(Window window, double w, double h){
        double x = getX() + getWidth() / 2 - w / 2;
        double y = getY() + getHeight() / 2 - h / 2;
        
        window.setX(x);
        window.setY(y);
    }
    public static void preventStageOverflowScreen(Stage window){
        preventStageOverflowScreen(window, getScreen(window).getVisualBounds());
    }
    public static void preventStageOverflowScreen(Stage window, Rectangle2D bounds){
        double w = window.getWidth();
        double h = window.getHeight();
        double x = window.getX();
        double y = window.getY();

        double minX = bounds.getMinX();
        double maxX = bounds.getMaxX();
        double minY = bounds.getMinY();
        double maxY = bounds.getMaxY();

        // Check in top right coordinates and move window
        if(x+w > maxX) x -= x+w - maxX;
        if(y+h > maxY) y -= y+h - maxY;

        // Check in bottom right coordinates and move window
        if(x < minX) x = minX;
        if(y < minY) y = minY;

        // Check in top right coordinates and resize window
        if(x+w > maxX) w -= x+w - maxX;
        if(y+h > maxY) h -= y+h - maxY;

        if(window.getMinWidth() > w) window.setMinWidth(w);
        if(window.getMinHeight() > h) window.setMinHeight(h);
        window.setWidth(w);
        window.setHeight(h);
        window.setX(x);
        window.setY(y);
    }
    public static Screen getScreen(){
        return getScreen(Main.window);
    }
    public static Screen getScreen(Window window){
        ObservableList<Screen> screens = Screen.getScreensForRectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight());
        if(screens.isEmpty()){
            if(window == Main.window) return Screen.getPrimary();
            else return getScreen();
        }else return screens.getFirst();
    }
    
    /*
     * @Param autoHide in seconds
     */
    public static void showNotification(AlertIconType type, String text, int autoHide){
        notificationPane.addToPending(text, type, autoHide);
    }
    public static void showNotificationNow(AlertIconType type, String text, int autoHide){
        notificationPane.showNow(text, type, autoHide);
    }
    
    private static void setupDecimalFormat(){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setDecimalSeparator(TR.tr("chars.decimalSeparator").charAt(0) == ',' ? ',' : '.');
        
        fourDigFormat = new DecimalFormat("0.####", symbols);
        twoDigFormat = new DecimalFormat("0.##", symbols);
        
        gradesDigFormat = new DecimalFormat("0.###", symbols);
        gradesDigFormat.setMaximumIntegerDigits(4);
    }
    
}
