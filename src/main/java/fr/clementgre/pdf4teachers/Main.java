/*
 * Copyright (c) 2019-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers;

import fr.clementgre.pdf4teachers.datasaving.SyncUserData;
import fr.clementgre.pdf4teachers.datasaving.settings.Settings;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguageWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.interfaces.windows.log.LogLevel;
import fr.clementgre.pdf4teachers.interfaces.windows.log.LogsManager;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.VersionIds;
import fr.clementgre.pdf4teachers.utils.fonts.AppFontsLoader;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.locking.LockManager;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Main extends Application {
    
    public static MainWindow window;
    
    public static Settings settings;
    public static SyncUserData syncUserData;
    
    public static HostServices hostServices;
    
    public static String dataFolder = System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator;
    public static final String APP_NAME = "PDF4Teachers";
    public static final String APP_ID = "fr.clementgre.pdf4teachers.applicationid";
    
    public enum Mode { DEV, SNAPSHOT, PRE_RELEASE, RELEASE }
    
    /********** Version parameters **********/
    public static final Mode mode = Mode.DEV;
    public static final int VERSION_ID = VersionIds.DV1_1_4_1;
    public static final String VERSION = getVersionName("1.4.1", 1);
    public static LogLevel logLevel = getLogLevel();
    /****************************************/
    
    public static final boolean TRANSLATIONS_IN_CODE = mode == Mode.DEV;
    public static final boolean COPY_TRANSLATIONS_AT_START = false;
    public static boolean firstLaunch;
    public static List<String> params;
    
    public static DecimalFormatSymbols baseDecimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
    
    static{
        baseDecimalFormatSymbols.setDecimalSeparator('.');
    }
    public static DecimalFormat fourDigENFormat = new DecimalFormat("0.####", baseDecimalFormatSymbols);
    public static DecimalFormat oneDigENFormat = new DecimalFormat("0.#", baseDecimalFormatSymbols);
    
    public static final DataFormat INTERNAL_FORMAT = new DataFormat("application/pdf4teachers-internal-format; class=java.lang.String");
    
    
    static {
        /*if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
            Desktop.getDesktop().setOpenFileHandler(event -> {
                for (File file : event.getFiles()) {
                    Log.d("Open file: " + file.getAbsolutePath());
                }
                
                final String searchTerm = event.getSearchTerm();
                if (searchTerm != null) {
                    Log.d("Search term: " + searchTerm);
                }
            });
        }*/
    }
    
    public static void main(String[] args){
        LogsManager.copyLogs();
        
        Log.i("Starting PDF4Teachers " + VERSION + " | Java " + System.getProperty("java.version") + " on JFX " + System.getProperty("javafx.runtime.version") + " on " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
        if(PlatformUtils.isMacAArch64() && !PlatformUtils.isJDKMacAArch64()){
            Log.w("You are using a build for amd64 on a AArch64 Mac. This will slow down the programm.");
        }
        Log.i("Run mode: " + mode.name().toLowerCase() + " | Log level: " + logLevel.name().toLowerCase());
        
        ImageIO.scanForPlugins();
        if(!LockManager.registerInstance(List.of(args))){
            Platform.exit();
            System.exit(0);
            return;
        }
        
        ///// START APP /////
        launch(args);
    }
    
    @Override
    public void start(Stage stage){
        try{
            setup();
        }catch(Exception e){
            Log.e(e);
        }
        
    }
    
    private void setup(){
        // Check double instance
        params = getParameters().getRaw();
        if(LockManager.FAKE_OPEN_FILE) params = List.of("C:\\Users\\Clement\\Documents\\PDF\\Kev.pdf");
    
        // Define important vars
        dataFolder = PlatformUtils.getDataFolder();
        firstLaunch = !new File(dataFolder + File.separator + "settings.yml").exists();
        hostServices = getHostServices();
    
        // Read params
        if(!getParameters().getRaw().isEmpty() || !getParameters().getNamed().isEmpty()){
            Log.d("Starting with parameters: \nRaw: " + getParameters().getRaw().toString()
                    + "\n Unnamed: " + getParameters().getUnnamed().toString()
                    + "\n Named: " + getParameters().getNamed().toString());
        }
    
        // Data loading
        settings = new Settings();
        syncUserData = new SyncUserData();
    
        // Setups
        TR.setup();
        StyleManager.setup();
        AutoTipsManager.setup();
        ImageUtils.setupListeners();
        FontUtils.setup();
        AppFontsLoader.loadAppFonts();
        
        // Show app
        if(languageAsk()){
            startMainWindowAuto();
        }
    }
    
    public boolean languageAsk(){
        if(settings.language.getValue().isEmpty()){
            String language = TR.getLanguageFromComputerLanguage();
            if(language != null){
                Main.settings.language.setValue(language);
                Main.settings.saveSettings();
                TR.updateLocale();
            }else{
                LanguageWindow.showLanguageWindow(true);
                return false;
            }
        }
        return true;
    }
    
    public static void showAboutWindow(){
        try{
            FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/fxml/AboutWindow.fxml")));
        }catch(IOException e){Log.eNotified(e);}
    }
    
    public static void startMainWindow(boolean openDocumentation){
        window = new MainWindow();
        window.setup(openDocumentation);
    }
    public static void startMainWindowAuto(){
        window = new MainWindow();
        window.setup(firstLaunch || settings.hasVersionChanged());
    }
    
    private static String getVersionName(String version, int id){
        return switch (mode) {
            case DEV -> version + "-dv" + id;
            case SNAPSHOT -> version + "-sn" + id;
            case PRE_RELEASE -> version + "-pre" + id;
            default -> version;
        };
    }
    private static LogLevel getLogLevel(){
        return switch (mode) {
            case DEV -> LogLevel.TRACE;
            case SNAPSHOT -> LogLevel.DEBUG;
            case PRE_RELEASE -> LogLevel.DEBUG;
            default -> LogLevel.INFO;
        };
    }
    
}
