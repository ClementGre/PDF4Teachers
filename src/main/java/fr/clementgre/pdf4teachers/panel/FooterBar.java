package fr.clementgre.pdf4teachers.panel;

import fr.clementgre.pdf4teachers.components.SliderWithoutPopup;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class FooterBar extends StackPane{
    
    private final StackPane messagePane = new StackPane();
    private final Label message = new Label();
    
    private final HBox root = new HBox();
    
    private final HBox zoom = new HBox();
    private final Label zoomInfo = new Label();
    private final SliderWithoutPopup zoomController = new SliderWithoutPopup(1, 40, 20);
    private final Label zoomPercent = new Label();
    private final ColorAdjust lightGrayColorAdjust = new ColorAdjust();
    private final Region fitZoom = SVGPathIcons.generateImage(SVGPathIcons.FULL_SCREEN, "lightgray", 0, 14, 14, lightGrayColorAdjust);
    private final Region overviewZoom = SVGPathIcons.generateImage(SVGPathIcons.SMALL_SCREEN, "lightgray", 0, 14, 14, lightGrayColorAdjust);
    
    private final Label statsElements = new Label();
    private final Label statsTexts = new Label();
    private final Label statsGrades = new Label();
    private final Label statsGraphics = new Label();
    private final Label statsTotalGrade = new Label();
    private final Label status = new Label();
    
    private final Region spacer = new Region();
    
    private int oldWidth = 0;
    private final int widthLimit = 1000;
    
    public FooterBar(){
        StyleManager.putStyle(this, Style.ACCENT);
        getStyleClass().add("app-footer-bar");
        setMaxHeight(20);
        setup();
    }
    
    public void setup(){
    
        zoomPercent.setText(((int) MainWindow.mainScreen.getZoomPercent()) + "%");
        MainWindow.mainScreen.pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
            zoomPercent.setText(((int) MainWindow.mainScreen.getZoomPercent()) + "%");
            if(zoomController.getValue() != newValue.doubleValue()){
                double scale = newValue.doubleValue();
                double val = 20;
                
                if(scale < 1){
                    val = scale * 20;
                }else if(scale > 1){
                    val = 20 + (20 * (scale-1)) / 4;
                }
                zoomController.setValue(val);
            }
        });
        PaneUtils.setHBoxPosition(zoomController, 0, 20, 0);
        zoomController.valueProperty().addListener((observable, oldValue, newValue) -> {
            double val = newValue.doubleValue();
            double scale = 1;
            // val < 20 : scale = val / 20
            // val > 20 : scale = 1 + (4 * (val-20)) / 20
            if(val < 20){
                scale = val / 20;
            }else if(val > 20){
                scale = 1 + (4 * (val-20)) / 20;
            }
            MainWindow.mainScreen.zoomOperator.zoom(scale, true);
        });
        // FIT ZOOM
        fitZoom.setOnMouseClicked((e) -> {
            MainWindow.mainScreen.zoomOperator.fitWidth(false);
        });
        fitZoom.setOnMouseEntered((e) -> {
            if(!fitZoom.isDisabled()) fitZoom.setStyle("-fx-background-color: white;");
        });
        fitZoom.setOnMouseExited((e) -> fitZoom.setStyle("-fx-background-color: lightgray;"));
        // COLORS ADJUST
        fitZoom.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) lightGrayColorAdjust.setBrightness(-0.6);
            else lightGrayColorAdjust.setBrightness(0);
        });
        // OVERVIEW ZOOM
        overviewZoom.setOnMouseClicked((e) -> {
            MainWindow.mainScreen.zoomOperator.overviewWidth(false);
        });
        overviewZoom.setOnMouseEntered((e) -> {
            if(!overviewZoom.isDisabled()) overviewZoom.setStyle("-fx-background-color: white;");
        });
        overviewZoom.setOnMouseExited((e) -> overviewZoom.setStyle("-fx-background-color: lightgray;"));
        // ZOOM INFO
        zoomInfo.setText(TR.tr("footerBar.zoom"));
        fitZoom.setMaxHeight(14);
        overviewZoom.setMaxHeight(14);
        zoom.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(fitZoom, new Insets(0, 5, 1, 5));
        HBox.setMargin(overviewZoom, new Insets(0, 0, 1, 3));
        zoomPercent.setMinWidth(40);
        zoom.setSpacing(5);
        zoom.getChildren().addAll(zoomInfo, zoomPercent, zoomController, fitZoom, overviewZoom);
        
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        MainWindow.mainScreen.statusProperty().addListener((observable, oldValue, newValue) -> updateStatus(newValue.intValue(), true));
        updateStatus(MainScreen.Status.CLOSED, true);
        
        statsElements.setStyle("-fx-text-fill: #b2b2b2;");
        statsTexts.setStyle("-fx-text-fill: #b2b2b2;");
        statsGrades.setStyle("-fx-text-fill: #b2b2b2;");
        statsGraphics.setStyle("-fx-text-fill: #b2b2b2;");
        statsTotalGrade.setStyle("-fx-text-fill: #b2b2b2;");
        
        root.setPadding(new Insets(0, 10, 0, 10));
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().setAll(zoom, getSpacerShape(), spacer, getSpacerShape(), this.status);
        getChildren().add(root);
        
        messagePane.getChildren().add(message);
        messagePane.setTranslateY(20);
        messagePane.prefWidthProperty().bind(widthProperty());
        messagePane.setPrefHeight(20);
        message.prefWidthProperty().bind(widthProperty());
        
        widthProperty().addListener((observable, oldValue, newValue) -> {
            if(oldWidth > widthLimit && newValue.intValue() < widthLimit){
                updateStatus(MainWindow.mainScreen.getStatus(), true);
                oldWidth = newValue.intValue();
            }else if(oldWidth < widthLimit && newValue.intValue() > widthLimit){
                updateStatus(MainWindow.mainScreen.getStatus(), true);
                oldWidth = newValue.intValue();
            }
        });
    }
    
    public void showAlert(Color background, Color messageColor, String text){
        if(!getChildren().contains(messagePane)){
            getChildren().add(messagePane);
            messagePane.setTranslateY(20);
        }
        messagePane.setBackground(new Background(new BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY)));
        messagePane.setOpacity(0);
        message.setTextFill(messageColor);
        message.setText(text);
        message.setAlignment(Pos.CENTER);
        message.setStyle("-fx-font-weight: 800; -fx-font-family: Arial;");
        
        Platform.runLater(() -> {
            Timeline timelineShow = new Timeline(60);
            timelineShow.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(messagePane.translateYProperty(), 0)),
                    new KeyFrame(Duration.millis(200), new KeyValue(messagePane.opacityProperty(), 1))
            );
            timelineShow.play();
            
            PlatformUtils.runLaterOnUIThread(2000, () -> {
                Timeline timelineHide = new Timeline(60);
                timelineHide.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(200), new KeyValue(messagePane.translateYProperty(), 20)),
                        new KeyFrame(Duration.millis(200), new KeyValue(messagePane.opacityProperty(), 0))
                );
                timelineHide.play();
                timelineHide.setOnFinished((e) -> getChildren().remove(messagePane));
            });
        });
    }
    
    private Pane getSpacerShape(){
        Line shape = new Line(0, 0, 0, 14);
        shape.setStroke(Color.web("#4B4B4B"));
        shape.setStrokeWidth(1);
        
        StackPane pane = new StackPane();
        pane.getChildren().add(shape);
        pane.setPadding(new Insets(3));
        
        return pane;
    }
    
    public void updateCurrentPage(){
        updateStatus(MainWindow.mainScreen.getStatus(), false);
    }
    
    public void updateStatus(int status, boolean hard){
        if(status == MainScreen.Status.OPEN){
            if(hard){
                if(getWidth() < widthLimit){
                    root.getChildren().setAll(zoom, getSpacerShape(), spacer, getSpacerShape(), this.status);
                }else{
                    root.getChildren().setAll(zoom, getSpacerShape(), spacer, getSpacerShape(),
                            statsElements, getSpacerShape(), statsTexts, getSpacerShape(), statsGrades, getSpacerShape(), statsGraphics, getSpacerShape(), statsTotalGrade, getSpacerShape(),
                            this.status);
                }
                
                updateStats();
            }
            zoomController.setDisable(false);
            zoomPercent.setDisable(false);
            zoomInfo.setDisable(false);
            fitZoom.setDisable(false);
            overviewZoom.setDisable(false);
            if(MainWindow.mainScreen.document.getLastCursorOverPage() == -1){
                this.status.setText(MainWindow.mainScreen.document.getFileName() + " - " + "?/" + MainWindow.mainScreen.document.totalPages);
            }else this.status.setText(MainWindow.mainScreen.document.getFileName() + " - " + (MainWindow.mainScreen.document.getLastCursorOverPage() + 1) + "/" + MainWindow.mainScreen.document.totalPages);
            
        }else{
            zoomController.setDisable(true);
            zoomPercent.setDisable(true);
            zoomInfo.setDisable(true);
            fitZoom.setDisable(true);
            overviewZoom.setDisable(true);
            if(hard){
                root.getChildren().setAll(zoom, getSpacerShape(), spacer, getSpacerShape(), this.status);
            }
            
            if(status == MainScreen.Status.CLOSED){
                this.status.setText(TR.tr("footerBar.documentStatus.noDocument"));
            }else if(status == MainScreen.Status.ERROR || status == MainScreen.Status.ERROR_EDITION){
                this.status.setText(TR.tr("footerBar.documentStatus.error"));
            }
        }
    }
    
    public void updateStats(){
        if(MainWindow.mainScreen.hasDocument(false)){
            Platform.runLater(() -> {
                if(MainWindow.mainScreen.document == null) return;
                int[] count = MainWindow.mainScreen.document.countElements();
                statsElements.setText(count[0] + " " + TR.tr("elements.name"));
                statsTexts.setText(count[1] + " " + TR.tr("elements.name.texts"));
                statsGrades.setText(count[2] + " " + TR.tr("elements.name.grades"));
                statsGraphics.setText(count[3] + " " + TR.tr("elements.name.paints"));
                
                if(GradeTreeView.getTotal() != null){
                    statsTotalGrade.setText(MainWindow.gradesDigFormat.format(GradeTreeView.getTotal().getCore().getVisibleValue()) + "/" + MainWindow.gradesDigFormat.format(GradeTreeView.getTotal().getCore().getTotal()));
                }
            });
        }
    }
}
