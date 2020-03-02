package fr.themsou.panel.LeftBar;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoDisplayTextElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.Set;

@SuppressWarnings("serial")
public class LBTextTab extends Tab {

	public Pane pane = new Pane();

	private ComboBox<String> fontCombo; String[] fontNames;
	private ComboBox<Integer> sizeCombo = new ComboBox<>(FXCollections.observableArrayList(6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30, 34, 38, 42, 46, 50));
	private ColorPicker colorPicker = new ColorPicker();
	private ToggleButton boldBtn = new ToggleButton("");
	private ToggleButton itBtn = new ToggleButton("");
	private TextArea txtArea = new TextArea();
	private Button deleteBtn = new Button("Supprimer");
	private Button newBtn = new Button("Nouveau");

	private TreeView treeView = new TreeView<>();
	private TreeItem<String> treeViewRoot = new TreeItem<>();
	private TreeItem<String> onFileText = new TreeItem<>("Éléments sur ce document");

	private boolean selectedIsNew = false;
	private boolean txtAreaScrollBarListenerIsSetup = false;

	public LBTextTab(){

		setClosable(false);
		setContent(pane);

		setGraphic(Builders.buildImage(getClass().getResource("/img/Text.png")+"", 0, 25));
		Main.leftBar.getTabs().add(1, this);

		setup();
	}

	private static File[] getResourceFolderFiles(String folder){
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(folder);
		String path = url.getPath();

		return new File(path + "/").listFiles();
	}

