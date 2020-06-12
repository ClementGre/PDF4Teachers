package fr.themsou.document.editions.elements;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.windows.MainWindow;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;

public abstract class Element extends Region {

	// Size for A4 - 200dpi (Static)
	public static float GRID_WIDTH = 1654;
	public static float GRID_HEIGHT = 2339;

	// ATTRIBUTES

	protected IntegerProperty realX = new SimpleIntegerProperty();
	protected IntegerProperty realY = new SimpleIntegerProperty();

	protected int pageNumber;
	protected int shiftX = 0;
	protected int shiftY = 0;

	public ContextMenu menu = new ContextMenu();

	public Element(int x, int y, int pageNumber){
		this.pageNumber = pageNumber;
		this.realX.set(x);
		this.realY.set(y);
	}

	// SETUP / EVENTS CALLBACK

	protected void setupGeneral(Node... components){
		getChildren().addAll(components);

		// SELECT EVENT
		MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(oldValue == this && newValue != this){
				setEffect(null);
				//setBorder(null);
				menu.hide();
			}else if(oldValue != this && newValue == this){
				DropShadow ds = new DropShadow();
				ds.setOffsetY(3.0f);
				ds.setColor(Color.color(0f, 0f, 0f));
				setEffect(ds);
				setCache(true);
				requestFocus();
				//setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.DOTTED, new CornerRadii(3), new BorderWidths(0.8))));
			}
		});

		layoutXProperty().bind(getPage().widthProperty().multiply(realX.divide(Element.GRID_WIDTH)));
		layoutYProperty().bind(getPage().heightProperty().multiply(realY.divide(Element.GRID_HEIGHT)));

		checkLocation(getLayoutX(), getLayoutY(), false);
		setCursor(Cursor.MOVE);

		//////////////////////////// EVENTS ///////////////////////////////////

		setOnMouseReleased(e -> {
			Edition.setUnsave();
			double itemX = getLayoutX() + e.getX() - shiftX;
			double itemY = getLayoutY() + e.getY() - shiftY;

			checkLocation(itemX, itemY, true);

			PageRenderer newPage = MainWindow.mainScreen.document.getPreciseMouseCurrentPage();
			if(newPage != null){
				if(newPage.getPage() != getPageNumber()){
					MainWindow.mainScreen.setSelected(null);

					switchPage(newPage.getPage());
					itemY = newPage.getPreciseMouseY() - shiftY;
					checkLocation(itemX, itemY, true);

					layoutXProperty().bind(getPage().widthProperty().multiply(realX.divide(Element.GRID_WIDTH)));
					layoutYProperty().bind(getPage().heightProperty().multiply(realY.divide(Element.GRID_HEIGHT)));

					MainWindow.mainScreen.setSelected(this);
				}
			}
			checkLocation(false);
			onMouseRelease();
		});

		setOnMousePressed(e -> {
			e.consume();

			shiftX = (int) e.getX();
			shiftY = (int) e.getY();
			menu.hide();
			select();

			if(e.getButton() == MouseButton.SECONDARY){
				menu.show(getPage(), e.getScreenX(), e.getScreenY());
			}
		});
		setOnMouseDragged(e -> {
			double itemX = getLayoutX() + e.getX() - shiftX;
			double itemY = getLayoutY() + e.getY() - shiftY;

			checkLocation(itemX, itemY, true);
		});
		setOnMouseClicked(Event::consume);

		/////////////////////////////////////////////////////////////////////////

		setupBindings();
		setupMenu();
	}
	protected abstract void setupBindings();
	protected abstract void setupMenu();
	protected abstract void onMouseRelease();

	// CHECKS

	public void checkLocation(boolean allowSwitchPage){
		checkLocation(getLayoutX(), getLayoutY(), allowSwitchPage);
	}
	public void checkLocation(double itemX, double itemY, boolean allowSwitchPage){
		double height = getHeight();
		double width = getWidth();

		if(getPageNumber() == 0 || !allowSwitchPage) if(itemY < 0) itemY = 0;
		if(getPageNumber() == MainWindow.mainScreen.document.totalPages-1 || !allowSwitchPage) if(itemY > getPage().getHeight()-height) itemY = getPage().getHeight()-height;

		if(itemX < 0) itemX = 0;
		if(itemX > getPage().getWidth() - width) itemX = getPage().getWidth() - width;

		realX.set((int) (itemX / getPage().getWidth() * Element.GRID_WIDTH));
		realY.set((int) (itemY / getPage().getHeight() * Element.GRID_HEIGHT));
	}

	// ACTIONS

	public abstract void select();
	protected void selectPartial(){
		MainWindow.mainScreen.setSelected(this);
		toFront();
		getPage().toFront();
	}
	public void delete(){
		if(getPage() != null){
			getPage().removeElement(this, true);
		}
	}
	public void switchPage(int page){
		getPage().switchElementPage(this, MainWindow.mainScreen.document.pages.get(page));
	}

	// READER AND WRITERS

	public abstract LinkedHashMap<Object, Object> getYAMLData();
	protected LinkedHashMap<Object, Object> getYAMLPartialData(){
		LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
		data.put("x", getRealX());
		data.put("y", getRealY());
		return data;
	}

	// GETTERS AND SETTERS

	public abstract float getAlwaysHeight();

	// COORDINATES GETTERS AND SETTERS

	public int getRealX() {
		return realX.get();
	}
	public IntegerProperty RealXProperty() {
		return realX;
	}
	public void setRealX(int x) {
		this.realX.set(x);
	}
	public int getRealY() {
		return realY.get();
	}
	public IntegerProperty RealYProperty() {
		return realY;
	}
	public void setRealY(int y) {
		this.realY.set(y);
	}

	// PAGE GETTERS AND SETTERS

	public PageRenderer getPage() {
		if(MainWindow.mainScreen.document == null) return null;
		if(MainWindow.mainScreen.document.pages.size() > pageNumber){
			return MainWindow.mainScreen.document.pages.get(pageNumber);
		}
		return null;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPage(PageRenderer page) {
		this.pageNumber = page.getPage();
	}
	public void setPage(int pageNumber){
		this.pageNumber = pageNumber;
	}

	// TRANSFORMATIONS

	public abstract Element clone();
	public void cloneOnDocument(){
		Element element = clone();
		element.setRealX(getRealX() + 50);
		element.setRealY(getRealY() + 50);
		element.getPage().addElement(element, true);
		element.select();
	}

}