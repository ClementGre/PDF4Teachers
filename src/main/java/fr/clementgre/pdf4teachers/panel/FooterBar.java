package fr.clementgre.pdf4teachers.panel;

import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.FontUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.sql.Time;
import java.util.Stack;

public class FooterBar extends StackPane {

	private StackPane messagePane = new StackPane();
	private Label message = new Label();

	private HBox root = new HBox();

	private final HBox zoom = new HBox();
	private final Label zoomInfo = new Label();
	private final Slider zoomController = new Slider(0.05, 5, 1);
	private final Label zoomPercent = new Label();

	private final Label status = new Label();

	public FooterBar(){
		StyleManager.putStyle(this, Style.ACCENT);
		getStyleClass().add("app-footer-bar");
		setMaxHeight(20);
		setup();
	}
	public void setup(){

		MainWindow.mainScreen.pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
			zoomPercent.setText(((int) MainWindow.mainScreen.getZoomPercent()) + "%");
			if(zoomController.getValue() != newValue.doubleValue()){
				zoomController.setValue(newValue.doubleValue());
			}
		});
		PaneUtils.setHBoxPosition(zoomController, 0, 20, 0);
		zoomController.valueProperty().addListener((observable, oldValue, newValue) -> {
			MainWindow.mainScreen.zoomOperator.zoom(newValue.doubleValue());
		});


		zoomInfo.setText(TR.tr("footerBar.zoom"));
		zoom.setSpacing(5);
		zoom.getChildren().addAll(zoomInfo, zoomController, zoomPercent);

		MainWindow.mainScreen.statusProperty().addListener((observable, oldValue, newValue) -> updateStatus(newValue.intValue()));

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		root.getChildren().addAll(zoom, getSpacerShape(), spacer, getSpacerShape(), status);

		root.setPadding(new Insets(0, 10, 0, 10));
		root.setSpacing(10);

		getChildren().add(root);

		messagePane.getChildren().add(message);
		messagePane.setTranslateY(20);
		messagePane.prefWidthProperty().bind(widthProperty());
		messagePane.setPrefHeight(20);
		message.prefWidthProperty().bind(widthProperty());

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
				timelineHide.setOnFinished((e) -> {
					getChildren().remove(messagePane);
				});
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

	public void updateCurrentPage() {
		updateStatus(MainWindow.mainScreen.getStatus());
	}

	public void updateStatus(int status){
		switch (status){
			case MainScreen.Status.CLOSED -> this.status.setText(TR.tr("footerBar.documentStatus.noDocument"));
			case MainScreen.Status.OPEN -> {
				if(MainWindow.mainScreen.document.getCurrentPage() == -1){
					this.status.setText(MainWindow.mainScreen.document.getFileName() + " - " + "?/" + MainWindow.mainScreen.document.totalPages);
				}else{
					this.status.setText(MainWindow.mainScreen.document.getFileName() + " - " + (MainWindow.mainScreen.document.getCurrentPage()+1) + "/" + MainWindow.mainScreen.document.totalPages);
				}
			}
			case MainScreen.Status.ERROR, MainScreen.Status.ERROR_EDITION -> this.status.setText(TR.tr("footerBar.documentStatus.error"));
		}
	}
}
