package fr.themsou.panel;

import java.io.File;
import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class MainScreen extends ScrollPane {

	private int zoom = 100;
	private IntegerProperty pageWidth = new SimpleIntegerProperty(595);
	private int status = 0;
	private int lastStatus = 0;

	public Document document;
	public Pane pane = new Pane();

	private Label info = new Label();

	public MainScreen(){

		setup();
		repaint();
	}

	public void repaint(){

		if(status != -1){
			info.setVisible(true);
			if(status == 0){
				info.setText("Aucun document ouvert");
			}else if(status == 1){
				info.setText("Chargement du document...");
			}else if(status == 2){
				info.setText("Une erreur est survenue lors du chargement du document :/");
			}

		}else{
			info.setVisible(false);

		}

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

		pane.getChildren().add(info);

	}
	public void openFile(File file){
		
		if(!closeFile(true)){
			return;
		}
		setCursor(Cursor.WAIT);
        status = 1;
		this.document = new Document(file);

		if(document.renderPDFPages()){
			status = -1;
			Main.window.setTitle("PDF Teacher - " + file.getName());
		}

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

        Main.lbTextTab.elementToEdit = null;

		status = 0;
		zoom = 150;
		//lastWidth = 0;
		//setPreferredSize(new Dimension(0, 0));
		//Main.mainScreenScroll.updateUI();
		Main.window.setTitle("PDF Teacher - Aucun document");

		return true;
	}

	public boolean hasDocument(boolean alert){

		if(status != -1){
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

	public void setStatus(int status){
		this.lastStatus = this.status;
		this.status = status;
	}
	public int getStatus(){
		return this.status;
	}

	public int getZoom(){
		return this.zoom;
	}
	public void zoomMore(){
		this.zoom += 5;
		checkzoom();
	}
	public void zoomLess(){
		this.zoom -=5;
		checkzoom();
		System.out.println("zoomless");
	}
	public void checkzoom(){
		if(zoom <= 9) zoom = 10;
		else if(zoom >= 399) zoom = 400;
	}
	public void setZoom(int zoom){
		this.zoom = zoom;
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
		pane.minWidthProperty().bind( page.widthProperty().add(100) );


		page.layoutYProperty().bind(page.heightProperty().add(50).multiply(page.getPage()).add(50));
		page.layoutXProperty().bind(pane.widthProperty().divide(2).subtract(page.widthProperty().divide(2)));

		pane.getChildren().add(page);


	}

}
