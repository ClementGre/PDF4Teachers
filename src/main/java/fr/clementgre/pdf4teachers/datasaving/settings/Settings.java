/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.settings;

import de.jangassen.MenuToolkit;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeView;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.io.File;
import java.lang.reflect.Field;

public class Settings {
    
    private int settingsVersionID = 0;
    private String settingsVersionCode = "";
    
    
    @SettingObject
    public StringSetting language = new StringSetting("", false, SVGPathIcons.GLOBE, "language",
            "settings.language.title", "");
    
    @SettingsGroup(title = "settings.group.accessibility")
    public Setting<?>[] accessibilityGroup = {language}; // zoom/scale
    
    
    @SettingObject
    public BooleanSetting darkTheme = new BooleanSetting(!Main.isOSX() || MenuToolkit.toolkit().systemUsesDarkMode(), true, SVGPathIcons.SUN, "darkTheme",
            "settings.darkTheme.title", "");
    @SettingObject
    public BooleanSetting animations = new BooleanSetting(true, true, SVGPathIcons.LAYERS, "animations",
            "settings.animations.title", "settings.animations.tooltip");
    @SettingObject
    public BooleanSetting restoreLastSession = new BooleanSetting(true, true, SVGPathIcons.REDO, "restoreLastSession",
            "settings.restoreLastSession.title", "settings.restoreLastSession.tooltip");
    
    @SettingsGroup(title = "settings.group.ergonomics")
    public Setting<?>[] ergonomicsGroup = {darkTheme, restoreLastSession, animations};
    
    
    @SettingObject
    public BooleanSetting checkUpdates = new BooleanSetting(true, true, SVGPathIcons.WIFI, "checkUpdates",
            "settings.checkUpdates.title", "settings.checkUpdates.tooltip");
    @SettingObject
    public BooleanSetting sendStats = new BooleanSetting(true, true, SVGPathIcons.STATS, "sendStatistics",
            "settings.sendStatistics.title", "settings.sendStatistics.tooltip");
    
    @SettingsGroup(title = "settings.group.network")
    public Setting<?>[] networkGroup = {checkUpdates, sendStats};
    
    
    @SettingObject
    public BooleanSetting autoSave = new BooleanSetting(true, true, SVGPathIcons.SAVE, "autoSave",
            "settings.autoSave.title", "settings.autoSave.tooltip");
    @SettingObject
    public IntSetting regularSave = new IntSetting(-1, true, 1, 60, 5, true, false, SVGPathIcons.CLOCK, "regularSave",
            "settings.regularSave.title", "settings.regularSave.tooltip");
    
    @SettingsGroup(title = "settings.group.save")
    public Setting<?>[] saveGroup = {autoSave, regularSave};
    
    
    @SettingObject
    public IntSetting pagesFastMenuTextsNumber = new IntSetting(8, true, 0, 12, 2, false, false, SVGPathIcons.TEXT_HEIGHT, "pagesFastMenuTextsNumber",
            "settings.pagesFastMenuTextsNumber.title", "");
    
    @SettingObject
    public BooleanSetting pagesFastMenuShowImages = new BooleanSetting(false, true, SVGPathIcons.PICTURES, "pagesFastMenuShowImages",
            "settings.pagesFastMenuShowImages.title", "");
    
    @SettingsGroup(title = "settings.group.pagesContextMenu")
    public Setting<?>[] pagesContextMenu = {pagesFastMenuTextsNumber, pagesFastMenuShowImages};
    
    
    @SettingObject
    public BooleanSetting listsMoveAndDontCopy = new BooleanSetting(true, true, SVGPathIcons.STAR, "listsMoveAndDontCopy",
            "settings.listsMoveAndDontCopy.title", "settings.listsMoveAndDontCopy.tooltip");
    @SettingObject
    public BooleanSetting textOnlyStart = new BooleanSetting(true, true, SVGPathIcons.LIST, "textOnlyStart",
            "settings.textOnlyStart.title", "settings.textOnlyStart.tooltip");
    @SettingObject
    public BooleanSetting textSmall = new BooleanSetting(false, true, SVGPathIcons.TEXT_HEIGHT, "textSmall",
            "settings.textSmall.title", "");
    
    @SettingsGroup(title = "settings.group.elementsLists")
    public Setting<?>[] elementsLists = {listsMoveAndDontCopy, textOnlyStart, textSmall};
    
    
    @SettingObject
    public BooleanSetting defaultLatex = new BooleanSetting(false, true, SVGPathIcons.SUBSCRIPT, "text.defaultLaTeX",
            "settings.defaultLatex.title", "settings.defaultLatex.tooltip");
    
    @SettingObject
    public IntSetting defaultMaxWidth = new IntSetting(90, true, 1, 100, 5, false, true, SVGPathIcons.TEXT_WIDH, "text.defaultMaxWidth",
            "settings.textMaxWidth.title", "settings.textMaxWidth.tooltip");
    
