package fr.clementgre.pdf4teachers.datasaving.settings;

import de.jangassen.MenuToolkit;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeView;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.beans.value.ObservableValue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class Settings{
    
    private String settingsVersion = "";
    
    @SettingObject
    public StringSetting language = new StringSetting("", false, SVGPathIcons.GLOBE, "language",
            "settings.language.title", "settings.language.tooltip");
    @SettingObject
    public BooleanSetting restoreLastSession = new BooleanSetting(true, true, SVGPathIcons.REDO, "restoreLastSession",
            "settings.restoreLastSession.title", "settings.restoreLastSession.tooltip");
    @SettingObject
    public BooleanSetting checkUpdates = new BooleanSetting(true, true, SVGPathIcons.WIFI, "checkUpdates",
            "settings.checkUpdates.title", "settings.checkUpdates.tooltip");
    @SettingObject
    public BooleanSetting sendStats = new BooleanSetting(true, true, SVGPathIcons.STATS, "sendStatistics",
            "settings.sendStatistics.title", "settings.sendStatistics.tooltip");
    @SettingObject
    public IntSetting defaultZoom = new IntSetting(130, true, SVGPathIcons.SEARCH, "defaultZoom",
            "settings.defaultZoom.title", "settings.defaultZoom.tooltip");
    @SettingObject
    public BooleanSetting zoomAnimations = new BooleanSetting(!Main.isOSX(), true, SVGPathIcons.LAYERS, "zoomAnimations",
            "settings.zoomAnimations.title", "settings.zoomAnimations.tooltip");
    @SettingObject
    public BooleanSetting darkTheme = new BooleanSetting(!Main.isOSX() || MenuToolkit.toolkit().systemUsesDarkMode(), true, SVGPathIcons.SUN, "darkTheme",
            "settings.darkTheme.title", "settings.darkTheme.tooltip");
    
    @SettingObject
    public BooleanSetting autoSave = new BooleanSetting(true, true, SVGPathIcons.SAVE, "autoSave",
            "settings.autoSave.title", "settings.autoSave.tooltip");
    @SettingObject
    public IntSetting regularSave = new IntSetting(-1, true, SVGPathIcons.CLOCK, "regularSave",
            "settings.regularSave.title", "settings.regularSave.tooltip");
    
    @SettingObject
    public BooleanSetting textAutoRemove = new BooleanSetting(true, true, SVGPathIcons.STAR, "textAutoRemove",
            "settings.textAutoRemove.title", "settings.textAutoRemove.tooltip");
    @SettingObject
    public BooleanSetting textOnlyStart = new BooleanSetting(true, true, SVGPathIcons.LIST, "textOnlyStart",
            "settings.textOnlyStart.title", "settings.textOnlyStart.tooltip");
    @SettingObject
    public BooleanSetting textSmall = new BooleanSetting(false, true, SVGPathIcons.TEXT_HEIGHT, "textSmall",
            "settings.textSmall.title", "settings.textSmall.tooltip");
    
    @SettingObject
    public BooleanSetting allowAutoTips = new BooleanSetting(true, true, SVGPathIcons.TOOLTIP, "allowAutoTips",
            "settings.allowAutoTips.title", "settings.allowAutoTips.tooltip");
    
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
                
                settingsVersion = config.getString("version");
                
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
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                
                if(!settingsVersion.equals(Main.VERSION)) saveSettings();
            }
        }catch(IOException e){
            e.printStackTrace();
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
                
            }catch(IOException e){
                e.printStackTrace();
            }
        }, "settingsSaver").start();
        
    }
    
    public String getSettingsVersion(){
        return settingsVersion;
    }
}
