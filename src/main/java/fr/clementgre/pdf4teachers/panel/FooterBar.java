/*
 * Copyright (c) 2019-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel;

import fr.clementgre.pdf4teachers.components.SliderWithoutPopup;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.MainScreen.ZoomOperator;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class FooterBar extends StackPane {

    private final StackPane messagePane = new StackPane();
    private final Label message = new Label();

    private final HBox root = new HBox();

    private final HBox zoom = new HBox();
    private final SliderWithoutPopup zoomController = new SliderWithoutPopup(1, 20, 10);
    private final Label zoomPercent = new Label();
    private final ColorAdjust lightGrayColorAdjust = new ColorAdjust();
    private final ToggleGroup viewGroup = new ToggleGroup();
    private final ToggleButton columnView = new ToggleButton("", SVGPathIcons.generateImage(SVGPathIcons.SINGLE_PAGE, "white", 0, 25, lightGrayColorAdjust));
    private final ToggleButton gridView = new ToggleButton("", SVGPathIcons.generateImage(SVGPathIcons.MULTI_PAGE, "white", 0, 25, lightGrayColorAdjust));
    private final ToggleButton editPagesMode = new ToggleButton(TR.tr("footerBar.editPages"));

    private final Label statsElements = new Label();
    private final Label statsTexts = new Label();
    private final Label statsGrades = new Label();
    private final Label statsGraphics = new Label();
    private final Label statsTotalGrade = new Label();
    private final Label status = new Label();

    private final Region spacer = new Region();

    private int oldWidth;
    private final int widthLimit = 1350;

    public FooterBar(){
        StyleManager.putStyle(this, Style.ACCENT);
        getStyleClass().add("app-footer-bar");
        setMaxHeight(20);
        setMinHeight(20);
        setPadding(new Insets(0));
        setBorder(null);
        setup();
    }

    public void setup(){

        // ZOOM INFO
        zoomController.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("footerBar.zoom")));
        zoomPercent.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("footerBar.zoom")));
        zoom.setAlignment(Pos.CENTER_LEFT);
        zoomPercent.setMinWidth(40);
        zoom.setSpacing(5);

        zoomPercent.setText(((int) MainWindow.mainScreen.getZoomPercent()) + "%");
        MainWindow.mainScreen.pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
            zoomPercent.setText(((int) MainWindow.mainScreen.getZoomPercent()) + "%");
            if(zoomController.getValue() != newValue.doubleValue()){
                double scale = newValue.doubleValue();
                double val = 10;

                if(scale < 1){
                    val = scale * 10;
                }else if(scale > 1){
                    val = 10 + (10 * (scale - 1)) / 4;
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
            if(val < 10){
                scale = val / 10;
            }else if(val > 10){
                scale = 1 + (4 * (val - 10)) / 10;
            }
            MainWindow.mainScreen.zoomOperator.zoom(scale, true);
        });

        columnView.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("footerBar.columnView")));
        gridView.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("footerBar.gridView")));
        columnView.setToggleGroup(viewGroup);
        gridView.setToggleGroup(viewGroup);
        PaneUtils.setHBoxPosition(columnView, -1, 19, new Insets(-2, 0, 0, 0));
        PaneUtils.setHBoxPosition(gridView, -1, 19, new Insets(-2, 5, 0, -5));
        ZoomOperator zoomOperator = MainWindow.mainScreen.zoomOperator;
        columnView.setOnAction(e -> zoomOperator.fitWidth(false, false));
        gridView.setOnAction(e -> zoomOperator.fitWidth(false, true));

        columnView.setSelected(true);

        viewGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) viewGroup.selectToggle(oldValue);
            boolean gridView = viewGroup.getSelectedToggle() == this.gridView;
            if(MainWindow.mainScreen.isMultiPagesMode() != gridView) MainWindow.mainScreen.setIsMultiPagesMode(gridView);
        });
        MainWindow.mainScreen.isMultiPagesModeProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && viewGroup.getSelectedToggle() != gridView) gridView.setSelected(true);
            if(!newValue && viewGroup.getSelectedToggle() != columnView) columnView.setSelected(true);
        });

        editPagesMode.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("footerBar.editPages.tooltip")));
        PaneUtils.setHBoxPosition(editPagesMode, -1, 19, new Insets(-2, 0, 0, 0));
        MainWindow.mainScreen.isEditPagesModeProperty().bindBidirectional(editPagesMode.selectedProperty());

        columnView.disableProperty().bind(MainWindow.mainScreen.isEditPagesModeProperty().or(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN)));
        gridView.disableProperty().bind(MainWindow.mainScreen.isEditPagesModeProperty().or(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN)));

        zoom.getChildren().addAll(zoomPercent, zoomController, getSpacerShape(), editPagesMode, getSpacerShape(), columnView, gridView);

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
        root.getChildren().setAll(zoom, spacer, getSpacerShape(), status);
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
    public enum ToastDuration { SHORT(2000), MEDIUM(6000), LONG(10000);
        private final int duration;
        ToastDuration(int duration){
            this.duration = duration;
        }
        public int getDuration(){
            return duration;
        }
    }
    public void showToast(Color background, Color messageColor, String text){
        showToast(background, messageColor, ToastDuration.SHORT, text);
    }
    public void showToast(Color background, Color messageColor, ToastDuration duration, String text){
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

            PlatformUtils.runLaterOnUIThread(duration.duration, () -> {
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
                    root.getChildren().setAll(zoom, spacer, getSpacerShape(), this.status);
                }else{
                    root.getChildren().setAll(zoom, spacer, getSpacerShape(),
                            statsElements, getSpacerShape(), statsTexts, getSpacerShape(), statsGrades, getSpacerShape(), statsGraphics, getSpacerShape(), statsTotalGrade, getSpacerShape(),
                            this.status);
                }

                updateStats();
            }
            zoomController.setDisable(false);
            zoomPercent.setDisable(false);
            editPagesMode.setDisable(false);
            if(MainWindow.mainScreen.document.getLastCursorOverPage() == -1){
                this.status.setText(MainWindow.mainScreen.document.getFileName() + " - " + "?/" + MainWindow.mainScreen.document.numberOfPages);
            }else
                this.status.setText(MainWindow.mainScreen.document.getFileName() + " - " + (MainWindow.mainScreen.document.getLastCursorOverPage() + 1) + "/" + MainWindow.mainScreen.document.numberOfPages);

        }else{
            zoomController.setDisable(true);
            zoomPercent.setDisable(true);
            editPagesMode.setDisable(true);
            if(hard){
                root.getChildren().setAll(zoom, spacer, getSpacerShape(), this.status);
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
                    double grade = GradeTreeView.getTotal().getCore().getVisibleValue();
                    double total = GradeTreeView.getTotal().getCore().getVisibleTotal();

                    if(GradeTreeView.getTotal().getCore().getOutOfTotal() >= 0 && total != 0){
                        grade = grade * GradeTreeView.getTotal().getCore().getOutOfTotal() / total;
                        total = GradeTreeView.getTotal().getCore().getOutOfTotal();
                    }

                    statsTotalGrade.setText(MainWindow.twoDigFormat.format(grade) + "/" + MainWindow.twoDigFormat.format(total));
                }
            });
        }
    }

    public Node getEditPagesModeNode(){
        return editPagesMode;
    }
    public Node getViewModeNode(){
        return gridView;
    }
}
