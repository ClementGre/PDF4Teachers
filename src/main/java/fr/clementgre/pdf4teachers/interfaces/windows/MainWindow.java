package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.interfaces.AutoHideNotificationPane;
import fr.clementgre.pdf4teachers.interfaces.Macro;
import fr.clementgre.pdf4teachers.interfaces.OSXTouchBarManager;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguagesUpdater;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.FooterBar;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.files.FileTab;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTab;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.AlertIconType;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import jfxtras.styles.jmetro.JMetro;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainWindow extends Stage{
    
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
    public static PaintTab paintTab;
    
    
    public static DecimalFormat format;
    public static DecimalFormat twoDigFormat;
    
    public OSXTouchBarManager osxTouchBarManager;
    public JMetro jMetro;
    
    public static double TEMP_SCALE = 1;
    
    public MainWindow(){
        
        root = new BorderPane();
        notificationPane = new AutoHideNotificationPane(root);
        
        Scene scene = new Scene(notificationPane);
        scene.setFill(Color.TRANSPARENT);
        loadDimensions();
        setupDecimalFormat();
        setTitle(TR.tr("mainWindow.title.noDocument"));
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        
        setMinWidth(700);
        setMinHeight(393);
        setResizable(true);
        setScene(scene);
        
        new Macro(scene);
        
        setOnCloseRequest(e -> {
            userData.save();
            if(!mainScreen.closeFile(!Main.settings.autoSave.getValue())){
                e.consume();
                return;
            }
            
            Main.params = new ArrayList<>();
            AutoTipsManager.hideAll();
            if(paintTab.galleryWindow != null) paintTab.galleryWindow.close();
            
            LanguagesUpdater.backgroundStats(() -> {
                System.out.println("Closing PDF4Teachers");
                System.exit(0);
            });
            
        });
        
        widthProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });
        heightProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });
        xProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });
        yProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });
        
    }
    
    public void setup(){
        
        //		SETUPS
        
        mainPane = new SplitPane();
        
        leftBar = new SideBar(true);
        rightBar = new SideBar(false);
        
        mainScreen = new MainScreen();
        footerBar = new FooterBar();
        
        filesTab = new FileTab();
        textTab = new TextTab();
        gradeTab = new GradeTab();
        try{
            FXMLLoader.load(getClass().getResource("/fxml/PaintTab.fxml"));
        }catch(IOException e){
            e.printStackTrace();
        }
        
        menuBar = new MenuBar();
        mainScreen.repaint();

//		PANELS
        
        show();
        requestFocus();
        
        mainPane.getItems().addAll(leftBar, mainScreen, rightBar);
        SideBar.setupDividers(mainPane);
        SideBar.loadBarsOrganization();
        
        root.setCenter(mainPane);
        root.setTop(menuBar);
        root.setBottom(footerBar);
        
        Main.window.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) root.setBottom(null);
            else root.setBottom(footerBar);
        });
        root.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.TAB){
                if(MainWindow.textTab.isSelected() && !MainWindow.gradeTab.isSelected()) MainWindow.gradeTab.select();
                if(!MainWindow.textTab.isSelected() && MainWindow.gradeTab.isSelected()) MainWindow.textTab.select();
                e.consume();
            }
        });

//		SHOWING
        
        setupDesktopEvents();
        updateStyle();
        mainScreen.repaint();
        //osxTouchBarManager = new OSXTouchBarManager(this);

//      OPEN DOC WITH PARAMS OR Auto Documentation
        
        List<File> toOpenFiles = new ArrayList<>();
        for(String param : Main.params){
            if(new File(param).exists()){
                toOpenFiles.add(new File(param));
            }
        }
        filesTab.openFiles(toOpenFiles);
        
        if(Main.firstLaunch || !Main.settings.getSettingsVersion().equals(Main.VERSION)){
            mainScreen.openFile(TR.getDocFile());
        }else if(toOpenFiles.size() >= 1){
            if(FilesUtils.getExtension(toOpenFiles.get(0).getName()).equalsIgnoreCase("pdf")){
                mainScreen.openFile(toOpenFiles.get(0));
            }
        }


