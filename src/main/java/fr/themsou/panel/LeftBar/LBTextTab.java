package fr.themsou.panel.LeftBar;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.Hand;
import fr.themsou.utils.Location;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class LBTextTab extends Tab {

	public ScrollPane scroller = new ScrollPane();
	public Pane pane = new Pane();

	private ArrayList<TextElement> favorites = new ArrayList<>();
	private ArrayList<TextElement> lasts = new ArrayList<>();
	private int width = 0;
	private int height = 0;
	private int maxWidth = 0;

	private int currentTime = 0;
	private int current = -1;

	public TextElement elementToEdit;

	// Swing elements

	private ComboBox<String> fontCombo; String[] fontNames;
	private ComboBox<Integer> sizeCombo = new ComboBox<>(FXCollections.observableArrayList(6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30, 34, 38, 42));

	private ColorPicker colorPicker = new ColorPicker();

	ToggleButton boldBtn = new ToggleButton("B");
	ToggleButton itBtn = new ToggleButton("I");

	private TextField txtField = new TextField();
	private Button deleteBtn = new Button("Supprimer");
	private Button newBtn = new Button("Nouveau");

	private Canvas canvas;
	private GraphicsContext g;

	public LBTextTab(){

		setClosable(false);
		setContent(scroller);
		scroller.setContent(pane);

		scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

		setGraphic(Builders.buildImage(getClass().getResource("/img/Text.png")+"", 0, 25));
		Main.leftBar.getTabs().add(1, this);

		setup();
		repaint();
	}

	public void repaint(){

	}

	public void setup(){

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontNames = ge.getAvailableFontFamilyNames();
		fontCombo = new ComboBox<>(FXCollections.observableArrayList(fontNames));

		fontCombo.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> stringListView) {

				return new ShapeCell();
			}
		});

		Builders.setPosition(fontCombo, 5, 5, 160, 30, true);
		fontCombo.setStyle("-fx-font-size: 13");
		fontCombo.setCursor(Cursor.HAND);
		fontCombo.getSelectionModel().select("Arial");
		fontCombo.setMaxHeight(25);
		pane.getChildren().add(fontCombo);

		Builders.setPosition(sizeCombo, 170, 5, 95, 30, true);
		sizeCombo.setStyle("-fx-font-size: 13");
		sizeCombo.setCursor(Cursor.HAND);
		sizeCombo.getSelectionModel().select(7);
		pane.getChildren().add(sizeCombo);

		Builders.setPosition(colorPicker, 5, 40, 160, 30, false);
		colorPicker.setStyle("-fx-font-size: 13");
		colorPicker.setCursor(Cursor.HAND);
		pane.getChildren().add(colorPicker);

		Builders.setPosition(boldBtn, 170, 40, 45, 29, true);
		boldBtn.setCursor(Cursor.HAND);
		boldBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		boldBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/Bold.png")+"", 0, 0));
		pane.getChildren().add(boldBtn);

		Builders.setPosition(itBtn, 220, 40, 45, 29, true);
		itBtn.setFont(Font.font("Arial", FontPosture.ITALIC, 20));
		itBtn.setCursor(Cursor.HAND);
		itBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/Italic.png")+"", 0, 0));
		pane.getChildren().add(itBtn);

		Builders.setPosition(txtField, 5, 75, 260, 3, false);
		pane.getChildren().add(txtField);

		Builders.setPosition(deleteBtn, 5, 110, 127.5, 30, false);
		deleteBtn.setCursor(Cursor.HAND);
		pane.getChildren().add(deleteBtn);

		Builders.setPosition(newBtn, 137.5, 110, 127.5, 30, false);
		newBtn.setCursor(Cursor.HAND);
		pane.getChildren().add(newBtn);

		fontCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {

			}
		});
		sizeCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>(){
			@Override public void changed(ObservableValue<? extends Integer> observableValue, Integer oldValue, Integer newValue) {

			}
		});

		txtField.textProperty().addListener((obs, oldText, newText) -> {
			elementToEdit.setContent(newText);
		});

		colorPicker.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent actionEvent) {
				elementToEdit.setColor(colorPicker.getValue());
			}
		});

		boldBtn.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {

			}
		});
		itBtn.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {

			}
		});

		newBtn.setOnMouseReleased(new EventHandler<MouseEvent>(){
			@Override public void handle(MouseEvent mouseEvent) {

				if(Main.mainScreen.document != null){

					if(elementToEdit != null){
						if(txtField.getText().isEmpty()) Main.mainScreen.document.edition.removeElement(elementToEdit);
					}

					elementToEdit = new TextElement(new Location(0, 0), 0, new Font("Arial", 14), "", Color.BLACK);
					Main.mainScreen.document.edition.editRender.hand = new Hand(elementToEdit, new Location(0, 0), Main.mainScreen.document.currentPage);
					txtField.setText("");
					txtField.requestFocus();
				}else{
					JOptionPane.showMessageDialog(null, "Vous devez ouvrir un document");
				}
		}});

		deleteBtn.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				Main.mainScreen.document.edition.removeElement(elementToEdit);
				elementToEdit = null;
			}
		});
	}


	public void selectTextElement(Element element) {
		elementToEdit = (TextElement) element;

		fontCombo.getSelectionModel().select(((TextElement) element).getFont().getName());
		sizeCombo.getSelectionModel().select((int) ((TextElement) element).getFont().getSize());
		colorPicker.setValue(((TextElement) element).getColor());
		txtField.setText(((TextElement) element).getContent());

	}

	private void refreshFont(){

		FontWeight fontWeight = FontWeight.NORMAL;
		FontPosture fontPosture = FontPosture.REGULAR;
		if(boldBtn.isSelected()) fontWeight = FontWeight.BOLD;
		if(itBtn.isSelected()) fontPosture = FontPosture.ITALIC;

		elementToEdit.setFont(Font.font(fontCombo.getSelectionModel().getSelectedItem(), fontWeight, fontPosture, sizeCombo.getSelectionModel().getSelectedItem()));
	}

	private String toHexString(Color colour) throws NullPointerException {
		String hexColour = String.format("#%02x%02x%02x", (int)colour.getRed(), (int)colour.getGreen(), (int)colour.getBlue());
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
		}
		return "#" + hexColour;
	}

	public class ShapeCell extends ListCell<String>{
		@Override
		public void updateItem(String item, boolean empty){
			super.updateItem(item, empty);

			if(empty){
				setText(null);
				setGraphic(null);
			}else{
				setText(item);
				setStyle("-fx-font: 14 \"" + item + "\"");
			}
		}
	}
}
