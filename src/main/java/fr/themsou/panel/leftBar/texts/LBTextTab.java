package fr.themsou.panel.leftBar.texts;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeSection;
import fr.themsou.utils.*;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class LBTextTab extends Tab {

	public VBox pane = new VBox();
	public VBox optionPane = new VBox();

	// OPTIONS DE MISE EN PAGE + INPUTS + BOUTONS
	// Séparés par ligne

	private HBox combosBox = new HBox();
	private ComboBox<String> fontCombo = new ComboBox<>(FontUtils.fonts);
	private ComboBox<Integer> sizeCombo = new ComboBox<>(FontUtils.sizes);

	private HBox colorAndParamsBox = new HBox();
	private ColorPicker colorPicker = new ColorPicker();
	private ToggleButton boldBtn = new ToggleButton("");
	private ToggleButton itBtn = new ToggleButton("");

	public TextArea txtArea = new TextArea();

	private HBox btnBox = new HBox();
	private Button deleteBtn = new Button(TR.tr("Supprimer"));
	public Button newBtn = new Button(TR.tr("Nouveau"));

	// FIELDS

	public boolean isNew = false;

	public String lastFont = "Open Sans";
	public int lastFontSize = 14;
	public String lastColor = "#000000";
	public boolean lastBold = false;
	public boolean lastItalic = false;

	// TREEVIEW
	public TextTreeView treeView;

	// OTHER

	private boolean txtAreaScrollBarListenerIsSetup = false;

	public LBTextTab(){

		setClosable(false);
		setContent(pane);
		setGraphic(Builders.buildImage(getClass().getResource("/img/text.png")+"", 0, 25));
		MainWindow.leftBar.getTabs().add(1, this);

		setup();

		pane.getChildren().addAll(optionPane, treeView);
	}

	public void setup(){

		treeView = new TextTreeView(pane);

		optionPane.setMinWidth(200);
		fontCombo.setCellFactory((ListView<String> stringListView) -> new ShapeCell());
		//fontCombo.setEditable(true);

		Builders.setHBoxPosition(fontCombo, -1, 30, 2.5);
		fontCombo.setStyle("-fx-font-size: 13");
		fontCombo.getSelectionModel().select("Open Sans");
		fontCombo.setMaxHeight(25);
		fontCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
		fontCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastFont = newValue;
		});

		Builders.setHBoxPosition(sizeCombo, 95, 30, 2.5);
		sizeCombo.setStyle("-fx-font-size: 13");
		sizeCombo.getSelectionModel().select(7);
		sizeCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
		sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastFontSize = newValue;
		});

		Builders.setHBoxPosition(colorPicker, -1, 30, 2.5);
		colorPicker.setStyle("-fx-font-size: 13");
		colorPicker.setValue(Color.BLACK);
		colorPicker.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
		colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastColor = newValue.toString();
		});

		Builders.setHBoxPosition(boldBtn, 45, 29, 2.5);
		boldBtn.setCursor(Cursor.HAND);
		boldBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/bold.png")+"", 0, 0));
		boldBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
		boldBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastBold = newValue;
		});

		Builders.setHBoxPosition(itBtn, 45, 29, 2.5);
		itBtn.setCursor(Cursor.HAND);
		itBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/italic.png")+"", 0, 0));
		itBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
		itBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastItalic = newValue;
		});

		Builders.setHBoxPosition(txtArea, -1, 30, 0);
		if(Main.settings.isSmallFontInTextsList()) txtArea.setStyle("-fx-font-size: 12");
		else txtArea.setStyle("-fx-font-size: 13");
		txtArea.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.getSelected() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
		txtArea.setPromptText(TR.tr("Commencez par $ pour écrire du LaTeX"));
		txtArea.setId("no-vertical-scroll-bar");

		Builders.setHBoxPosition(deleteBtn, -1, 30, 2.5);
		deleteBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));

		Builders.setHBoxPosition(newBtn, -1, 30, 2.5);
		newBtn.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));

		combosBox.getChildren().addAll(fontCombo, sizeCombo);
		colorAndParamsBox.getChildren().addAll(colorPicker, boldBtn, itBtn);
		btnBox.getChildren().addAll(deleteBtn, newBtn);

		VBox.setMargin(combosBox, new Insets(2.5, 2.5, 0, 2.5));
		VBox.setMargin(colorAndParamsBox, new Insets(0, 2.5, 0, 2.5));
		VBox.setMargin(txtArea, new Insets(2.5, 5, 2.5, 5));
		VBox.setMargin(btnBox, new Insets(0, 2.5, 7.5, 2.5));
		optionPane.getChildren().addAll(combosBox, colorAndParamsBox, txtArea, btnBox);


		MainWindow.mainScreen.selectedProperty().addListener((ObservableValue<? extends Element> observable, Element oldElement, Element newElement) -> {
			isNew = false;
			if(oldElement != null){
				if(oldElement instanceof TextElement){
					TextElement current = (TextElement) oldElement;
					current.textProperty().unbind();
					current.fontProperty().unbind();

					if(((TextElement) oldElement).getText().isBlank()){
						oldElement.delete();
					}
				}
			}if(newElement != null){
				if(newElement instanceof TextElement){
					TextElement current = (TextElement) newElement;

					txtArea.setText(current.getText());
					boldBtn.setSelected(FontUtils.getFontWeight(current.getFont()) == FontWeight.BOLD);
					itBtn.setSelected(FontUtils.getFontPosture(current.getFont()) == FontPosture.ITALIC);
					colorPicker.setValue(current.getColor());
					fontCombo.getSelectionModel().select(current.getFont().getFamily());
					sizeCombo.getSelectionModel().select((Integer) ((int) current.getFont().getSize()));

					current.fontProperty().bind(Bindings.createObjectBinding(() -> { Edition.setUnsave(); return getFont(); }, fontCombo.getSelectionModel().selectedItemProperty(), sizeCombo.getSelectionModel().selectedItemProperty(), itBtn.selectedProperty(), boldBtn.selectedProperty()));
				}
			}
		});

		ContextMenu menu = new ContextMenu();
		MenuItem deleteReturn = new MenuItem(TR.tr("Supprimer les retours à la ligne inutiles"));
		deleteReturn.setOnAction(event -> {
			String wrapped = new TextWrapper(txtArea.getText().replaceAll(Pattern.quote("\n"), " "), ((TextElement) MainWindow.mainScreen.getSelected()).getFont(), (int) MainWindow.mainScreen.getSelected().getPage().getWidth()).wrap();
			if(txtArea.getText().endsWith(" ")) wrapped += " ";

			if(!wrapped.equals(txtArea.getText())){
				int positionCaret = txtArea.getCaretPosition();
				txtArea.setText(wrapped);
				txtArea.positionCaret(positionCaret);
			}
			Platform.runLater(() -> MainWindow.mainScreen.getSelected().checkLocation(false));
		});
		menu.getItems().add(deleteReturn);
		txtArea.setContextMenu(menu);
		txtArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

			if(newValue.contains("\u0009")){ // TAB
				txtArea.setText(newValue.replaceAll(Pattern.quote("\u0009"), ""));
				return;
			}

			if(!newValue.startsWith("$")){
				// WRAP TEXT
				if(MainWindow.mainScreen.getSelected() == null) return;
				Platform.runLater(() -> {
					if(MainWindow.mainScreen.getSelected() == null) return;
					String wrapped = new TextWrapper(newValue, ((TextElement) MainWindow.mainScreen.getSelected()).getFont(), (int) MainWindow.mainScreen.getSelected().getPage().getWidth()).wrap();
					if(newValue.endsWith(" ")) wrapped += " ";

					if(!wrapped.equals(newValue)){
						int positionCaret = txtArea.getCaretPosition();
						txtArea.setText(wrapped);
						txtArea.positionCaret(positionCaret);
					}
					Platform.runLater(() -> MainWindow.mainScreen.getSelected().checkLocation(false));
				});
			}


			updateHeightAndYLocations(getHorizontalSB(txtArea).isVisible());
			if(!txtAreaScrollBarListenerIsSetup){
				getHorizontalSB(txtArea).visibleProperty().addListener((ObservableValue<? extends Boolean> observableTxt, Boolean oldTxtValue, Boolean newTxtValue) ->  updateHeightAndYLocations(newTxtValue));
				txtAreaScrollBarListenerIsSetup = true;
			}
			((TextElement) MainWindow.mainScreen.getSelected()).setText(newValue);
		});
		sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
			String wrapped = new TextWrapper(txtArea.getText(), ((TextElement) MainWindow.mainScreen.getSelected()).getFont(), (int) MainWindow.mainScreen.getSelected().getPage().getWidth()).wrap();
			if(txtArea.getText().endsWith(" ")) wrapped += " ";

			if(!wrapped.equals(txtArea.getText())){
				int positionCaret = txtArea.getCaretPosition();
				txtArea.setText(wrapped);
				txtArea.positionCaret(positionCaret);
			}
			Platform.runLater(() -> MainWindow.mainScreen.getSelected().checkLocation(false));
		}));
		txtArea.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.DELETE){
				if(txtArea.getCaretPosition() == txtArea.getText().length()){
					Element element = MainWindow.mainScreen.getSelected();
					if(element != null){
						MainWindow.mainScreen.setSelected(null);
						element.delete();
					}
				}
			}else if(e.getCode() == KeyCode.TAB){
				if(MainWindow.leftBar.getSelectionModel().getSelectedIndex() == 1) MainWindow.leftBar.getSelectionModel().select(2);
				else MainWindow.leftBar.getSelectionModel().select(1);
			}
		});
		colorPicker.setOnAction((ActionEvent e) -> {
			if(MainWindow.mainScreen.getSelected() != null){
				if(MainWindow.mainScreen.getSelected() instanceof TextElement){
					((TextElement) MainWindow.mainScreen.getSelected()).setColor(colorPicker.getValue());
					Edition.setUnsave();
				}

			}
		});
		newBtn.setOnAction(e -> {

			PageRenderer page = MainWindow.mainScreen.document.pages.get(0);
			if(MainWindow.mainScreen.document.getCurrentPage() != -1) page = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

			MainWindow.mainScreen.setSelected(null);

			fontCombo.getSelectionModel().select(lastFont.isEmpty() ? "Open Sans" : lastFont);
			sizeCombo.getSelectionModel().select((Integer) lastFontSize);
			colorPicker.setValue(Color.valueOf(lastColor.isEmpty() ? "#000000" : lastColor));
			boldBtn.setSelected(lastBold);
			itBtn.setSelected(lastItalic);

			TextElement current = new TextElement((int) (60 * Element.GRID_WIDTH / page.getWidth()), (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight()), page.getPage(),
					getFont(), txtArea.getText(), colorPicker.getValue(), true);

			page.addElement(current, true);
			MainWindow.mainScreen.setSelected(current);
			isNew = true;

			txtArea.setText("");
			TextTreeView.addSavedElement(current.toNoDisplayTextElement(TextTreeSection.LAST_TYPE, true));
			txtArea.requestFocus();
		});
		deleteBtn.setOnAction(e -> {
			MainWindow.mainScreen.getSelected().delete();
			MainWindow.mainScreen.setSelected(null);
		});
	}

	public void updateHeightAndYLocations(boolean sbIsVisible){

		int lineNumber = txtArea.getParagraphs().size();
		int height = lineNumber >= 3 ? 70 : lineNumber*20+10;

		if(sbIsVisible) height += 16;

		if(txtArea.getHeight() != height){
			txtArea.minHeightProperty().bind(new SimpleDoubleProperty(height));
			deleteBtn.setLayoutY(80 + height);
			newBtn.setLayoutY(80 + height);
		}

	}

	public void selectItem(){
		new Thread(() -> {
			try{
				Thread.sleep(50);
				Platform.runLater(() -> {
					String text = txtArea.getText();
					txtArea.setText(text);
					txtArea.positionCaret(txtArea.getText().length());
					txtArea.requestFocus();
				});
			}catch(InterruptedException e){ e.printStackTrace();}
		}, "selector").start();

	}

	private Font getFont(){

		return FontUtils.getFont(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected(), sizeCombo.getSelectionModel().getSelectedItem());
	}

	public static class ShapeCell extends ListCell<String>{
		@Override
		public void updateItem(String item, boolean empty){
			super.updateItem(item, empty);

			if(empty){
				setText(null);
				setGraphic(null);
			}else{
				setText(item);
				FontUtils.getFont(item, false, false, 14);
				setStyle("-fx-font: 14 \"" + item + "\"");
			}
		}
	}


	private ScrollBar getHorizontalSB(final TextArea scrollPane) {
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
}