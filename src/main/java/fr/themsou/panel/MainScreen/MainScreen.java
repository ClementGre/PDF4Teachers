package fr.themsou.panel.MainScreen;

import java.io.File;
import java.io.IOException;
import fr.themsou.document.Document;
import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.render.convert.ConvertDocument;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

public class MainScreen extends Pane {

	public Pane pane = new Pane();
	public ZoomOperator zoomOperator;

	private int pageWidth = 596;

	public double paneMouseX = 0;
	public double paneMouseY = 0;

	public double mouseX = 0;
	public double mouseY = 0;

	private IntegerProperty status = new SimpleIntegerProperty(Status.CLOSED);
	public ObjectProperty<Element> selected = new SimpleObjectProperty<>();
	public Document document;

	private Label info = new Label();
	private Hyperlink infoLink = new Hyperlink();

	public static class Status {
		public static final int CLOSED = 0;
		public static final int OPEN = 1;
		public static final int ERROR = 2;
		public static final int ERROR_EDITION = 3;
	}

	int dragNScrollFactor = 0;
	double dragStartX;
	double dragStartY;

	public Thread dragNScrollThread = new Thread(() -> {
		while(true){
			if(dragNScrollFactor != 0){
				Platform.runLater(() -> {
					if(dragNScrollFactor < 0){
						zoomOperator.scrollUp((dragNScrollFactor+50)/2, true);
					}else if(dragNScrollFactor > 0){
						zoomOperator.scrollDown(dragNScrollFactor/2, true);
					}
				});
				try{ Thread.sleep(20); }catch(InterruptedException ex){ ex.printStackTrace(); }
			}else{
				try{ Thread.sleep(200); }catch(InterruptedException ex){ ex.printStackTrace(); }
			}

		}

	}, "DragNScroll");

	public MainScreen(){

		setup();
		repaint();

	}

