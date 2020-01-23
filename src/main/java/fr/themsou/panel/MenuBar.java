package fr.themsou.panel;

import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.AboutWindow;
import fr.themsou.main.Main;
import fr.themsou.panel.LeftBar.LBFilesListView;
import fr.themsou.utils.Builders;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar{

	Menu fichier = new Menu("Fichier");
	MenuItem fichier1Open = new MenuItem("Ouvrir un·des fichiers    ");
	MenuItem fichier2OpenDir = new MenuItem("Ouvrir un dossier    ");
	MenuItem fichier3Clear = new MenuItem("Vider la liste     ");
	MenuItem fichier4Save = new MenuItem("Sauvegarder l'édition    ");
	MenuItem fichier5Delete = new MenuItem("Supprimer l'édition     ");
	MenuItem fichier6Close = new MenuItem("Fermer le fichier     ");
	MenuItem fichier7SameName = new Menu("Éditions du même nom     ");
	MenuItem fichier8Export = new MenuItem("Exporter (Regénérer le PDF)     ");
	MenuItem fichier9ExportAll = new MenuItem("Tout exporter     ");

	Menu preferences = new Menu("Préférences");
	MenuItem preferences1Zoom = new MenuItem("Zoom par défaut     ");
	RadioMenuItem preferences2Save = new RadioMenuItem("Sauvegarde auto     ");
	MenuItem preferences3Regular = new MenuItem("Sauvegarde régulière     ");
	RadioMenuItem preferences4Restore = new RadioMenuItem("Toujours restaurer la session précédente     ");

	Menu apropos = new Menu();
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

		fichier3Clear.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/vider.png")+"", 0, 0));
		fichier3Clear.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+W"));
		fichier3Clear.disableProperty().bind(Bindings.size(Main.lbFilesTab.files.getItems()).isEqualTo(0));

		fichier4Save.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));
		fichier4Save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		fichier4Save.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier5Delete.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/supprimer.png")+"", 0, 0));
		fichier5Delete.setAccelerator(KeyCombination.keyCombination("Ctrl+Del"));
		fichier5Delete.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier6Close.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/fermer.png")+"", 0, 0));
		fichier6Close.setAccelerator(KeyCombination.keyCombination("Ctrl+W"));
		fichier6Close.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));


		fichier7SameName.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/memeNom.png")+"", 0, 0));
		fichier7SameName.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier8Export.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/exporter.png")+"", 0, 0));
		fichier8Export.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
		fichier8Export.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.mainScreen.statusProperty().get() != -1;}, Main.mainScreen.statusProperty()));

		fichier9ExportAll.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/exporter.png")+"", 0, 0));
		fichier9ExportAll.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+E"));
		fichier9ExportAll.disableProperty().bind(Bindings.size(Main.lbFilesTab.files.getItems()).isEqualTo(0));

		fichier.getItems().addAll(fichier1Open, fichier2OpenDir, fichier3Clear, new SeparatorMenuItem(), fichier4Save, fichier5Delete, fichier6Close, fichier7SameName, new SeparatorMenuItem(), fichier8Export, fichier9ExportAll);

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
		preferences2Save.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));
		preferences3Regular.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));
		//preferences4Restore.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));

		preferences2Save.selectedProperty().set(Main.settings.isAutoSave());
		preferences4Restore.selectedProperty().set(Main.settings.isRestoreLastSession());

		Main.settings.autoSavingProperty().bind(preferences2Save.selectedProperty());
		Main.settings.restoreLastSessionProperty().bind(preferences4Restore.selectedProperty());

		preferences.getItems().addAll(preferences1Zoom, preferences2Save, preferences3Regular, preferences4Restore);

		aide.getItems().add(aide1Doc);


		fichier1Open.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent event){

				final FileChooser chooser = new FileChooser();
				chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
				chooser.setTitle("Selectionner un ou plusieurs fichier");
				chooser.setInitialDirectory((LBFilesListView.lastDirChoosed.exists() ? LBFilesListView.lastDirChoosed : new File(System.getProperty("user.home"))));

				List<File> listFiles = chooser.showOpenMultipleDialog(Main.window);
				if(listFiles != null){
					File[] files = new File[listFiles.size()];
					files = listFiles.toArray(files);
					Main.lbFilesTab.openFiles(files);
					if(files.length == 1){
						Main.mainScreen.openFile(files[0]);
					}
					LBFilesListView.lastDirChoosed = files[0].getParentFile();

				}

			}
		});
		fichier2OpenDir.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent event){

				final DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Selectionner un dossier");
				chooser.setInitialDirectory((LBFilesListView.lastDirChoosed.exists() ? LBFilesListView.lastDirChoosed : new File(System.getProperty("user.home"))));

				File file = chooser.showDialog(Main.window);
				if(file != null) {
					Main.lbFilesTab.openFiles(new File[]{file});
					LBFilesListView.lastDirChoosed = file.getParentFile();
				}

			}
		});
		fichier3Clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				Main.lbFilesTab.clearFiles(true);
			}
		});
		fichier4Save.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				if(Main.mainScreen.hasDocument(true)){
					Main.mainScreen.document.edition.save();
				}
			}
		});
		fichier5Delete.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				if(Main.mainScreen.hasDocument(true)){
					Main.mainScreen.document.edition.clearEdit(true);
					Main.mainScreen.setSelected(null);
				}
			}
		});
		fichier6Close.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent event){
				if(Main.mainScreen.hasDocument(true)){
					Main.mainScreen.closeFile(true);
				}
			}
		});
		fichier8Export.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {

				Main.mainScreen.document.save();
				new ExportWindow(Collections.singletonList(Main.mainScreen.document.getFile()));
			}
		});
		fichier9ExportAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {

				if(Main.mainScreen.hasDocument(false))
					Main.mainScreen.document.save();

				new ExportWindow(Main.lbFilesTab.files.getItems());
			}
		});

		preferences1Zoom.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {

				List<Integer> choices = new ArrayList<>(Arrays.asList(50, 70, 80, 90, 100, 110, 120, 140, 160, 180, 200, 230, 250, 280, 300));
				ChoiceDialog<Integer> dialog = new ChoiceDialog<>(Main.settings.getDefaultZoom(), choices);
				new JMetro(dialog.getDialogPane(), Style.LIGHT);
				Builders.secureAlert(dialog);
				dialog.setTitle("Zoom par défaut");
				dialog.setHeaderText("Zoom par défaut lors de l'ouverture d'un document");
				dialog.setContentText("Choisir un pourcentage :");

				Optional<Integer> newZoom = dialog.showAndWait();
				if(!newZoom.isEmpty()){
					Main.settings.setDefaultZoom(newZoom.get());
				}

			}
		});
		preferences3Regular.setOnAction(new EventHandler<ActionEvent>() {
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
				dialog.setHeaderText("Définir le nombre de minutes entre deux sauvegardes automatiques.");

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

		aide1Doc.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {

				try{

					InputStream docRes = getClass().getResourceAsStream("/Documentation - PDFTeacher.pdf");

					File doc = new File(Main.dataFolder + "" +
							"Documentation - PDFTeacher.pdf");
					if(!doc.exists()) Files.copy(docRes, doc.getAbsoluteFile().toPath());

					Main.mainScreen.openFile(doc);

				}catch(IOException ex){
					ex.printStackTrace();
				}

			}
		});

		Label name = new Label("À propos");
		name.setAlignment(Pos.CENTER_LEFT);
		name.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				new AboutWindow();
			}
		});
		apropos.setGraphic(name);


		getMenus().addAll(fichier, preferences, apropos, aide);

		for(Menu menu : getMenus()){
			Builders.setMenuSize(menu);
		}


		
	}
}