//      CHECK UPDATES
        new Thread(() -> {
            
            userData = new UserData();
            
            if(UpdateWindow.checkVersion()){
                Platform.runLater(() -> {
                    if(UpdateWindow.newVersion){
                        if(MenuBar.isSystemMenuBarSupported()){
                            menuBar.about.setText(TR.tr("menuBar.about") + " (" + TR.tr("aboutWindow.version.update.available.short") + ")");
                        }else{
                            menuBar.about.setStyle(menuBar.about.getStyle() + " -fx-background-color: #d6a600;");
                            Tooltip.install(menuBar.about.getGraphic(), PaneUtils.genToolTip(TR.tr("aboutWindow.version.update.available")));
                        }
                        
                        if(Main.settings.checkUpdates.getValue()){
                            new UpdateWindow();
                        }
                    }
                });
            }
        }).start();
        
    }

    public void updateStyle(){
        jMetro = StyleManager.putStyle(getScene(), Style.DEFAULT, jMetro);
    }
    
    public void restart(){
        TR.updateLocale();
        
        userData.save();
        if(MainWindow.mainScreen.closeFile(true)){
            close();
            Platform.runLater(Main::startMainWindow);
        }
    }
    
    public void loadDimensions(){
        
        setMaximized(Main.syncUserData.mainWindowMaximized);

        if(Main.syncUserData.mainWindowX != -1) setX(Main.syncUserData.mainWindowX);
        if(Main.syncUserData.mainWindowY != -1) setY(Main.syncUserData.mainWindowY);
        setWidth(Main.syncUserData.mainWindowWidth);
        setHeight(Main.syncUserData.mainWindowHeight);
        
        preventWindowOverflowScreen(this);
    }
    
    private boolean saveDimensionRunning = false;
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
        double sw = Main.SCREEN_BOUNDS.getWidth();
        double sh = Main.SCREEN_BOUNDS.getHeight();
        
        double x = getX() + getWidth() / 2 - w / 2;
        double y = getY() + getHeight() / 2 - h / 2;
        
        if(x > sw - w) x = sw - w;
        if(y > sh - h) y = sh - h;
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        
        window.setX(x);
        window.setY(y);
    }
    public static void preventWindowOverflowScreen(Stage window){
        double w = window.getWidth();
        double h = window.getHeight();
        double x = window.getX();
        double y = window.getY();
    
        double sw = Main.SCREEN_VISUAL_BOUNDS.getWidth();
        double sh = Main.SCREEN_VISUAL_BOUNDS.getHeight();
        if(w > sw) w = sw;
        if(h > sh) h = sh;
        if(x != -1){
            if(x + w > sw) window.setX(0);
        }
        if(y != -1){
            if(y + h > sh) window.setY(0);
        }
        if(window.getMinWidth() > w) window.setMinWidth(w);
        if(window.getMinHeight() > h) window.setMinHeight(h);
        window.setWidth(w);
        window.setHeight(h);
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
    
    public void setupDesktopEvents(){
        
        if(Desktop.isDesktopSupported()){
            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_ABOUT)){
                Desktop.getDesktop().setAboutHandler(e -> {
                    Platform.runLater(Main::showAboutWindow);
                });
            }
            
            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_URI)){
                Desktop.getDesktop().setOpenURIHandler(e -> {
                    System.out.println(e.getURI());
                    Platform.runLater(() -> {
                        File file = new File(e.getURI());
                        if(file.exists()){
                            MainWindow.filesTab.openFiles(new File[]{file});
                            MainWindow.mainScreen.openFile(file);
                        }
                    });
                });
            }
            
            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)){
                Desktop.getDesktop().setOpenFileHandler(e -> {
                    Platform.runLater(() -> {
                        System.out.println(e.getFiles().get(0).getAbsolutePath());
                        MainWindow.filesTab.openFiles((File[]) e.getFiles().toArray());
                        if(e.getFiles().size() == 1) MainWindow.mainScreen.openFile(e.getFiles().get(0));
                    });
                    
                });
            }
            
            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES)){
                Desktop.getDesktop().setPreferencesHandler(e -> {
                    Platform.runLater(() -> {
                        menuBar.settings.fire();
                    });
                });
            }
            
        }
        
    }
    
    private static void setupDecimalFormat(){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        char separator = TR.tr("chars.decimalSeparator").charAt(0);
        if(separator == 'D') separator = ',';
        else if(separator != ',' && separator != '.') separator = '.';
        symbols.setDecimalSeparator(separator);
        format = new DecimalFormat("0.####", symbols);
        twoDigFormat = new DecimalFormat("0.##", symbols);
    }
    
}