	public void repaint(){
		if(status.get() != Status.OPEN) {
			info.setVisible(true);

			if(status.get() == Status.CLOSED){
				infoLink.setVisible(true);
				info.setText(TR.tr("Aucun document ouvert"));
				infoLink.setText(TR.tr("Convertir des images en documents PDF"));
				infoLink.setOnAction(e -> new ConvertDocument());

			}else if(status.get() == Status.ERROR){
				info.setText(TR.tr("Impossible de charger ce document") + "\n\n" +
						TR.tr("Vérifiez que le fichier n'est pas corrompu") + "\n" +
						TR.tr("et que l'utilisateur a les droits de lecture sur ce fichier."));
				infoLink.setVisible(false);

			}else if(status.get() == Status.ERROR_EDITION){
				infoLink.setVisible(true);
				info.setText(TR.tr("Impossible de charger l'édition du document") + "\n\n" +
						TR.tr("Supprimez l'édition ou réparez la en modifiant le fichier d'éditions (YAML) dans :"));
				infoLink.setText(Main.dataFolder + "editions" + File.separator);
				infoLink.setOnAction(e -> Main.hostServices.showDocument(Main.dataFolder + "editions" + File.separator));
			}
		}else{
			info.setVisible(false);
			infoLink.setVisible(false);
		}
	}
	public void setup(){

		setStyle("-fx-padding: 0; -fx-background-color: #484848;");
		setBorder(Border.EMPTY);

		pane.setStyle("-fx-background-color: #484848;");
		pane.setBorder(Border.EMPTY);
		getChildren().add(pane);

		info.setStyle("-fx-text-fill: white; -fx-font-size: 22;");
		info.setTextAlignment(TextAlignment.CENTER);

		info.translateXProperty().bind(widthProperty().divide(2).subtract(info.widthProperty().divide(2)));
		info.translateYProperty().bind(heightProperty().divide(2).subtract(info.heightProperty().divide(2)));
		getChildren().add(info);

		infoLink.setStyle("-fx-text-fill: white; -fx-font-size: 15;");
		infoLink.setLayoutY(60);
		infoLink.setTextAlignment(TextAlignment.CENTER);

		infoLink.translateXProperty().bind(widthProperty().divide(2).subtract(infoLink.widthProperty().divide(2)));
		infoLink.translateYProperty().bind(heightProperty().divide(2).subtract(infoLink.heightProperty().divide(2)));
		getChildren().add(infoLink);

		zoomOperator = new ZoomOperator(pane, this);

		// Update show status when scroll level change
		pane.translateYProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			if(document != null){
				Platform.runLater(() -> document.updateShowsStatus());
			}
		});
		pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
			if(document != null){
				Platform.runLater(() -> document.updateZoom());
			}
		});

		addEventFilter(ZoomEvent.ZOOM, (ZoomEvent e) -> {
			if(getStatus() == Status.OPEN){
				zoomOperator.zoom(e.getZoomFactor(), e.getX(), e.getY());
			}
		});

		addEventFilter(ScrollEvent.SCROLL, e -> {
			if(e.isControlDown()){ // ZOOM

				if(getStatus() == Status.OPEN){
					if(e.getDeltaY() < 0){
						zoomOperator.zoom(1+e.getDeltaY()/200, e.getSceneX(), e.getSceneY());
					}else if(e.getDeltaY() > 0){
						zoomOperator.zoom(1+e.getDeltaY()/200, e.getSceneX(), e.getSceneY());
					}
				}
			}else{ // SCROLL

				if(e.getDeltaY() != 0){
					if(e.getDeltaY() > 0){
						zoomOperator.scrollUp((int) (e.getDeltaY() * 2.5), false);
					}else{
						zoomOperator.scrollDown((int) (-e.getDeltaY() * 2.5), false);
					}
				}

				if(e.getDeltaX() != 0){
					if(e.getDeltaX() > 0){
						zoomOperator.scrollLeft((int) (e.getDeltaX() * 2.5), false);
					}else{
						zoomOperator.scrollRight((int) (-e.getDeltaX() * 2.5), false);
					}
				}

			}

		});
		heightProperty().addListener((observable, oldValue, newValue) -> {
			if(document != null){
				Platform.runLater(() -> document.updateShowsStatus());
			}
		});

		setOnMouseDragged(e -> {
			if(!(((Node) e.getTarget()).getParent() instanceof Element) && !(e.getTarget() instanceof Element)){ // GrabNScroll
				double distY = e.getY() - dragStartY;
				double distX = e.getX() - dragStartX;

				if(distY > 0){
					zoomOperator.scrollUp((int) distY, true);
				}else if(distY < 0){
					zoomOperator.scrollDown((int) -distY, true);
				}

				if(distX > 0){
					zoomOperator.scrollLeft((int) distX, true);
				}else if(distX < 0){
					zoomOperator.scrollRight((int) -distX, true);
				}
			}else{ // DragNScroll with an Element
				double y = Math.max(1, Math.min(getHeight(), e.getY()));
				if(y < 50){
					dragNScrollFactor = (int) (y*-1);
				}else if(getHeight() - y < 50){
					dragNScrollFactor = (int) ((getHeight()-y)*-1 + 50);
				}else{
					dragNScrollFactor = 0;
				}
			}

			dragStartY = e.getY();
			dragStartX = e.getX();

			mouseY = e.getY();
			mouseX = e.getX();
		});
		setOnMousePressed(e -> {
			dragStartX = e.getX();
			dragStartY = e.getY();
			setSelected(null);
			if(hasDocument(false)) setCursor(Cursor.CLOSED_HAND);
		});
		setOnMouseReleased(e -> {
			dragNScrollFactor = 0;
			setCursor(Cursor.DEFAULT);
		});
		setOnMouseMoved(e -> {
			mouseY = e.getY();
			mouseX = e.getX();
		});
		pane.setOnMouseMoved(e -> {
			paneMouseY = e.getY();
			paneMouseX = e.getX();
		});
		pane.setOnMouseDragged(e -> {
			paneMouseY = e.getY();
			paneMouseX = e.getX();
		});

		// bind window's name
		Main.window.titleProperty().bind(Bindings.createStringBinding(() -> status.get() == Status.OPEN ? "PDF4Teachers - " + document.getFile().getName() + (Edition.isSave() ? "" : " "+TR.tr("(Non sauvegardé)")) : TR.tr("PDF4Teachers - Aucun document"), status, Edition.isSaveProperty()));

		// Start the Drag and Scroll Thread
		dragNScrollThread.start();

	}
	public void openFile(File file){

		if(!closeFile(!Main.settings.isAutoSave())){
			return;
		}

		repaint();
		MainWindow.footerBar.repaint();

		try{
			document = new Document(file);
		}catch(IOException e){
			e.printStackTrace();
			failOpen();
			return;
		}

		// FINISH OPEN
		MainWindow.footerBar.leftInfo.textProperty().bind(Bindings.createStringBinding(() -> TR.tr("zoom") + " : " + (int) (pane.getScaleX()*100) + "%", pane.scaleXProperty()));

		status.set(Status.OPEN);

		document.showPages();
		try{
			document.loadEdition();
		}catch(Exception e){
			System.err.println("ERREUR : Impossible de changer l'édition");
			e.printStackTrace();
			closeFile(false);
			status.set(Status.ERROR_EDITION);
		}

		repaint();
		MainWindow.footerBar.repaint();
		Platform.runLater(() -> zoomOperator.updatePaneHeight(0, 0.5));
	}
	public void failOpen(){

		document = null;
		status.set(Status.ERROR);
		repaint();
		MainWindow.footerBar.repaint();

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

		pane.setScaleX(Main.settings.getDefaultZoom()/100.0);
		pane.setScaleY(Main.settings.getDefaultZoom()/100.0);

		pane.setPrefHeight(1);
		pane.setPrefWidth(1);

		status.set(Status.CLOSED);
		selected.set(null);

		repaint();
		MainWindow.footerBar.repaint();

		System.runFinalization();
		return true;
	}
	public boolean hasDocument(boolean confirm){

		if(status.get() != Status.OPEN){
			if(confirm){
				Alert alert = Builders.getAlert(Alert.AlertType.ERROR, TR.tr("Erreur"));
				alert.setHeaderText(TR.tr("Aucun document n'est ouvert !"));
				alert.setContentText(TR.tr("Cette action est censée s'éxécuter sur un document ouvert"));

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

	public IntegerProperty statusProperty() {
		return status;
	}
	public int getStatus(){
		return this.status.get();
	}

	public double getZoomFactor(){
		return pane.getScaleX();
	}
	public double getZoomPercent(){
		return getZoomFactor()*100;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public void addPage(PageRenderer page){
		pane.getChildren().add(page);
	}
	public void updateSize(int totalHeight){

		pane.setPrefWidth(pageWidth + 60.0);
		pane.setPrefHeight(totalHeight);
	}
}