package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.settings.Setting;
import fr.clementgre.pdf4teachers.datasaving.settings.SettingObject;
import fr.clementgre.pdf4teachers.datasaving.settings.Settings;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.lang.reflect.Field;

public class SettingsWindow extends AlternativeWindow<GridPane>{
    
    public SettingsWindow(){
        super(new GridPane(), StageWidth.NORMAL, TR.tr("settingsWindow.title"));
    }
    
    @Override
    public void setupSubClass(){
        setSubHeaderText(TR.tr("settingsWindow.description"));
    
        root.setHgap(10);
        root.setVgap(5);
        
        Settings s = Main.settings;
        int i = 0;
        for(Field field : s.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(SettingObject.class)){
                try{
                    Setting<?> setting = (Setting<?>) field.get(Main.settings);
                    
                    root.add(generateSettingLabel(setting), 0, i);
                    root.add(generateSettingEditor(setting), 1, i);
                    
                    i++;
                }catch(IllegalAccessException e){ e.printStackTrace(); }
            }
        }
        /*settings.getItems().addAll(
                s.language.getMenuItem(), s.checkUpdates.getMenuItem(), s.sendStats.getMenuItem(),
                new SeparatorMenuItem(), s.restoreLastSession.getMenuItem(), s.defaultZoom.getMenuItem(), s.zoomAnimations.getMenuItem(), s.darkTheme.getMenuItem(),
                new SeparatorMenuItem(), s.autoSave.getMenuItem(), s.regularSave.getMenuItem(),
                new SeparatorMenuItem(), s.textAutoRemove.getMenuItem(), s.textOnlyStart.getMenuItem(), s.textSmall.getMenuItem(),
                new SeparatorMenuItem(), s.allowAutoTips.getMenuItem());*/
        
        setupBtns();
    }
    
    private void setupBtns(){
        Button cancel = new Button(TR.tr("actions.cancel"));
        Button save = new Button(TR.tr("actions.save"));
    
        cancel.setOnAction(event -> {
            Main.settings.loadSettings();
            close();
        });
        save.setOnAction(event -> {
            Main.settings.saveSettings();
        });
        
        setButtons(cancel, save);
    }
    
    private Node generateSettingLabel(Setting<?> setting){
        
        Label label = new Label(TR.tr(setting.getTitle()));
        label.setWrapText(true);
        label.setMinWidth(250);
        
        Region icon = SVGPathIcons.generateImage(SVGPathIcons.INFO, "red", 0, 15, 15);
        Pane iconContainer = new Pane(icon);
        icon.setPrefSize(15, 15);
        HBox.setMargin(icon, new Insets(0, 10, 0, 0));
    
        Tooltip tooltip = PaneUtils.genWrappedToolTip(TR.tr(setting.getDescription()));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setShowDuration(Duration.INDEFINITE);
        Tooltip.install(iconContainer, tooltip);
        
        HBox box = new HBox(icon, label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinHeight(25);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }
    private Node generateSettingEditor(Setting<?> setting){
        HBox pane = setting.getCustomEditPane();
        pane.setPrefHeight(25);
        return pane;
    }
    
    @Override
    public void afterShown(){
    
    }
}
