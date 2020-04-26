package fr.themsou.panel.leftBar.texts;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.utils.*;
import fr.themsou.utils.sort.SortEvent;
import fr.themsou.utils.sort.SortManager;
import fr.themsou.utils.sort.Sorter;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class LBTextTab extends Tab {

	public VBox pane = new VBox();
	public VBox optionPane = new VBox();

	// OPTIONS DE MISE EN PAGE + INPUTS + BOUTONS
	// Séparés par ligne

	private HBox combosBox = new HBox();
	private ComboBox<String> fontCombo = new ComboBox<>(Element.fonts);
	private ComboBox<Integer> sizeCombo = new ComboBox<>(Element.sizes);

	private HBox colorAndParamsBox = new HBox();
	private ColorPicker colorPicker = new ColorPicker();
	private ToggleButton boldBtn = new ToggleButton("");
	private ToggleButton itBtn = new ToggleButton("");

	private TextArea txtArea = new TextArea();

	private HBox btnBox = new HBox();
	private Button deleteBtn = new Button(TR.tr("Supprimer"));
	private Button newBtn = new Button(TR.tr("Nouveau"));

	// TREE VIEW

	public TreeView treeView = new TreeView<>();
	public TreeItem<String> treeViewRoot = new TreeItem<>();

	public TreeItem<String> favoritesText = new TreeItem<>("favoritesText");
	public TreeItem<String> favoritesTextOptionsItem = new TreeItem("favoritesOptions");
	public ToggleButton favoritesTextToggleOption = new ToggleButton("");
	public GridPane favoritesTextOptions = new GridPane();
	public SortManager favoritesTextSortManager;

	public TreeItem<String> lastsText = new TreeItem<>("lastsText");
	public TreeItem<String> lastsTextOptionsItem = new TreeItem("lastsOptions");
	public ToggleButton lastsTextToggleOption = new ToggleButton("");
	public GridPane lastsTextOptions = new GridPane();
	public SortManager lastsTextSortManager;

	public TreeItem<String> onFileText = new TreeItem<>("onFileText");
	public TreeItem<String> onFileTextOptionsItem = new TreeItem("onFileOptions");
	public ToggleButton onFileTextToggleOption = new ToggleButton("");
	public GridPane onFileTextOptions = new GridPane();
	public SortManager onFileTextSortManager;

	public boolean isNew = false;

	public String lastFont = "Arial";
	public int lastFontSize = 14;
	public String lastColor = "#000000";
	public boolean lastBold = false;
	public boolean lastItalic = false;

	// OTHER

	private boolean txtAreaScrollBarListenerIsSetup = false;

	public LBTextTab(){

		setClosable(false);
		setContent(pane);
		setGraphic(Builders.buildImage(getClass().getResource("/img/Text.png")+"", 0, 25));
		Main.leftBar.getTabs().add(1, this);

		setup();

		pane.getChildren().addAll(optionPane, treeView);
	}

	public void setup(){

		optionPane.setMinWidth(200);

		fontCombo.setCellFactory((ListView<String> stringListView) -> new ShapeCell());
		//fontCombo.setEditable(true);

		Builders.setHBoxPosition(fontCombo, -1, 30, 2.5);
		fontCombo.setStyle("-fx-font-size: 13");
		fontCombo.setCursor(Cursor.HAND);
		fontCombo.getSelectionModel().select("Arial");
		fontCombo.setMaxHeight(25);
		fontCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> Main.mainScreen.selectedProperty().get() == null, Main.mainScreen.selectedProperty()));
		fontCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastFont = newValue;
		});

		Builders.setHBoxPosition(sizeCombo, 95, 30, 2.5);
		sizeCombo.setStyle("-fx-font-size: 13");
		sizeCombo.setCursor(Cursor.HAND);
		sizeCombo.getSelectionModel().select(7);
		sizeCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> Main.mainScreen.selectedProperty().get() == null, Main.mainScreen.selectedProperty()));
		sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastFontSize = newValue;
		});

		Builders.setHBoxPosition(colorPicker, -1, 30, 2.5);
		colorPicker.setStyle("-fx-font-size: 13");
		colorPicker.setCursor(Cursor.HAND);
		colorPicker.setValue(Color.BLACK);
		colorPicker.disableProperty().bind(Bindings.createBooleanBinding(() -> Main.mainScreen.selectedProperty().get() == null, Main.mainScreen.selectedProperty()));
		colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastColor = newValue.toString();
		});

		Builders.setHBoxPosition(boldBtn, 45, 29, 2.5);
		boldBtn.setCursor(Cursor.HAND);
		boldBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		boldBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/Bold.png")+"", 0, 0));
		boldBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> Main.mainScreen.selectedProperty().get() == null, Main.mainScreen.selectedProperty()));
		boldBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastBold = newValue;
		});

		Builders.setHBoxPosition(itBtn, 45, 29, 2.5);
		itBtn.setFont(Font.font("Arial", FontPosture.ITALIC, 20));
		itBtn.setCursor(Cursor.HAND);
		itBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/Italic.png")+"", 0, 0));
		itBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> Main.mainScreen.selectedProperty().get() == null, Main.mainScreen.selectedProperty()));
		itBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(isNew) lastItalic = newValue;
		});

		Builders.setHBoxPosition(txtArea, -1, 30, 0);
		txtArea.setStyle("-fx-font-size: 13");
		txtArea.disableProperty().bind(Bindings.createBooleanBinding(() -> Main.mainScreen.getSelected() == null || !(Main.mainScreen.getSelected() instanceof TextElement), Main.mainScreen.selectedProperty()));

		Builders.setHBoxPosition(deleteBtn, -1, 30, 2.5);
		deleteBtn.setCursor(Cursor.HAND);
		deleteBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> Main.mainScreen.selectedProperty().get() == null, Main.mainScreen.selectedProperty()));

		Builders.setHBoxPosition(newBtn, -1, 30, 2.5);
		newBtn.setCursor(Cursor.HAND);
		newBtn.disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));

		combosBox.getChildren().addAll(fontCombo, sizeCombo);
		colorAndParamsBox.getChildren().addAll(colorPicker, boldBtn, itBtn);
		btnBox.getChildren().addAll(deleteBtn, newBtn);

		VBox.setMargin(combosBox, new Insets(2.5, 2.5, 0, 2.5));
		VBox.setMargin(colorAndParamsBox, new Insets(0, 2.5, 0, 2.5));
		VBox.setMargin(txtArea, new Insets(2.5, 5, 2.5, 5));
		VBox.setMargin(btnBox, new Insets(0, 2.5, 7.5, 2.5));
		optionPane.getChildren().addAll(combosBox, colorAndParamsBox, txtArea, btnBox);


		Main.mainScreen.selectedProperty().addListener((ObservableValue<? extends Element> observable, Element oldElement, Element newElement) -> {
			isNew = false;
			if(oldElement != null){
				if(oldElement instanceof TextElement){
					TextElement current = (TextElement) oldElement;
					current.textProperty().unbind();
					current.fontProperty().unbind();

					if(((TextElement) oldElement).getText().isBlank()){
						oldElement.delete();
					}

					lastsTextSortManager.simulateCall();
					onFileTextSortManager.simulateCall();
				}
			}if(newElement != null){
				if(newElement instanceof TextElement){
					TextElement current = (TextElement) newElement;

					txtArea.setText(current.getText());
					boldBtn.setSelected(Element.getFontWeight(current.getFont()) == FontWeight.BOLD);
					itBtn.setSelected(Element.getFontPosture(current.getFont()) == FontPosture.ITALIC);
					colorPicker.setValue((Color) current.getFill());
					fontCombo.getSelectionModel().select(current.getFont().getFamily());
					sizeCombo.getSelectionModel().select((Integer) ((int) current.getFont().getSize()));

					current.textProperty().bind(txtArea.textProperty());
					current.fontProperty().bind(Bindings.createObjectBinding(() -> { Edition.setUnsave(); return getFont(); }, fontCombo.getSelectionModel().selectedItemProperty(), sizeCombo.getSelectionModel().selectedItemProperty(), itBtn.selectedProperty(), boldBtn.selectedProperty()));
				}
			}
		});
		txtArea.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.TAB){
				if(Main.leftBar.getSelectionModel().getSelectedIndex() == 1) Main.leftBar.getSelectionModel().select(2);
				else Main.leftBar.getSelectionModel().select(1);
			}
		});
		txtArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

			if(newValue.contains("\u0009")){
					txtArea.setText(newValue.replaceAll(Pattern.quote("\u0009"), ""));
			}

			updateHeightAndYLocations(getHorizontalSB(txtArea).isVisible());
			if(!txtAreaScrollBarListenerIsSetup){
				getHorizontalSB(txtArea).visibleProperty().addListener((ObservableValue<? extends Boolean> observableTxt, Boolean oldTxtValue, Boolean newTxtValue) ->  updateHeightAndYLocations(newTxtValue));
				txtAreaScrollBarListenerIsSetup = true;
			}
		});
		colorPicker.setOnAction((ActionEvent e) -> {
			if(Main.mainScreen.getSelected() != null){
				if(Main.mainScreen.getSelected() instanceof TextElement){
					((TextElement) Main.mainScreen.getSelected()).setFill(colorPicker.getValue());
					Edition.setUnsave();
				}

			}
		});
		newBtn.setOnMouseReleased((MouseEvent mouseEvent) -> {

			PageRenderer page = Main.mainScreen.document.pages.get(0);
			if(Main.mainScreen.document.getCurrentPage() != -1) page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

			Main.mainScreen.setSelected(null);

			fontCombo.getSelectionModel().select(lastFont);
			sizeCombo.getSelectionModel().select((Integer) lastFontSize);
			colorPicker.setValue(Color.valueOf(lastColor));
			boldBtn.setSelected(lastBold);
			itBtn.setSelected(lastItalic);

			TextElement current = new TextElement(30, (int) (page.mouseY * Element.GRID_HEIGHT / page.getHeight()), getFont(),
					txtArea.getText(), colorPicker.getValue(), page.getPage(), page);

			page.addElement(current, true);
			Main.mainScreen.setSelected(current);
			isNew = true;

			txtArea.setText("");
			addSavedElement(current.toNoDisplayTextElement(TextTreeItem.LAST_TYPE, true));
			txtArea.requestFocus();
		});
		deleteBtn.setOnMouseReleased((MouseEvent mouseEvent) -> {
			Main.mainScreen.getSelected().delete();
			Main.mainScreen.setSelected(null);
		});

		// TREE VIEW
		Builders.setPosition(favoritesTextToggleOption, 0, 0, 30, 30, true);
		favoritesTextToggleOption.setGraphic(Builders.buildImage(getClass().getResource("/img/Sort/sort.png") +"", 0, 0));
		favoritesTextToggleOption.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue){
				favoritesText.getChildren().add(0, favoritesTextOptionsItem);
				favoritesText.setExpanded(true);
			}else{
				favoritesText.getChildren().remove(0);
			}
		});

		Builders.setPosition(lastsTextToggleOption, 0, 0, 30, 30, true);
		lastsTextToggleOption.setGraphic(Builders.buildImage(getClass().getResource("/img/Sort/sort.png") +"", 0, 0));
		lastsTextToggleOption.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue){
				lastsText.getChildren().add(0, lastsTextOptionsItem);
				lastsText.setExpanded(true);
			}else{
				lastsText.getChildren().remove(0);
			}
		});

		Builders.setPosition(onFileTextToggleOption, 0, 0, 30, 30, true);
		onFileTextToggleOption.setGraphic(Builders.buildImage(getClass().getResource("/img/Sort/sort.png") +"", 0, 0));
		onFileTextToggleOption.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue){
				onFileText.getChildren().add(0, onFileTextOptionsItem);
				onFileText.setExpanded(true);
			}else{
				onFileText.getChildren().remove(0);
			}
		});

		treeView.disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
		treeView.setEditable(true);
		treeView.setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));
		treeView.prefHeightProperty().bind(pane.heightProperty().subtract(treeView.layoutYProperty()));
		treeView.prefWidthProperty().bind(pane.widthProperty());
		treeView.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			// Update element's graphic only if it is the last width value
			new Thread(() -> {
				try{ Thread.sleep(200); }catch(InterruptedException e){ e.printStackTrace(); }
				Platform.runLater(() -> {
					if(treeView.getWidth() == newValue.longValue()) updateListsGraphic();
				});
			}).start();
		});
		treeView.setMaxWidth(400);
		treeView.setShowRoot(false);
		treeView.setRoot(treeViewRoot);

		new TextTreeView(treeView);

		favoritesTextSortManager = new SortManager(new SortEvent(){
			@Override public void call(String sortType, boolean order){

				List<TextTreeItem> toSort = new ArrayList<>();
				for(int i = 0; i < favoritesText.getChildren().size(); i++){
					if(favoritesText.getChildren().get(i) instanceof TextTreeItem){
						toSort.add((TextTreeItem) favoritesText.getChildren().get(i));
					}
				}
				clearSavedFavoritesElements();
				for(TextTreeItem item : autoSortList(toSort, sortType, order)) favoritesText.getChildren().add(item);
			}
		}, null, null);
		favoritesTextSortManager.setup(favoritesTextOptions, TR.tr("Ajout"), TR.tr("Ajout"), TR.tr("Nom"), TR.tr("Utilisation"), "\n", TR.tr("Police"), TR.tr("Taille"), TR.tr("Couleur"));

		lastsTextSortManager = new SortManager(new SortEvent(){
			@Override public void call(String sortType, boolean order){

				List<TextTreeItem> toSort = new ArrayList<>();
				for(int i = 0; i < lastsText.getChildren().size(); i++){
					if(lastsText.getChildren().get(i) instanceof TextTreeItem){
						toSort.add((TextTreeItem) lastsText.getChildren().get(i));
					}
				}
				clearSavedLastsElements();
				for(TextTreeItem item : autoSortList(toSort, sortType, order)) lastsText.getChildren().add(item);

			}
		}, null, null);
		lastsTextSortManager.setup(lastsTextOptions, TR.tr("Ajout"), TR.tr("Ajout"), TR.tr("Nom"), TR.tr("Utilisation"), "\n", TR.tr("Police"), TR.tr("Taille"), TR.tr("Couleur"));

		onFileTextSortManager = new SortManager(new SortEvent(){
			@Override public void call(String sortType, boolean order){

				List<TextTreeItem> toSort = new ArrayList<>();
				for(int i = 0; i < onFileText.getChildren().size(); i++){
					if(onFileText.getChildren().get(i) instanceof TextTreeItem){
						toSort.add((TextTreeItem) onFileText.getChildren().get(i));
					}
				}
				clearSavedOnFileElements();
				for(TextTreeItem item : autoSortList(toSort, sortType, order)) onFileText.getChildren().add(item);

			}
		}, null, null);
		onFileTextSortManager.setup(onFileTextOptions, TR.tr("Position"), TR.tr("Position"), TR.tr("Nom"), "\n", TR.tr("Police"), TR.tr("Taille"), TR.tr("Couleur"));

		favoritesText.setExpanded(true);
		lastsText.setExpanded(true);
		onFileText.setExpanded(true);
		treeViewRoot.getChildren().addAll(favoritesText, lastsText, onFileText);



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
				});
			}catch(InterruptedException e){ e.printStackTrace();}
		}, "selector").start();

	}

	private Font getFont(){

		return Element.getFont(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected(), sizeCombo.getSelectionModel().getSelectedItem());
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
				setStyle("-fx-font: 14 \"" + item + "\"");
			}
		}
	}
	public void updateListsGraphic(){

		for(TreeItem<String> item : favoritesText.getChildren()){
			if(item instanceof TextTreeItem) ((TextTreeItem) item).updateGraphic();
		}
		for(TreeItem<String> item : lastsText.getChildren()){
			if(item instanceof TextTreeItem) ((TextTreeItem) item).updateGraphic();
		}
		for(TreeItem<String> item : onFileText.getChildren()){
			if(item instanceof TextTreeItem) ((TextTreeItem) item).updateGraphic();
		}

	}
	public void addSavedElement(TextTreeItem element){
		if(element.getType() == TextTreeItem.FAVORITE_TYPE){
			if(!favoritesText.getChildren().contains(element)){
				favoritesText.getChildren().add(element);
				favoritesTextSortManager.simulateCall();
			}
		}else if(element.getType() == TextTreeItem.LAST_TYPE){
			if(!lastsText.getChildren().contains(element)){

				if(lastsText.getChildren().size() > 49){
					List<TextTreeItem> toSort = new ArrayList<>();
					for(int i = 0; i < lastsText.getChildren().size(); i++){
						if(lastsText.getChildren().get(i) instanceof TextTreeItem){
							toSort.add((TextTreeItem) lastsText.getChildren().get(i));
						}
					}
					List<TextTreeItem> sorted = Sorter.sortElementsByUtils(toSort, true);
					removeSavedElement(sorted.get(sorted.size()-1));
				}
				lastsText.getChildren().add(element);
				lastsTextSortManager.simulateCall();
			}
		}
	}
	public void removeSavedElement(TextTreeItem element){
		if(element.getType() == TextTreeItem.FAVORITE_TYPE){
			favoritesText.getChildren().remove(element);
		}else{
			lastsText.getChildren().remove(element);
		}
	}
	public void clearSavedFavoritesElements(){
		List<TreeItem<String>> items = favoritesText.getChildren();
		for(int i = items.size()-1; i >= 0; i--){
			if(items.get(i) instanceof TextTreeItem){
				items.remove(i);
			}
		}
	}
	public void clearSavedLastsElements(){
		List<TreeItem<String>> items = lastsText.getChildren();
		for(int i = items.size()-1; i >= 0; i--){
			if(items.get(i) instanceof TextTreeItem){
				items.remove(i);
			}
		}
	}
	public void clearSavedOnFileElements(){
		List<TreeItem<String>> items = onFileText.getChildren();
		for(int i = items.size()-1; i >= 0; i--){
			if(items.get(i) instanceof TextTreeItem){
				items.remove(i);
			}
		}
	}

	public void updateOnFileElementsList(){
		clearSavedOnFileElements();

		if(Main.mainScreen.getStatus() == MainScreen.Status.OPEN){
			for(PageRenderer page : Main.mainScreen.document.pages){
				for(int i = 0; i < page.getElements().size(); i++){
					if(page.getElements().get(i) instanceof TextElement){
						TextElement element = (TextElement) page.getElements().get(i);
						onFileText.getChildren().add(element.toNoDisplayTextElement(TextTreeItem.ONFILE_TYPE, true));
					}
				}
			}
		}
		onFileTextSortManager.simulateCall();
	}
	public void addOnFileElement(TextElement element){

		onFileText.getChildren().add(element.toNoDisplayTextElement(TextTreeItem.ONFILE_TYPE, true));
		onFileTextSortManager.simulateCall();

	}
	public void removeOnFileElement(TextElement element){

		List<TreeItem<String>> items = onFileText.getChildren();
		for(TreeItem<String> item : items){
			if(item instanceof TextTreeItem){
				if(((TextTreeItem) item).getCore().equals(element)){
					items.remove(item);
					break;
				}
			}
		}
		items = lastsText.getChildren();
		for(TreeItem<String> item : items){
			if(item instanceof TextTreeItem){
				if(((TextTreeItem) item).getCore() != null){
					if(((TextTreeItem) item).getCore().equals(element)){
						items.remove(item);
						break;
					}
				}
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
	private List<TextTreeItem> autoSortList(List<TextTreeItem> toSort, String sortType, boolean order){

		if(sortType.equals(TR.tr("Ajout"))){
			return Sorter.sortElementsByDate(toSort, order);
		}else if(sortType.equals(TR.tr("Nom"))){
			return Sorter.sortElementsByName(toSort, order);
		}else if(sortType.equals(TR.tr("Utilisation"))){
			return Sorter.sortElementsByUtils(toSort, order);
		}else if(sortType.equals(TR.tr("Police"))){
			return Sorter.sortElementsByPolice(toSort, order);
		}else if(sortType.equals(TR.tr("Taille"))){
			return Sorter.sortElementsBySize(toSort, order);
		}else if(sortType.equals(TR.tr("Couleur"))){
			return Sorter.sortElementsByColor(toSort, order);
		}else if(sortType.equals(TR.tr("Position"))){
			return Sorter.sortElementsByCorePosition(toSort, order);
		}
		return toSort;
	}
}
