/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.log;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class LogWindow extends Stage {
    
    private final Label label = new Label(LogsManager.getLogs());
    private final Pane pane = new Pane();
    private final ScrollPane scrollPane = new ScrollPane(pane);
    
    private boolean doScrollToBottom;
    
    public LogWindow(){
        
        Pane root = new Pane();
        Scene scene = new Scene(root, 1200, 675);
        
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        setResizable(true);
        setTitle(TR.tr("printStreamWindow.title"));
        setScene(scene);
        setOnCloseRequest(e -> {
            stopUpdater();
            close();
        });
        new JMetro(scene, Style.DARK);
        
        root.setStyle("-fx-background-color: black;");
        scrollPane.setStyle("-fx-background-color: black;");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.prefWidthProperty().bind(scene.widthProperty());
        scrollPane.prefHeightProperty().bind(scene.heightProperty());
        label.heightProperty().addListener((o, oldValue, newValue) -> {
            Platform.runLater(() -> {
                doScrollToBottom = scrollPane.getVvalue() == scrollPane.getVmax();
                pane.setMinHeight(newValue.doubleValue());
            });
        });
        pane.heightProperty().addListener((o, oldValue, newValue) -> {
            if(doScrollToBottom) {
                scrollPane.setVvalue(scrollPane.getVmax());
            }
        });
        root.getChildren().add(scrollPane);
        
        setupUi(pane);
        
        Main.window.centerWindowIntoMe(this);
        show();
        Main.window.centerWindowIntoMe(this);
        MainWindow.preventStageOverflowScreen(this, MainWindow.getScreen().getVisualBounds());
    }
    
    private void setupUi(Pane root){
        
        label.setStyle("-fx-text-fill: white; -fx-padding: 5; -fx-font: 13 'Courier Prime' !important;");
        
        label.setWrapText(true);
        label.prefWidthProperty().bind(root.widthProperty());
        label.minHeight(Double.MAX_VALUE);
        
        root.getChildren().add(label);
        root.setBackground(new Background(new BackgroundFill(Color.web("#2a2a31"), CornerRadii.EMPTY, Insets.EMPTY)));
        
        updater.start();
    }
    
    
    private boolean needToStopUpdater;
    public void stopUpdater(){
        needToStopUpdater = true;
    }
    
    private final Thread updater = new Thread(() -> {
        needToStopUpdater = false;
        try{
            Thread.sleep(200);
        }catch(InterruptedException e){Log.eNotified(e);}
        
        int lastLen = 0;
        while(!needToStopUpdater){
            if(lastLen != LogsManager.getLogsLength()){
                lastLen = LogsManager.getLogsLength();
                Platform.runLater(() -> label.setText(LogsManager.getLogs()));
            }
            PlatformUtils.sleepThread(200);
        }
    });
    
}
