package fr.themsou.panel;

import java.io.File;
import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;


public class MainScreen extends ScrollPane {

	private IntegerProperty zoom = new SimpleIntegerProperty(Main.settings.getDefaultZoom());
	private int defaultPageWidth;
	private IntegerProperty pageWidth = new SimpleIntegerProperty(defaultPageWidth);
	private IntegerProperty status = new SimpleIntegerProperty(0);

	public ObjectProperty<Element> selected = new SimpleObjectProperty<>();


	public Document tmpDocument;
	public Document document;
	public Pane pane = new Pane();

	private Label info = new Label();
	private ProgressBar loader = new ProgressBar();

	public MainScreen(int defaultPageWidth){

		this.defaultPageWidth = defaultPageWidth;

		setup();
		repaint();
	}


	public void repaint(){

		if(status.get() != -1 && status.get() != 1) {
			loader.setVisible(true);
			info.setVisible(true);

			if(status.get() == 0){
				info.setText("Aucun document ouvert");
				loader.setVisible(false);
			}else if(status.get() == 2){
				info.setText("Une erreur est survenue lors du chargement du document :/");
				loader.setVisible(false);
			}

		}else if(status.get() == 1){
			loader.setVisible(true);
			info.setVisible(false);
		}else{
			loader.setVisible(false);
			info.setVisible(false);
		}

		pane.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent e) {
				if(e.isControlDown()){
					if(e.getDeltaY() > 0) zoomMore();
					if(e.getDeltaY() < 0) zoomLess();
				}
			}
		});
	}
	public void setup(){

		setStyle("-fx-padding: 0;");
		setContent(pane);

		setFitToHeight(true);
		setFitToWidth(true);

		pane.setBackground(new Background(new BackgroundFill(Color.rgb(102, 102, 102), CornerRadii.EMPTY, Insets.EMPTY)));
		pane.setBorder(Border.EMPTY);
		setBorder(Border.EMPTY);

		info.setFont(new Font("FreeSans", 22));
		info.setStyle("-fx-text-fill: white;");

		info.translateXProperty().bind(pane.widthProperty().divide(2).subtract(info.widthProperty().divide(2)));
		info.translateYProperty().bind(pane.heightProperty().divide(2).subtract(info.heightProperty().divide(2)));

		loader.setPrefWidth(300);
		loader.setPrefHeight(20);
		loader.translateXProperty().bind(pane.widthProperty().divide(2).subtract(loader.widthProperty().divide(2)));
		loader.translateYProperty().bind(pane.heightProperty().divide(2).subtract(loader.heightProperty().divide(2)));

		pageWidth.bind(zoom.multiply(defaultPageWidth).divide(100));

		pane.getChildren().add(loader);
		pane.getChildren().add(info);

		zoom.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldZoom, Number newZoom) {
				pane.setPrefHeight(pane.getHeight());
			}
		});

	}
	public void openFile(File file){

		if(!closeFile(!Main.settings.isAutoSave())){
			return;
		}

		new Thread(new Runnable() {
			public void run(){

			}
		}, "loader").start();

		setCursor(Cursor.WAIT);
        status.set(1);
		repaint();
		Main.footerBar.repaint();


		new Thread(new Runnable() {
			@Override
			public void run() {
				tmpDocument = new Document(file);
				tmpDocument.renderPDFPages();

				Platform.runLater(new Runnable() {
					public void run() {
						if(tmpDocument.hasRendered()){
							finishOpen();
						}else{
							failOpen();
						}

					}
				});
			}
		}, "loader").start();

	}

	public void finishOpen(){

		this.document = this.tmpDocument;
		tmpDocument = null;

		status.set(-1);
		document.showPages();
		document.loadEdition();
		Main.window.setTitle("PDF Teacher - " + document.getFile().getName());

		setHvalue(0.5);
		setVvalue(0);
		setCursor(Cursor.DEFAULT);

		repaint();
		Main.footerBar.repaint();

	}
	public void failOpen(){

		this.tmpDocument = null;
		status.set(2);
		setCursor(Cursor.DEFAULT);
		repaint();
		Main.footerBar.repaint();

	}
	public boolean closeFile(boolean confirm){

	    if(document != null){
	    	if(confirm){
	    		if(!document.save()){
	    			return false;
				}
			}
			else document.edition.save();

            document = null;
        }

	    pane.getChildren().clear();
		pane.getChildren().add(info);
		pane.getChildren().add(loader);
		pane.minHeightProperty().unbind();
		pane.minWidthProperty().unbind();
		pane.setMinWidth(0);
		pane.setMinHeight(0);

		selected.set(null);

		status.set(0);
		zoom.set(Main.settings.getDefaultZoom());

		repaint();
		Main.footerBar.repaint();

		Main.window.setTitle("PDF Teacher - Aucun document");

		return true;
	}

	public boolean hasDocument(boolean confirm){

		if(status.get() != -1){
			if(confirm){
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				new JMetro(alert.getDialogPane(), Style.LIGHT);
				Builders.secureAlert(alert);
				alert.setAlertType(Alert.AlertType.ERROR);
				alert.setTitle("Erreur");
				alert.setHeaderText("Aucun document n'est ouvert !");
				alert.setContentText("Cette action est censé s'éxécuter sur un document ouvert.");

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
	public void setStatus(int status){
		this.status.set(status);
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
	public void zoomMore(){
		this.zoom.set(zoom.get() + 5);
		checkzoom();
	}
	public void zoomLess(){
		this.zoom.set(zoom.get() - 5);
		checkzoom();
	}
	public void checkzoom(){
		if(zoom.get() <= 9) zoom.set(10);
		else if(zoom.get() >= 399) zoom.set(400);
		Main.footerBar.repaint();
	}
	public void setZoom(int zoom){
		this.zoom.set(zoom);
		Main.footerBar.repaint();
	}
	public int getPageWidth() {
		return pageWidth.get();
	}
	public IntegerProperty pageWidthProperty() {
		return pageWidth;
	}
	public void setPageWidth(int pageWidth) {
		this.pageWidth.set(pageWidth);
	}

	public void addPage(PageRenderer page){

		pane.minHeightProperty().bind( page.heightProperty().add(50).multiply(page.getPage()+1).add(50) );
		pane.minWidthProperty().bind( page.widthProperty().add(100));

		page.layoutYProperty().bind(page.heightProperty().add(50).multiply(page.getPage()).add(50));
		page.layoutXProperty().bind(pane.widthProperty().divide(2).subtract(page.widthProperty().divide(2)));

		pane.getChildren().add(page);
	}
}
