/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces;

import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;
import org.controlsfx.control.NotificationPane;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoHideNotificationPane extends NotificationPane {
    
    
    private static class Notification {
        public String text;
        public AlertIconType iconType;
        public TextField input;
        public int autoHideTime;
        
        public Notification(String text, AlertIconType iconType, int autoHideTime){
            this.text = text;
            this.iconType = iconType;
            this.autoHideTime = autoHideTime;
        }
        
        public Notification(String text, AlertIconType iconType, TextField input){
            this.text = text;
            this.iconType = iconType;
            this.input = input;
            this.autoHideTime = -1;
        }
    }
    
    // PENDING MANAGING
    
    ArrayList<Notification> pendingList = new ArrayList<>();
    
    public AutoHideNotificationPane(Node content){
        super(content);
        final EventHandler<Event> onHideEvent = e -> checkPending();
        addEventHandler(NotificationPane.ON_HIDDEN, onHideEvent);
        setShowFromTop(false);
        
        PaneUtils.setupScaling(this, true, true);
    }
    
    public void addToPending(String text, AlertIconType iconType, int autoHideTime){
        pendingList.add(new Notification(text, iconType, autoHideTime));
        checkPending();
    }
    
    public void showNow(String text, AlertIconType iconType, int autoHideTime){
        pendingList.addFirst(new Notification(text, iconType, autoHideTime));
        if(isShowing()) hide();
        else checkPending();
    }
    
    public void showNow(String text, AlertIconType iconType, TextField input){
        pendingList.addFirst(new Notification(text, iconType, input));
        if(isShowing()) hide();
        else checkPending();
    }
    
    private void checkPending(){
        if(!pendingList.isEmpty() && !isShowing()){
            Notification notif = pendingList.getFirst();
            if(notif.input == null) show(notif.text, notif.iconType, notif.autoHideTime);
            else showWithInput(notif.text, notif.input, notif.iconType, notif.autoHideTime);
            pendingList.removeFirst();
        }
    }
    
    
    // SHOW AND UI DESIGN
    
    public void show(String text, AlertIconType iconType, int autoHideTime){
        show(text, getGraphic(iconType), autoHideTime);
    }
    
    private static HBox getGraphic(AlertIconType iconType){
        Image image = new Image(Objects.requireNonNull(AutoHideNotificationPane.class.getResourceAsStream("/img/dialogs/" + iconType.getFileName() + ".png")));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(35);
        imageView.setPreserveRatio(true);
        
        HBox graphic = new HBox(imageView);
        HBox.setMargin(imageView, new Insets(4));
        return graphic;
    }
    
    public void showWithInput(String text, TextField input, AlertIconType iconType, int autoHideTime){
        show("", getInputGraphic(iconType, text, input), autoHideTime);
    }
    
    private HBox getInputGraphic(AlertIconType iconType, String text, TextField input){
        HBox graphic = getGraphic(iconType);
        VBox textGraphic = new VBox();
        HBox.setMargin(textGraphic, new Insets(4, 20, 4, 20));
        HBox.setHgrow(textGraphic, Priority.ALWAYS);
        input.setMaxWidth(400);
        
        Label textLabel = new Label(text);
        textGraphic.getChildren().addAll(textLabel, input);
        
        graphic.getChildren().add(textGraphic);
        return graphic;
    }
    
    
    // SHOW PROCESS
    
    private void show(String text, Pane graphic, int autoHideTime){
        hideAndThen(() -> {
            show(text, graphic);
            Platform.runLater(() -> {
                setupStyle();
                setupAutoHide(autoHideTime);
            });
        });
    }
    
    private void setupStyle(){
        Pane region = (Pane) lookup(".notification-bar > .pane");
        if(region != null){
            region.getStyleClass().add(JMetroStyleClass.BACKGROUND);
            region.setEffect(new DropShadow());
        }
        TextField input = (TextField) lookup(".text-field");
        if(input != null){
            input.requestFocus();
            PlatformUtils.runLaterOnUIThread(100, input::requestFocus);
        }
    }
    
    private void setupAutoHide(int autoHideTime){
        if(autoHideTime > 0){
            AtomicBoolean hidden = new AtomicBoolean(false);
            setOnHiding((e) -> hidden.set(true));
            
            new Thread(() -> {
                try{
                    Thread.sleep(autoHideTime * 1000L);
                }catch(InterruptedException e){
                    Log.eNotified(e, "Hide thread sleep interrupted");
                }
                if(!hidden.get()) hide();
                
            }, "notification auto hide").start();
        }
    }
    
    
    private void hideAndThen(final Runnable r){
        if(isShowing()){
            final EventHandler<Event> eventHandler = new EventHandler<>() {
                @Override
                public void handle(Event e){
                    r.run();
                    removeEventHandler(NotificationPane.ON_HIDDEN, this);
                }
            };
            addEventHandler(NotificationPane.ON_HIDDEN, eventHandler);
            hide();
        }else{
            r.run();
        }
    }
    
}
