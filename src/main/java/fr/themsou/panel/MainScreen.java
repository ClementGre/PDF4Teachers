package fr.themsou.panel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import fr.themsou.document.Document;
import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.AnimatedZoomOperator;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;


public class MainScreen extends Pane {

	public Pane pane = new Pane();

	private IntegerProperty zoom = new SimpleIntegerProperty(Main.settings.getDefaultZoom());
	AnimatedZoomOperator zoomOperator;

	private int totalHeight = 40;
	private int pageWidth = (int) (21 * 37.795275591);

	private IntegerProperty status = new SimpleIntegerProperty(Status.CLOSED);
	public ObjectProperty<Element> selected = new SimpleObjectProperty<>();
	public Document document;

	private Label info = new Label();
	public boolean ctrlDown = false;

	public static class Status {
		public static final int CLOSED = 0;
		public static final int OPEN = 1;
		public static final int ERROR = 2;
	}

	public MainScreen(){

		setup();
		repaint();

	}

	public void repaint(){

		if(status.get() == Status.CLOSED || status.get() == Status.ERROR) {
			info.setVisible(true);

			if(status.get() == Status.CLOSED){
				info.setText(TR.tr("Aucun document ouvert"));
			}else if(status.get() == Status.ERROR){
				info.setText(TR.tr("Impossible de charger ce document"));
			}

		}else{
			info.setVisible(false);
		}

	}
	public void setup(){

		setStyle("-fx-padding: 0;");
		getChildren().add(pane);

		setBackground(new Background(new BackgroundFill(Color.rgb(80, 80, 80), CornerRadii.EMPTY, Insets.EMPTY)));
		pane.setBackground(new Background(new BackgroundFill(Color.rgb(102, 102, 102), CornerRadii.EMPTY, Insets.EMPTY)));
		pane.setBorder(Border.EMPTY);
		setBorder(Border.EMPTY);

		info.setFont(new Font("FreeSans", 22));
		info.setStyle("-fx-text-fill: white;");

		info.translateXProperty().bind(pane.widthProperty().divide(2).subtract(info.widthProperty().divide(2)));
		info.translateYProperty().bind(pane.heightProperty().divide(2).subtract(info.heightProperty().divide(2)));

		pane.getChildren().add(info);

		zoomOperator = new AnimatedZoomOperator(pane, this);

		addEventFilter(ScrollEvent.SCROLL, e -> {

			e.consume();

			// ZOOM
			if(e.isControlDown()){
				ctrlDown = true;


				if(getStatus() == Status.OPEN){

					if(e.getDeltaY() > 0){
						zoomOperator.zoom(1.3, e.getSceneX(), e.getSceneY());
					}else if(e.getDeltaY() < 0){
						zoomOperator.zoom(0.7, e.getSceneX(), e.getSceneY());
					}
					document.updateShowsStatus();
				}
			}else{
				ctrlDown = false;

				if(e.getDeltaY() > 0){
					zoomOperator.scrollUp(200);
				}else if(e.getDeltaY() < 0){
					zoomOperator.scrollDown(200);
				}

			}

		});

		setOnMouseMoved(e -> ctrlDown = e.isControlDown());


		// bind zoom value with the page size
		zoom.addListener((observableValue, oldZoom, newZoom) -> pane.setPrefHeight(pane.getHeight()));

		// bind window's name
		Main.window.titleProperty().bind(Bindings.createStringBinding(() -> status.get() == Status.OPEN ? "PDF4Teachers - " + document.getFile().getName() + (Edition.isSave() ? "" : " "+TR.tr("(Non sauvegardé)")) : TR.tr("PDF4Teachers - Aucun document"), status, Edition.isSaveProperty()));

		setOnMousePressed(e -> {
			if(!(e.getTarget() instanceof Element)){
				setSelected(null);
			}
		});

	}
	public void openFile(File file){

		if(!closeFile(!Main.settings.isAutoSave())){
			return;
		}

		repaint();
		Main.footerBar.repaint();

		try{
			document = new Document(file);
		}catch(IOException e){
			e.printStackTrace();
			failOpen();
			return;
		}

		// FINISH OPEN

		totalHeight = 40;
		status.set(Status.OPEN);

		document.showPages();
		document.loadEdition();

		repaint();
		Main.footerBar.repaint();

	}
	public void failOpen(){

		document = null;
		status.set(Status.ERROR);
		repaint();
		Main.footerBar.repaint();

	}
	public boolean closeFile(boolean confirm){

	    if(document != null){

	    	if(!Edition.isSave()){
				if(confirm){
					if(!document.save()){
						return false;
					}
				}else document.edition.save();
			}
			document.documentSaver.stop();
			document.close();
            document = null;
        }

	    pane.getChildren().clear();

		selected.set(null);

		status.set(Status.CLOSED);
		zoom.set(Main.settings.getDefaultZoom());

		repaint();
		Main.footerBar.repaint();
		if(!Main.hasToClose) Main.settings.setOpenedFile(null);

		return true;
	}
	public boolean hasDocument(boolean confirm){

		if(status.get() != Status.OPEN){
			if(confirm){
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				new JMetro(alert.getDialogPane(), Style.LIGHT);
				Builders.secureAlert(alert);
				alert.setAlertType(Alert.AlertType.ERROR);
				alert.setTitle(TR.tr("Erreur"));
				alert.setHeaderText(TR.tr("Aucun document n'est ouvert !"));
				alert.setContentText(TR.tr("Cette action est censé s'éxécuter sur un document ouvert."));

				alert.showAndWait();
			}
			return false;
		}
		return true;
	}

	public Element getSelected() {
		return selected.get();
	}
	public ObjectProperty<Element> selectedProperty() {
		return selected;
	}
	public void setSelected(Element selected) {
		this.selected.set(selected);
	}

	public IntegerProperty zoomProperty() {
		return zoom;
	}
	public IntegerProperty statusProperty() {
		return status;
	}

	public int getStatus(){
		return this.status.get();
	}

	public int getZoom(){
		return zoom.get();
	}
	public void checkzoom(){
		if(zoom.get() <= 9) zoom.set(10);
		else if(zoom.get() >= 399) zoom.set(400);
		Main.footerBar.repaint();
	}
	public int getPageWidth() {
		return pageWidth;
	}

	public void addPage(PageRenderer page){
		page.setTranslateX(30);
		page.setTranslateY(totalHeight);

		totalHeight += 40 + page.getHeight();

		pane.getChildren().add(page);
	}
	public void finalizePages(){
		pane.setScaleX(1); pane.setScaleY(1);
		pane.setMinWidth(pageWidth + 60.0);
		pane.setMinHeight(totalHeight);

		zoomOperator.setupDocument();

	}
}