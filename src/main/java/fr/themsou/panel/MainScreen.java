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
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;


public class MainScreen extends ScrollPane {

	private IntegerProperty zoom = new SimpleIntegerProperty(Main.settings.getDefaultZoom());
	private int defaultPageWidth;

	private IntegerProperty pageWidth = new SimpleIntegerProperty(defaultPageWidth);
	private ArrayList<ReadOnlyDoubleProperty> pageHeights = new ArrayList<>();

	private IntegerProperty status = new SimpleIntegerProperty(Status.CLOSED);

	public ObjectProperty<Element> selected = new SimpleObjectProperty<>();

	public Document document;
	public VBox pane = new VBox();

	private Label info = new Label();

	public boolean ctrlDown = false;

	public boolean lastVerticalScrollIsVisible = true;
	public boolean lastHorizontalScrollIsVisible = true;
	public double lastVerticalScrollValue = 0;
	public double lastHorizontalScrollValue = 0.5;

	public static class Status {
		public static final int CLOSED = 0;
		public static final int OPEN = 1;
		public static final int ERROR = 2;
	}

	public MainScreen(int defaultPageWidth){

		this.defaultPageWidth = defaultPageWidth;

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

		pageWidth.bind(zoom.multiply(defaultPageWidth).divide(100));

		pane.getChildren().add(info);


		final double SPEED = 3;
		getContent().setOnScroll(scrollEvent -> {
			double deltaY = scrollEvent.getDeltaY() / pane.getHeight() * SPEED;
			setVvalue(getVvalue() - deltaY);
		});


		addEventFilter(ScrollEvent.SCROLL, e -> {

			// prevent scroll on zoom
			if(e.isControlDown()){
				ctrlDown = true;
				e.consume();

				if(getStatus() == Status.OPEN){
					if(e.getDeltaY() > 0) zoomMore();
					if(e.getDeltaY() < 0) zoomLess();

					setVvalue(lastVerticalScrollValue);
					setHvalue(lastHorizontalScrollValue);
				}


			}else ctrlDown = false;

			// Check on ScrollBar switch to visible state
			// In order to set the default value to 0.5
			boolean newVisible = getVerticalSB(Main.mainScreen).isVisible();
			if(!lastVerticalScrollIsVisible && newVisible){
				lastVerticalScrollIsVisible = true;
				lastVerticalScrollValue = 0.5;
				setVvalue(0.5);
			}
			lastVerticalScrollIsVisible = newVisible;

			newVisible = getHorizontalSB(Main.mainScreen).isVisible();
			if(!lastHorizontalScrollIsVisible && newVisible){
				lastHorizontalScrollIsVisible = true;
				lastHorizontalScrollValue = 0.5;
				setHvalue(0.5);
			}
			lastHorizontalScrollIsVisible = newVisible;

			lastVerticalScrollValue = getVvalue();
			lastHorizontalScrollValue = getHvalue();
		});

		setOnMouseMoved(e -> ctrlDown = e.isControlDown());

		vvalueProperty().addListener((observable, oldValue, newValue) -> {
			if(!ctrlDown){
				lastVerticalScrollValue = newValue.doubleValue();
			}
		});
		hvalueProperty().addListener((observable, oldValue, newValue) -> {
			if(!ctrlDown){
				lastHorizontalScrollValue = newValue.doubleValue();
			}
		});
		// end

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

		pageHeights = new ArrayList<>();

		try{
			document = new Document(file);
		}catch(IOException e){
			e.printStackTrace();
			failOpen();
			return;
		}

		// FINISH OPEN

		status.set(Status.OPEN);
		document.showPages();
		document.loadEdition();

		setHvalue(0.5);
		setVvalue(0);
		lastHorizontalScrollValue = 0.5;
		lastHorizontalScrollValue = 0;
		lastHorizontalScrollIsVisible = true;
		lastVerticalScrollIsVisible = true;

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
		pane.getChildren().add(info);
		pane.minHeightProperty().unbind();
		pane.minWidthProperty().unbind();
		pane.setMinWidth(0);
		pane.setMinHeight(0);

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
	public IntegerProperty pageWidthProperty() {
		return pageWidth;
	}

	public void addPage(PageRenderer page){

		// BIND PAGE X
		page.translateXProperty().bind(pane.widthProperty().divide(2).subtract(page.widthProperty().divide(2)));

		VBox.setMargin(page, new Insets(15, 0, 15, 0));

		pageHeights.add(page.heightProperty());
		pane.getChildren().add(page);
	}
	public void finalizePages(){

		pane.minWidthProperty().bind(document.pages.get(0).widthProperty().add(60));

		DoubleBinding heightBindings = null;
		for(ReadOnlyDoubleProperty height : pageHeights){
			if(heightBindings == null){
				heightBindings = height.add(30);
			}else{
				heightBindings = heightBindings.add(height).add(30);
			}
		}

		if(heightBindings != null){
			heightBindings = heightBindings.add(65);
			pane.minHeightProperty().bind(heightBindings);
		}
		pageHeights = new ArrayList<>();
	}

	private ScrollBar getHorizontalSB(final ScrollPane scrollPane) {
		Set<Node> nodes = scrollPane.lookupAll(".scroll-bar");
		for (final Node node : nodes) {
			if (node instanceof ScrollBar) {
				ScrollBar sb = (ScrollBar) node;
				if(sb.getOrientation() == Orientation.HORIZONTAL){
					return sb;
				}
			}
		}
		return null;
	}
	private ScrollBar getVerticalSB(final ScrollPane scrollPane) {
		Set<Node> nodes = scrollPane.lookupAll(".scroll-bar");
		for (final Node node : nodes) {
			if (node instanceof ScrollBar) {
				ScrollBar sb = (ScrollBar) node;
				if(sb.getOrientation() == Orientation.VERTICAL){
					return sb;
				}
			}
		}
		return null;
	}
}
