/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.settings;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.settings.Setting;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class SettingGroupPane extends VBox {
    
    private final Label title;
    private final GridPane container = new GridPane();
    
    public SettingGroupPane(String titleKey, Setting<?>... settings){
        
        title = new Label(TR.tr(titleKey));
        title.setStyle("-fx-font-size: 16;");
        
        container.setHgap(10);
        container.setVgap(4);
        VBox.setMargin(container, new Insets(0, 0, 0, 15));
        
        int i = 0;
        for(Setting<?> setting : settings){
            container.add(generateSettingIcon(setting), 0, i);
            container.add(generateSettingLabel(setting), 1, i);
            container.add(generateSettingEditor(setting), 2, i);
            i++;
        }
        
        setSpacing(10);
        getChildren().addAll(title, container);
    }
    private Node generateSettingIcon(Setting<?> setting){
        String color = Main.settings.darkTheme.getValue() ? "darkgray" : "gray";
        Region icon = SVGPathIcons.generateImage(setting.getIcon(), color, 0, 20, 20);
        Pane iconContainer = new Pane(icon);
        iconContainer.setMinSize(20, 20);
        iconContainer.setMaxSize(20, 20);
        HBox.setMargin(iconContainer, new Insets(2.5, 10, 2.5, 0));
        
        return iconContainer;
    }
    private Node generateSettingLabel(Setting<?> setting){
        
        Label label = new Label(TR.tr(setting.getTitle()));
        label.setWrapText(true);
        
        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinHeight(25);
        GridPane.setHgrow(box, Priority.ALWAYS);
        
        return box;
    }
    private Node generateSettingEditor(Setting<?> setting){
        HBox box = setting.getCustomEditPane();
        box.setPrefHeight(25);
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER_RIGHT);
        
        if(!setting.getDescription().isBlank()){
            String color = Main.settings.darkTheme.getValue() ? "#ff8989" : "#ff8989";
            Region icon = SVGPathIcons.generateImage(SVGPathIcons.INFO, color, 0, 15, 15);
            Pane iconContainer = new Pane(icon);
            iconContainer.setMinSize(15, 15);
            iconContainer.setMaxSize(15, 15);
            HBox.setMargin(iconContainer, new Insets(5, 0, 5, 0));
            
            Tooltip tooltip = PaneUtils.genWrappedToolTip(TR.tr(setting.getDescription()));
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setShowDuration(Duration.INDEFINITE);
            Tooltip.install(iconContainer, tooltip);
            
            box.getChildren().add(0, iconContainer);
        }
        
        return box;
    }
    
}
