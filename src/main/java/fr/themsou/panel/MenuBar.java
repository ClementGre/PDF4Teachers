package fr.themsou.panel;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
	Menu preferences1Zoom = new Menu("Zoom par défaut     ");
	Menu preferences2Pages = new Menu("Pages maximum     ");
	RadioMenuItem preferences3Save = new RadioMenuItem("Sauvegarde auto     ");

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

		fichier4Clear.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/vider.png")+"", 0, 0));
		fichier4Clear.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+W"));

		fichier5Save.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/sauvegarder.png")+"", 0, 0));
		fichier5Save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));

		fichier6Delete.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/supprimer.png")+"", 0, 0));

		fichier7SameName.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/memeNom.png")+"", 0, 0));
		fichier7SameName.setAccelerator(KeyCombination.keyCombination("Ctrl+Del"));

		fichier8Export.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/exporter.png")+"", 0, 0));
		fichier8Export.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));

		fichier9ExportAll.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/exporter.png")+"", 0, 0));
		fichier9ExportAll.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+E"));

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
		preferences3Save.selectedProperty().set(Main.settings.isAutoSave());
		Main.settings.autoSavingProperty().bind(preferences3Save.selectedProperty());

		preferences.getItems().addAll(preferences1Zoom, preferences2Pages, preferences3Save);


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
					Main.mainScreen.document.save();
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

			}
		});
		fichier9ExportAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {

			}
		});

		//setStyle("-fx-font-size: 13");
		getMenus().addAll(fichier, preferences, apropos, aide);

		for(Menu menu : getMenus()){
			Builders.setMenuSize(menu);
		}


		
	}
}
