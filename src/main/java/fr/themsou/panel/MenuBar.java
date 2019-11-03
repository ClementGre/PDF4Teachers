package fr.themsou.panel;

import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.List;

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar{

	Menu fichier = new Menu("Fichier");
	MenuItem fichier1Open = new MenuItem("Ouvrir un fichier    ");
	MenuItem fichier2OpenDir = new MenuItem("Ouvrir un dossier    ");
	MenuItem fichier3Close = new MenuItem("Fermer le fichier     ");
	MenuItem fichier4Clear = new MenuItem("Vider la liste     ");
	MenuItem fichier5Save = new MenuItem("Sauvegarder l'édition    ");
	MenuItem fichier6Delete = new MenuItem("Supprimer l'édition     ");
	MenuItem fichier7SameName = new Menu("Éditions du même nom     ");
	MenuItem fichier8Export = new MenuItem("Exporter     ");
	MenuItem fichier9ExportAll = new MenuItem("Tout exporter     ");

	Menu preferences = new Menu("Préférences");
	MenuItem preferences1Zoom = new MenuItem("Zoom par défaut     ");
	MenuItem preferences2Pages = new MenuItem("Pages maximum     ");
	RadioMenuItem preferences3Save = new RadioMenuItem("Sauvegarde auto     ");
	MenuItem preferences4Regular = new MenuItem("Sauvegarde régulière     ");

	Menu apropos = new Menu("À propos");
	Menu aide = new Menu("Aide");
	MenuItem aide1Doc = new MenuItem("Charger le document d'aide     ");

	public MenuBar(){
		setup();
	}

	public void setup(){


		setStyle("-fx-background-color: #2B2B2B;");

		fichier1Open.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/ouvrir.png")+"", 0, 0));
		fichier1Open.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));

		fichier2OpenDir.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/directory.png")+"", 0, 0));
		fichier2OpenDir.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+O"));

		fichier3Close.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/fermer.png")+"", 0, 0));
		fichier3Close.setAccelerator(KeyCombination.keyCombination("Ctrl+W"));
		fichier3Close.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier4Clear.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/vider.png")+"", 0, 0));
		fichier4Clear.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+W"));
		fichier4Clear.disableProperty().bind(Bindings.size(Main.lbFilesTab.files.getItems()).isEqualTo(0));

		fichier5Save.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));
		fichier5Save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		fichier5Save.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier6Delete.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/supprimer.png")+"", 0, 0));
		fichier6Delete.setAccelerator(KeyCombination.keyCombination("Ctrl+Del"));
		fichier6Delete.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier7SameName.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/memeNom.png")+"", 0, 0));
		fichier7SameName.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier8Export.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/exporter.png")+"", 0, 0));
		fichier8Export.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
		fichier8Export.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier9ExportAll.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/exporter.png")+"", 0, 0));
		fichier9ExportAll.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+E"));
		fichier9ExportAll.disableProperty().bind(Bindings.size(Main.lbFilesTab.files.getItems()).isEqualTo(0));



		Main.lbFilesTab.files.itemsProperty().addListener(new ChangeListener<ObservableList<File>>() {
			@Override
			public void changed(ObservableValue<? extends ObservableList<File>> observable, ObservableList<File> oldValue, ObservableList<File> newValue) {
				System.out.println("change");
			}
		});

		fichier.getItems().addAll(fichier1Open, fichier2OpenDir, fichier3Close, fichier4Clear, new SeparatorMenuItem(), fichier5Save, fichier6Delete, fichier7SameName, new SeparatorMenuItem(), fichier8Export, fichier9ExportAll);

		/*Menu menu2 = new Menu("Édition");
		MenuItem menu2arg1 = new MenuItem(" Annuler     ");
		menu2arg1.setGraphic(Builders.buildImage(Main.devices.getClass().getResource("/img/MenuBar/annuler.png")+"", 0, 0));
		menu2arg1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK, true));
		menu2arg2.setGraphic(Builders.buildImage(Main.devices.getClass().getResource("/img/MenuBar/retablir.png")+"", 0, 0));
		MenuItem menu2arg3 = new MenuItem(" Couper     ");
		menu2arg3.setGraphic(Builders.buildImage(Main.devices.getClass().getResource("/img/MenuBar/couper.png")+"", 0, 0));
		MenuItem menu2arg4 = new MenuItem(" Copier     ");
		menu2arg4.setGraphic(Builders.buildImage(Main.devices.getClass().getResource("/img/MenuBar/copier.png")+"", 0, 0));
		MenuItem menu2arg5 = new MenuItem(" Coller     ");
		menu2arg5.setGraphic(Builders.buildImage(Main.devices.getClass().getResource("/img/MenuBar/coller.png")+"", 0, 0));*/



		preferences1Zoom.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/zoom.png")+"", 0, 0));
		preferences2Pages.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/maxPages.png")+"", 0, 0));
		preferences3Save.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));
		preferences4Regular.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));
		preferences3Save.selectedProperty().set(Main.settings.isAutoSave());
		Main.settings.autoSavingProperty().bind(preferences3Save.selectedProperty());

		preferences.getItems().addAll(preferences1Zoom, preferences2Pages, preferences3Save, preferences4Regular);


		aide.getItems().add(aide1Doc);


		fichier1Open.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent event){

				final FileChooser chooser = new FileChooser();
				chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
				chooser.setTitle("Selexionnez un ou plusieurs fichier");
				chooser.setInitialDirectory(new File(System.getProperty("user.home")));

				List<File> listFiles = chooser.showOpenMultipleDialog(Main.window);
				if(listFiles != null){
					File[] files = new File[listFiles.size()];
					files = listFiles.toArray(files);
					Main.lbFilesTab.openFiles(files);
				}

			}
		});
		fichier2OpenDir.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent event){

				final DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Selexionnez un ou plusieurs dossier");
				chooser.setInitialDirectory(new File(System.getProperty("user.home")));

				File file = chooser.showDialog(Main.window);
				if(file != null) {
					Main.lbFilesTab.openFile(file);
				}

			}
		});
		fichier3Close.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent event){
				if(Main.mainScreen.hasDocument(true)){
					Main.mainScreen.closeFile(true);
				}
			}
		});
		fichier4Clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				Main.lbFilesTab.clearFiles(true);
			}
		});
		fichier5Save.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				if(Main.mainScreen.hasDocument(true)){
					Main.mainScreen.document.edition.save();
				}
			}
		});
		fichier6Delete.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				if(Main.mainScreen.hasDocument(true)){
					Main.mainScreen.document.edition.clearEdit(true);
					Main.mainScreen.setSelected(null);
				}
			}
		});
		fichier8Export.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				new ExportWindow().export(Collections.singletonList(Main.mainScreen.document.getFile()));
			}
		});
		fichier9ExportAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				new ExportWindow().export(Main.lbFilesTab.files.getItems());
			}
		});

		preferences1Zoom.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {

				List<Integer> choices = new ArrayList<>(Arrays.asList(50, 70, 80, 90, 100, 110, 120, 140, 160, 180, 200, 230, 250, 280, 300));
				ChoiceDialog<Integer> dialog = new ChoiceDialog<>(Main.settings.getDefaultZoom(), choices);
				new JMetro(dialog.getDialogPane(), Style.LIGHT);
				Builders.secureAlert(dialog);
				dialog.setTitle("Zoom par défaut");
				dialog.setHeaderText("Vous allez définire le zoom par défaut quand vous ouvrirez un document.");
				dialog.setContentText("Choisissez un pourcentage :");

				Optional<Integer> newZoom = dialog.showAndWait();
				if(!newZoom.isEmpty()){
					Main.settings.setDefaultZoom(newZoom.get());
				}

			}
		});
		preferences2Pages.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {

				List<Integer> choices = new ArrayList<>(Arrays.asList(10, 20, 30, 40, 50, 60, 80, 100, 120, 160, 200, 99999));
				ChoiceDialog<Integer> dialog = new ChoiceDialog<>(Main.settings.getMaxPages(), choices);
				new JMetro(dialog.getDialogPane(), Style.LIGHT);
				Builders.secureAlert(dialog);
				dialog.setTitle("Nombre de pages maximum");
				dialog.setHeaderText("Vous allez définire le nombre de pages maximum qui pouront être affichés.");
				dialog.setContentText("Choisissez un nombre :");

				Optional<Integer> newMax = dialog.showAndWait();
				if(!newMax.isEmpty()){
					Main.settings.setMaxPages(newMax.get());
				}
			}
		});
		preferences4Regular.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {

				Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);

				HBox pane = new HBox();
				ComboBox<Integer> combo = new ComboBox<>(FXCollections.observableArrayList(1, 5, 10, 15, 20, 30, 45, 60));
				combo.getSelectionModel().select(Main.settings.getRegularSaving() == -1 ? (Integer) 5 : (Integer) Main.settings.getRegularSaving());
				combo.setStyle("-fx-padding-left: 20px;");
				CheckBox activated = new CheckBox("Activer");
				activated.setSelected(Main.settings.getRegularSaving() != -1);
				pane.getChildren().add(0, activated);
				pane.getChildren().add(1, combo);
				HBox.setMargin(activated, new Insets(5, 0, 0, 10));
				HBox.setMargin(combo, new Insets(0, 0, 0, 30));

				combo.disableProperty().bind(activated.selectedProperty().not());

				new JMetro(dialog.getDialogPane(), Style.LIGHT);
				Builders.secureAlert(dialog);

				dialog.setTitle("Sauvegarde régulière");
				dialog.setHeaderText("Vous allez définire le nombre de minutes entre deux sauvegardes automatiques.");

				dialog.getDialogPane().setContent(pane);

				ButtonType cancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
				ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
				dialog.getButtonTypes().setAll(cancel, ok);

				Optional<ButtonType> option = dialog.showAndWait();
				if(option.get() == ok){
					int time = -1;
					if(activated.isSelected()) time = combo.getSelectionModel().getSelectedItem();
					Main.settings.setRegularSaving(time);
				}

			}
		});

		//setStyle("-fx-font-size: 13");
		getMenus().addAll(fichier, preferences, apropos, aide);

		for(Menu menu : getMenus()){
			Builders.setMenuSize(menu);
		}


		
	}
}