	public void setup(){


		/*GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontNames = ge.getAvailableFontFamilyNames();
		fontCombo = new ComboBox<>(FXCollections.observableArrayList(fontNames));*/
		fontCombo = new ComboBox<>(FXCollections.observableArrayList("Arial", "Lato", "Lato Light", "Times New Roman"));

		fontCombo.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> stringListView) {

				return new ShapeCell();
			}
		});
		//fontCombo.setEditable(true);

		Builders.setPosition(fontCombo, 5, 5, 160, 30, true);
		fontCombo.setStyle("-fx-font-size: 13");
		fontCombo.setCursor(Cursor.HAND);
		fontCombo.getSelectionModel().select("Arial");
		fontCombo.setMaxHeight(25);
		pane.getChildren().add(fontCombo);
		fontCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.selectedProperty().get() == null;}, Main.mainScreen.selectedProperty()));

		Builders.setPosition(sizeCombo, 170, 5, 95, 30, true);
		sizeCombo.setStyle("-fx-font-size: 13");
		sizeCombo.setCursor(Cursor.HAND);
		sizeCombo.getSelectionModel().select(7);
		pane.getChildren().add(sizeCombo);
		sizeCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.selectedProperty().get() == null;}, Main.mainScreen.selectedProperty()));

		Builders.setPosition(colorPicker, 5, 40, 160, 30, false);
		colorPicker.setStyle("-fx-font-size: 13");
		colorPicker.setCursor(Cursor.HAND);
		colorPicker.setValue(Color.BLACK);
		pane.getChildren().add(colorPicker);
		colorPicker.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.selectedProperty().get() == null;}, Main.mainScreen.selectedProperty()));

		Builders.setPosition(boldBtn, 170, 40, 45, 29, true);
		boldBtn.setCursor(Cursor.HAND);
		boldBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		boldBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/Bold.png")+"", 0, 0));
		pane.getChildren().add(boldBtn);
		boldBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.selectedProperty().get() == null;}, Main.mainScreen.selectedProperty()));

		Builders.setPosition(itBtn, 220, 40, 45, 29, true);
		itBtn.setFont(Font.font("Arial", FontPosture.ITALIC, 20));
		itBtn.setCursor(Cursor.HAND);
		itBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/Italic.png")+"", 0, 0));
		pane.getChildren().add(itBtn);
		itBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.selectedProperty().get() == null;}, Main.mainScreen.selectedProperty()));

		Builders.setPosition(txtArea, 5, 75, 260, 30, true);
		txtArea.setStyle("-fx-font-size: 13");
		pane.getChildren().add(txtArea);
		txtArea.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.getSelected() == null || !(Main.mainScreen.getSelected() instanceof TextElement);}, Main.mainScreen.selectedProperty()));

		txtArea.textProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				updateOnFileElementsList();
				updateHeightAndYLocations(getHorizontalSB(txtArea).isVisible());
				if(!txtAreaScrollBarListenerIsSetup){
					getHorizontalSB(txtArea).visibleProperty().addListener(new ChangeListener<Boolean>() {
						@Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
							updateHeightAndYLocations(newValue);
						}
					});
					txtAreaScrollBarListenerIsSetup = true;
				}
			}
		});



		Builders.setPosition(deleteBtn, 5, 110, 127.5, 30, false);
		deleteBtn.setCursor(Cursor.HAND);
		pane.getChildren().add(deleteBtn);
		deleteBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.selectedProperty().get() == null;}, Main.mainScreen.selectedProperty()));

		Builders.setPosition(newBtn, 137.5, 110, 127.5, 30, false);
		newBtn.setCursor(Cursor.HAND);
		pane.getChildren().add(newBtn);
		newBtn.disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(-1));


		Main.mainScreen.selectedProperty().addListener(new ChangeListener<Element>() {
			@Override public void changed(ObservableValue<? extends Element> observable, Element oldValue, Element newValue) {

				if(oldValue != null && selectedIsNew){
					if(oldValue instanceof TextElement){
						if(!((TextElement) oldValue).getText().isEmpty()){
							addSavedElement(((TextElement) oldValue).toNoDisplayTextElement(NoDisplayTextElement.LAST_TYPE, false));
						}
					}
				}
				selectedIsNew = false;

			}
		});

		Main.mainScreen.selectedProperty().addListener(new ChangeListener<Element>() {
			public void changed(ObservableValue<? extends Element> observableValue, Element oldElement, Element newElement){

				if(oldElement != null){
					if(oldElement instanceof TextElement){
						TextElement current = (TextElement) oldElement;

						current.textProperty().unbind();
						current.realFontProperty().unbind();
					}
				}


				if(newElement != null){
					if(newElement instanceof TextElement){
						TextElement current = (TextElement) newElement;

						txtArea.setText(current.getText());
						boldBtn.setSelected(TextElement.getFontWeight(current.getRealFont()) == FontWeight.BOLD);
						itBtn.setSelected(TextElement.getFontPosture(current.getRealFont()) == FontPosture.ITALIC);
						colorPicker.setValue((Color) current.getFill());
						fontCombo.getSelectionModel().select(current.getRealFont().getFamily());
						sizeCombo.getSelectionModel().select((Integer) ((int) current.getRealFont().getSize()));

						current.textProperty().bind(txtArea.textProperty());
						current.realFontProperty().bind(Bindings.createObjectBinding(() -> { Edition.setUnsave(); return getFont(); }, fontCombo.getSelectionModel().selectedItemProperty(), sizeCombo.getSelectionModel().selectedItemProperty(), itBtn.selectedProperty(), boldBtn.selectedProperty()));

					}
				}
			}
		});

		colorPicker.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				if(Main.mainScreen.getSelected() != null){
					if(Main.mainScreen.getSelected() instanceof TextElement){
						((TextElement) Main.mainScreen.getSelected()).setFill(colorPicker.getValue());
						Edition.setUnsave();
					}

				}
			}
		});

		newBtn.setOnMouseReleased(new EventHandler<MouseEvent>(){
			@Override public void handle(MouseEvent mouseEvent) {

				if(Main.mainScreen.getSelected() != null){
					if(Main.mainScreen.getSelected() instanceof TextElement){
						if(((TextElement) Main.mainScreen.getSelected()).getText().isEmpty()){
							Main.mainScreen.getSelected().delete();
						}
					}
				}

				PageRenderer page = Main.mainScreen.document.pages.get(0);
				if(Main.mainScreen.document.getCurrentPage() != -1)
					page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());


				TextElement current = new TextElement(30, (int) (page.mouseY * 800 / page.getHeight()), getFont(),
						txtArea.getText(), colorPicker.getValue(), page.getPage(), page);


				page.addElement(current);
				Main.mainScreen.selectedProperty().setValue(current);

				txtArea.setText("");
				selectedIsNew = true;
				txtArea.requestFocus();
			}
		});

		deleteBtn.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {

				Main.mainScreen.getSelected().delete();
				Main.mainScreen.setSelected(null);
			}
		});

		// TREE VIEW

		treeView.disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(-1));
		treeView.setEditable(true);
		treeView.setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));
		treeView.setLayoutY(150);
		treeView.setPrefWidth(270);
		treeView.prefHeightProperty().bind(pane.heightProperty().subtract(150));
		treeView.setShowRoot(false);
		treeView.setRoot(treeViewRoot);
		new LBTextTreeView(treeView);

		treeViewRoot.getChildren().addAll(Main.userData.favoritesText, Main.userData.lastsText, onFileText);

		pane.getChildren().add(treeView);

	}

	public void updateHeightAndYLocations(boolean sbIsVisible){

		int lineNumber = txtArea.getParagraphs().size();
		int height = lineNumber >= 3 ? 70 : lineNumber*20+10;

		if(sbIsVisible) height += 15;

		if(txtArea.getHeight() != height){
			txtArea.minHeightProperty().bind(new SimpleDoubleProperty(height));
			deleteBtn.setLayoutY(80 + height);
			newBtn.setLayoutY(80 + height);
			treeView.setLayoutY(120 + height);
		}

	}

	public void selectItem(){
		new Thread(new Runnable() {
			@Override public void run() {
				try{
					Thread.sleep(50);
					Platform.runLater(new Runnable() {
						@Override public void run() {
							String text = txtArea.getText();
							txtArea.setText(text);
							txtArea.positionCaret(txtArea.getText().length());
						}
					});
				}catch(InterruptedException e){ e.printStackTrace();}
			}
		}, "selector").start();

	}

	private Font getFont(){

		return TextElement.getFont(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected(), sizeCombo.getSelectionModel().getSelectedItem());
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
	public void addSavedElement(NoDisplayTextElement element){
		if(element.getType() == NoDisplayTextElement.FAVORITE_TYPE){
			if(!Main.userData.favoritesText.getChildren().contains(element)){
				Main.userData.favoritesText.getChildren().add(element);
			}
		}else if(element.getType() == NoDisplayTextElement.LAST_TYPE){
			if(!Main.userData.lastsText.getChildren().contains(element)){
				Main.userData.lastsText.getChildren().add(0, element);

				if(Main.userData.lastsText.getChildren().size() > 30){
					Main.userData.lastsText.getChildren().remove(Main.userData.lastsText.getChildren().size()-1);
				}
			}
		}
	}
	public void removeSavedElement(NoDisplayTextElement element){
		if(element.getType() == NoDisplayTextElement.FAVORITE_TYPE){
			Main.userData.favoritesText.getChildren().remove(element);
		}else{
			Main.userData.lastsText.getChildren().remove(element);
		}
	}
	public void clearSavedFavoritesElements(){
		Main.userData.favoritesText.getChildren().clear();
	}
	public void clearSavedLastsElements(){
		Main.userData.lastsText.getChildren().clear();
	}

	public void ascendElement(NoDisplayTextElement element) {
		int pos = (element.getType() == NoDisplayTextElement.FAVORITE_TYPE) ? Main.userData.favoritesText.getChildren().indexOf(element) : Main.userData.lastsText.getChildren().indexOf(element);
		removeSavedElement(element);

		if(element.getType() == NoDisplayTextElement.FAVORITE_TYPE){
			Main.userData.favoritesText.getChildren().add(pos - 1, element);
		}else if(element.getType() == NoDisplayTextElement.LAST_TYPE){
			Main.userData.lastsText.getChildren().add(pos - 1, element);
		}
	}

	public void descendElement(NoDisplayTextElement element) {
		int pos = (element.getType() == NoDisplayTextElement.FAVORITE_TYPE) ? Main.userData.favoritesText.getChildren().indexOf(element) : Main.userData.lastsText.getChildren().indexOf(element);
		removeSavedElement(element);

		if(element.getType() == NoDisplayTextElement.FAVORITE_TYPE){
			Main.userData.favoritesText.getChildren().add(pos + 1, element);
		}else if(element.getType() == NoDisplayTextElement.LAST_TYPE){
			Main.userData.lastsText.getChildren().add(pos + 1, element);
		}
	}

	public void updateOnFileElementsList(){

		onFileText.getChildren().clear();

		if(Main.mainScreen.getStatus() == -1){
			for(PageRenderer page : Main.mainScreen.document.pages){
				for(int i = 0; i < page.getElements().size(); i++){
					if(page.getElements().get(i) instanceof TextElement){
						TextElement element = (TextElement) page.getElements().get(i);
						onFileText.getChildren().add(element.toNoDisplayTextElement(NoDisplayTextElement.ONFILE_TYPE, true));
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
}
