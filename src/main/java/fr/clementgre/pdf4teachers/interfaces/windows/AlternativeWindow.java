/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.SmoothishScrollpane;
import fr.clementgre.pdf4teachers.interfaces.AutoHideNotificationPane;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StagesUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Objects;

public abstract class AlternativeWindow<R extends Node> extends Stage {
    
    protected final VBox container = new VBox();
    private final VBox header = new VBox();
    public R root;
    
    private final HBox bottomBarContainer = new HBox();
    public HBox infoBox;
    public HBox buttonsBox;
    
    protected SmoothishScrollpane scrollPane = new SmoothishScrollpane(container);
    private final BorderPane borderPane = new BorderPane(scrollPane);
    private final Scene scene = new Scene(borderPane);
    
    private final Label headerText = new Label();
    private final Label subHeaderText = new Label();
    
    public enum StageWidth {
        NORMAL(545),
        LARGE(700),
        ULTRA_LARGE(1000),
        MAXIMUM(99999);
        private final int width;
        StageWidth(int width){
            this.width = width;
        }
        public int getWidth(){
            return width;
        }
    }
    
    public AlternativeWindow(R root, StageWidth width, String titleHeader){
        this(root, width, 0, titleHeader, titleHeader, null);
    }
    public AlternativeWindow(R root, StageWidth width, String title, String header){
        this(root, width, 0, title, header, null);
    }
    public AlternativeWindow(R root, StageWidth width, String title, String header, String subHeader){
        this(root, width, 0, title, header, subHeader);
    }
    public AlternativeWindow(R root, StageWidth width, int height, String title, String header, String subHeader){
        this.root = root;

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        
        setWidth(width.getWidth());
        setMaxHeight(width.getWidth() * 1.75);
        if(height != 0) setHeight(height);
        
        setTitle(title + " - PDF4Teachers");
        setScene(scene);
        StyleManager.putStyle(scene, Style.DEFAULT);
        StyleManager.putStyle(borderPane, Style.DEFAULT);
        StyleManager.putCustomStyle(scene, "alternativeWindow.css");
        if(StyleManager.DEFAULT_STYLE == jfxtras.styles.jmetro.Style.LIGHT)
            StyleManager.putCustomStyle(scene, "alternativeWindow-light.css");
        else StyleManager.putCustomStyle(scene, "alternativeWindow-dark.css");
        PaneUtils.setupScaling(borderPane, true, false);
        
        setOnShown(e -> {
            StagesUtils.scaleStage(this, scene);
            
            setContentMinWidth(400, true);
            setMinHeight(300 * Main.settings.zoom.getValue());
            setMaxWidth(width.getWidth() * 2 * Main.settings.zoom.getValue());
            
            if(getHeight() > 1.6 * getWidth()) setHeight(1.6 * getWidth());
    
            if(Main.window != null) Main.window.centerWindowIntoMe(this);
            MainWindow.preventStageOverflowScreen(this);
    
            if(toRequestFocus != null){
                toRequestFocus.requestFocus();
                toRequestFocus.setDefaultButton(true);
            }
            
            if(height == 0){
                // When creating the window, there is a small animation so the dimensions are not correct after the runLater.
                double trueHeight = getHeight();
                double trueWidth = getWidth();
                Platform.runLater(() -> {
                    // Restore thue dimensions (due to the animation)
                    setHeight(trueHeight);
                    setWidth(trueWidth);
                    // Remove the scroll by stretching the window (+ 6px to prevent the scrollbar from showing)
        
                    double diffHeight = Math.max((container.getHeight() - scrollPane.getHeight() + 6) * Main.settings.zoom.getValue(), 0);
                    setHeight(Math.min(getMaxHeight(), getHeight() + diffHeight));
                });
            }
            Platform.runLater(() -> {
                if(Main.window != null) Main.window.centerWindowIntoMe(this);
                MainWindow.preventStageOverflowScreen(this);
                afterShown();
            });
            
            
        });
        
        scene.setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.ESCAPE){
                fireEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSE_REQUEST));
            }
        });
        
        setup(header, subHeader);
        
        Platform.runLater(() -> {
            setupSubClass();
            if(Main.window != null) {
                Main.window.centerWindowIntoMe(this, width.getWidth() * Main.settings.zoom.getValue(), 600 * Main.settings.zoom.getValue());
            }
            show();
            
        });

        // MenuBar on OSX Fix //
        if(PlatformUtils.isMac() && MenuBar.isSystemMenuBarSupported()){
            javafx.scene.control.MenuBar menuBar = new javafx.scene.control.MenuBar();
            borderPane.setTop(menuBar);
            menuBar.setUseSystemMenuBar(true);
        }
    }
    
    public abstract void setupSubClass();
    public abstract void afterShown();
    
    private void setup(String header, String subHeader){
        this.header.getStyleClass().add("header");
        root.getStyleClass().add("rootPane");
        container.getStyleClass().add("container");
        borderPane.getStyleClass().add("mainBorderPane");
        headerText.getStyleClass().add("headerText");
        subHeaderText.getStyleClass().add("subHeaderText");
        
        VBox.setMargin(headerText, new Insets(30, 20, -2, 20));
        VBox.setMargin(subHeaderText, new Insets(0, 20, 30, 20));
        subHeaderText.setMinHeight(Region.USE_PREF_SIZE);
        
        setHeaderText(header);
        setSubHeaderText(subHeader);
        this.header.getChildren().addAll(headerText, subHeaderText);
    
        container.getChildren().addAll(this.header, root);
        scrollPane.setFitToWidth(true);
    }
    
    private void setupButtonsBox(){
        infoBox = new HBox();
        buttonsBox = new HBox();
        bottomBarContainer.getChildren().setAll(buttonsBox);
        
        bottomBarContainer.getStyleClass().add("buttonBoxContainer");
        infoBox.getStyleClass().add("infoBox");
        buttonsBox.getStyleClass().add("buttonBox");
        buttonsBox.setMinWidth(Region.USE_PREF_SIZE);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        HBox.setHgrow(buttonsBox, Priority.ALWAYS);
        borderPane.setBottom(bottomBarContainer);
    }
    private Button toRequestFocus;
    
    
    public void updateInfoBox(AlertIconType iconType, String text){
        if(iconType == null && text.isBlank()){
            bottomBarContainer.getChildren().remove(infoBox);
            return;
        }
        if(!bottomBarContainer.getChildren().contains(infoBox)){
            bottomBarContainer.getChildren().add(0, infoBox);
        }
        
        Label info = new Label(text);
        Image image = new Image(Objects.requireNonNull(AutoHideNotificationPane.class.getResourceAsStream("/img/dialogs/" + iconType.getFileName() + ".png")));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(35);
        imageView.setPreserveRatio(true);
        
        infoBox.getChildren().setAll(imageView, info);
    }
    public void setInfoBoxLoader(){
        ProgressBar loader = new ProgressBar();
        PaneUtils.setHBoxPosition(loader, -1, 0, new Insets(13, 0, 0, 0));
        infoBox.getChildren().setAll(loader);
    }
    protected void clearInfoBox(){
        infoBox.getChildren().clear();
    }
    
    // Must be called after setButtons(), only once
    public void setLeftButtons(Button... buttons){
        buttonsBox.getChildren().add(0, new HBoxSpacer());
        for(int i = buttons.length-1; i >= 0; i--)
            buttonsBox.getChildren().add(0, buttons[i]);
    }
    // Should be called only once
    public void setButtons(Button... buttons){
        setupButtonsBox();
        toRequestFocus = buttons[buttons.length - 1];
        buttonsBox.getChildren().addAll(buttons);
    }
    
    public void setContentMinWidth(int width, boolean affectWindow){
        if(affectWindow) setMinWidth((width + 20 + 16) * Main.settings.zoom.getValue());
        else container.setMinWidth(width * Main.settings.zoom.getValue());
    }
    
    public void setHeaderText(String text){
        headerText.setText(text);
    }
    public void setSubHeaderText(String text){
        subHeaderText.setText(text);
        if(text == null){
            VBox.setMargin(subHeaderText, new Insets(0, 20, -subHeaderText.getHeight(), 20));
        }else{
            VBox.setMargin(subHeaderText, new Insets(0, 20, 30, 20));
        }
    }
    
    
    
    // Utils
    public static VBox generateInfo(String text, boolean topBar){
        
        VBox box = new VBox();
        
        if(topBar){
            Separator separator = new Separator();
            PaneUtils.setVBoxPosition(separator, 0, 0, new Insets(5, -5, 0, -5));
            box.getChildren().add(separator);
        }
        
        if(text != null){
            Label info = new Label(text);
            PaneUtils.setVBoxPosition(info, 0, 0, 2.5);
            box.getChildren().add(info);
        }
        
        return box;
    }
    
}