    @SettingsGroup(title = "settings.group.textElements")
    public Setting<?>[] textElements = {defaultLatex, defaultMaxWidth};
    
    
    @SettingObject
    public BooleanSetting allowAutoTips = new BooleanSetting(true, true, SVGPathIcons.TOOLTIP, "allowAutoTips",
            "settings.allowAutoTips.title", "settings.allowAutoTips.tooltip");
    
    @SettingsGroup(title = "menuBar.help")
    public Setting<?>[] helpGroup = {allowAutoTips};
    
    
    @SettingObject
    public DoubleSetting zoom = new DoubleSetting(1d, true, .25, 4, .25, false, false, SVGPathIcons.SEARCH, "zoom",
            "settings.zoom", "settings.zoom.tooltip");
    
    @SettingObject
    public DoubleSetting renderZoom = new DoubleSetting(1d, true, .01, 5, .25, false, false, SVGPathIcons.SEARCH, "renderZoom",
            "settings.renderZoom", "");
    
    @SettingObject
    public BooleanSetting renderWithZoom = new BooleanSetting(true, true, SVGPathIcons.REDO, "renderWithZoom",
            "settings.renderWithZoom", "");
    
    @SettingObject
    public IntSetting menuForceOpenDelay = new IntSetting(-1, true, 0, 3000, 10, true, false, SVGPathIcons.CLOCK, "menuForceOpenDelay",
            "settings.menuForceOpenDelay", "settings.menuForceOpenDelay.tooltip");
    
    @SettingsGroup(title = "menuBar.tools.debug")
    public Setting<?>[] debugGroup = {zoom, renderZoom, renderWithZoom, menuForceOpenDelay}; // menu popup force
    
    
    public Settings(){
        loadSettings();
        
        textOnlyStart.valueProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) -> {
            if(MainWindow.textTab != null) TextTreeView.updateListsGraphic();
        });
        textSmall.valueProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) -> {
            if(MainWindow.textTab != null) TextTreeView.updateListsGraphic();
            if(t1) MainWindow.textTab.txtArea.setStyle("-fx-font-size: 12");
            else MainWindow.textTab.txtArea.setStyle("-fx-font-size: 13");
        });
        language.setGetEditPaneCallback(() -> {
            Button button = new Button(TR.tr("actions.choose"));
            button.setOnAction((e) -> Main.showLanguageWindow(false));
            button.setDefaultButton(true);
            return new HBox(button);
        });
    }
    
    public void loadSettings(){
        
        new File(Main.dataFolder).mkdirs();
        File settings = new File(Main.dataFolder + "settings.yml");
        try{
            
            if(settings.createNewFile()){ //file was created
                saveSettings();
            }else{ // file already exist
                
                Config config = new Config(settings);
                config.load();
                
                settingsVersionID = (int) config.getLong("versionID");
                settingsVersionCode = config.getString("version");
                
                for(Field field : getClass().getDeclaredFields()){
                    if(field.isAnnotationPresent(SettingObject.class)){
                        try{
                            if(field.getType() == StringSetting.class){
                                StringSetting var = (StringSetting) field.get(this);
                                String value = config.getString(var.getPath());
                                if(!value.isEmpty()) var.setValue(value);
                                
                            }else if(field.getType() == BooleanSetting.class){
                                BooleanSetting var = (BooleanSetting) field.get(this);
                                Boolean value = config.getBooleanNull(var.getPath());
                                if(value != null) var.setValue(value);
                                
                            }else if(field.getType() == IntSetting.class){
                                IntSetting var = (IntSetting) field.get(this);
                                Long value = config.getLongNull(var.getPath());
                                if(value != null) var.setValue(Math.toIntExact(value));
                            }else if(field.getType() == DoubleSetting.class){
                                DoubleSetting var = (DoubleSetting) field.get(this);
                                Double value = config.getDoubleNull(var.getPath());
                                if(value != null) var.setValue(value);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                
                if(settingsVersionID != Main.VERSION_ID) saveSettings();
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Unable to load settings.yml");
        }
    }
    
    public void saveSettings(){
        new Thread(() -> {
            try{
                new File(Main.dataFolder).mkdirs();
                File settings = new File(Main.dataFolder + "settings.yml");
                settings.createNewFile();
                Config config = new Config(settings);
                
                config.set("version", Main.VERSION);
                config.set("versionID", Main.VERSION_ID);
                
                for(Field field : getClass().getDeclaredFields()){
                    if(field.isAnnotationPresent(SettingObject.class)){
                        try{
                            Setting<?> setting = (Setting<?>) field.get(this);
                            config.set(setting.getPath(), setting.getValue());
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                config.save();
                
            }catch(Exception e){
                e.printStackTrace();
                System.err.println("Unable to save settings.yml");
            }
        }, "settingsSaver").start();
        
    }
    
    public String getSettingsVersionCode(){
        return settingsVersionCode;
    }
    
    // Return 0 if the versionID is unknown (or settings.yml does not exist).
    public int getSettingsVersionID(){
        return settingsVersionID;
    }
    // This only works before the settings was re-loaded.
    // (They can be re-loaded when the user cancel his edits on the SettingWindow.)
    public boolean hasVersionChanged(){
        return settingsVersionID != Main.VERSION_ID;
    }
}
