package fr.themsou.panel;

import java.io.File;
import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class MainScreen extends ScrollPane {

	private IntegerProperty zoom = new SimpleIntegerProperty(100);
	private int defaultPageWidth;
	private IntegerProperty pageWidth = new SimpleIntegerProperty(defaultPageWidth);
	private IntegerProperty status = new SimpleIntegerProperty(0);
	private boolean hasRender = false;

	public ObjectProperty<Element> selected = new SimpleObjectProperty<>();


	public Document document;
	public Pane pane = new Pane();

	private Label info = new Label();

	public MainScreen(int defaultPageWidth){

		this.defaultPageWidth = defaultPageWidth;

		setup();
		repaint();
	}


	public void repaint(){

		if(status.get() != -1){
			info.setVisible(true);
			if(status.get() == 0){
				info.setText("Aucun document ouvert");
			}else if(status.get() == 1){
				info.setText("Chargement du document...");
			}else if(status.get() == 2){
				info.setText("Une erreur est survenue lors du chargement du document :/");
			}

		}else{
			info.setVisible(false);

		}

		pane.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent e) {
				if(e.isControlDown()){
					if(e.getDeltaY() > 0){
						zoomLess();
					}
					if(e.getDeltaY() < 0) zoomMore();
				}
			}
		});

	}
	public void setup(){

		setContent(pane);
		getChildren().add(info);

		setFitToHeight(true);
		setFitToWidth(true);

		pane.setBackground(new Background(new BackgroundFill(Color.rgb(102, 102, 102), CornerRadii.EMPTY, Insets.EMPTY)));
		pane.setBorder(Border.EMPTY);
		setBorder(Border.EMPTY);

		info.setFont(new Font("FreeSans", 22));
		info.setTextFill(Color.WHITE);

		info.translateXProperty().bind(pane.widthProperty().divide(2).subtract(info.widthProperty().divide(2)));
		info.translateYProperty().bind(pane.heightProperty().divide(2).subtract(info.heightProperty().divide(2)));

		pageWidth.bind(zoom.multiply(defaultPageWidth).divide(100));

		pane.getChildren().add(info);

	}
	public void openFile(File file){
		
		if(!closeFile(true)){
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

		hasRender = false;


		Platform.runLater(new Runnable() {
			public void run() {

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				new Thread(new Runnable() {
					@Override
					public void run() {
						document = new Document(file);
						if(document.renderPDFPages()){
						}else{
							document = null;
						}
						hasRender = true;
					}
				}, "loader").start();

				int i = 0;
				while(true) {

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i++;

					if (hasRender) {
						if (document.hasRendered()) {
							finishOpen();
						} else if (document == null) {
							failOpen();
						}
						break;
					}

					if (i >= 200) {
						failOpen();
						break;
					}
				}
			}
		});
	}

	public void finishOpen(){

		status.set(-1);
		document.showPages();
		Main.window.setTitle("PDF Teacher - " + document.getFile().getName());

		setHvalue(0.5);
		setVvalue(0);
		setCursor(Cursor.DEFAULT);

		repaint();
		Main.footerBar.repaint();

	}
	public void failOpen() {

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
		selected.set(null);

		status.set(0);
		zoom.set(100);

		repaint();
		Main.footerBar.repaint();

		Main.window.setTitle("PDF Teacher - Aucun document");

		return true;
	}

	public boolean hasDocument(boolean alert){

		if(status.get() != -1){
			if(alert){
				Alert alerte = new Alert(Alert.AlertType.INFORMATION);
				alerte.setAlertType(Alert.AlertType.ERROR);
				alerte.setTitle("Erreur");
				alerte.setHeaderText("Aucun document n'est ouvert !");
				alerte.setContentText("Cette action est censé s'éxécuter sur un document ouvert.");

				alerte.showAndWait();
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
