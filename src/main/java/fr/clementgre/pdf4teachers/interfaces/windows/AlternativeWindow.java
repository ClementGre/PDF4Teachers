package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.StagesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class AlternativeWindow<R extends Node> extends Stage{
    
    private final VBox container = new VBox();
    private final VBox header = new VBox();
    public R root;
    public HBox buttonsBox;
    
    protected ScrollPane scrollPane = new ScrollPane(container);
    private final BorderPane borderPane = new BorderPane(scrollPane);
    private final Scene scene = new Scene(borderPane);
    
    private final Label headerText = new Label();
    private final Label subHeaderText = new Label();
    
    public enum StageWidth{
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
        this(root, width, titleHeader, titleHeader, null);
    }
    public AlternativeWindow(R root, StageWidth width, String title, String header){
        this(root, width, title, header, null);
    }
    public AlternativeWindow(R root, StageWidth width, String title, String header, String subHeader){
        this.root = root;
        
        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        
        setWidth(width.getWidth());
        setMaxHeight(Main.SCREEN_VISUAL_BOUNDS.getHeight());
        
        setTitle("PDF4Teachers - " + title);
        setScene(scene);
        StyleManager.putStyle(scene, Style.DEFAULT);
        StyleManager.putStyle(borderPane, Style.DEFAULT);
        StyleManager.putCustomStyle(scene, "alternativeWindow.css");
        if(StyleManager.DEFAULT_STYLE == jfxtras.styles.jmetro.Style.LIGHT) StyleManager.putCustomStyle(scene, "alternativeWindow-light.css");
        else StyleManager.putCustomStyle(scene, "alternativeWindow-dark.css");
        PaneUtils.setupScaling(borderPane, true, false);
    
        setOnShown(e -> {
            StagesUtils.resizeStageAccordingToAppScale(this, scene);
    
            setContentMinWidth(400, true);
            setMinHeight(300 * Main.settings.zoom.getValue());
            setMaxWidth(width.getWidth()*2 * Main.settings.zoom.getValue());
    
            if(getHeight() > 1.5*getWidth()) setHeight(1.5*getWidth());
            
            Main.window.centerWindowIntoMe(this);
            MainWindow.preventWindowOverflowScreen(this);
            
            if(toRequestFocus != null){
                toRequestFocus.requestFocus();
                toRequestFocus.setDefaultButton(true);
            }
            
            afterShown();
        });
        
        setup(header, subHeader);
        Platform.runLater(() -> {
            setupSubClass();
            Main.window.centerWindowIntoMe(this, width.getWidth() * Main.settings.zoom.getValue(), 600 * Main.settings.zoom.getValue());
            show();
        });
        
        // SCROLLPANE SPEED FIX //
        
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            e.consume();
            if(Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) / 2){ // Accept side scrolling only if the scroll is not too vertical
                double hValue = scrollPane.getHvalue() + e.getDeltaY() * 2 / (scrollPane.getWidth() - container.getWidth());
                scrollPane.setHvalue(StringUtils.clamp(hValue, scrollPane.getHmin(), scrollPane.getHmax()));
            }
    
            double vValue = scrollPane.getVvalue() + e.getDeltaY() * 2 / (scrollPane.getHeight() - container.getHeight());
            scrollPane.setVvalue(StringUtils.clamp(vValue, scrollPane.getVmin(), scrollPane.getVmax()));
            
        });
    }
    
    public abstract void setupSubClass();
    public abstract void afterShown();
    
    private void setup(String header, String subHeader){
        this.header.getStyleClass().add("header");
        root.getStyleClass().add("rootPane");
        headerText.getStyleClass().add("headerText");
        subHeaderText.getStyleClass().add("subHeaderText");
        
        VBox.setMargin(headerText, new Insets(30, 20, -5, 20));
        VBox.setMargin(subHeaderText, new Insets(0, 20, 30, 20));
        
        setHeaderText(header);
        setSubHeaderText(subHeader);
        this.header.getChildren().addAll(headerText, subHeaderText);
    
        container.getChildren().addAll(this.header, root);
        scrollPane.setFitToWidth(true);
    }
    
    private void setupButtonsBox(){
        buttonsBox = new HBox();
        HBox container = new HBox(buttonsBox);
        
        container.getStyleClass().add("buttonBoxContainer");
        buttonsBox.getStyleClass().add("buttonBox");
        
        HBox.setHgrow(buttonsBox, Priority.ALWAYS);
        borderPane.setBottom(container);
    }
    private Button toRequestFocus;
    
    // Must be called after setButtons(), only once
    public void setLeftButtons(Button... buttons){
        buttonsBox.getChildren().add(0, new HBoxSpacer());
        for(Button button : buttons){
            buttonsBox.getChildren().add(0, button);
        }
    }
    // Should be called only once
    public void setButtons(Button... buttons){
        setupButtonsBox();
        toRequestFocus = buttons[buttons.length-1];
        buttonsBox.getChildren().addAll(buttons);
    }
    
    public void setContentMinWidth(int width, boolean affectWindow){
        if(affectWindow) setMinWidth((width+20+16) * Main.settings.zoom.getValue());
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
    
}
