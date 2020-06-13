package fr.themsou.panel;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.convert.ConvertDocument;
import fr.themsou.document.render.convert.ConvertWindow;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.UserData;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.utils.components.NodeMenuItem;
import fr.themsou.utils.components.NodeRadioMenuItem;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.AboutWindow;
import fr.themsou.main.Main;
import fr.themsou.utils.*;
import fr.themsou.windows.LanguageWindow;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar{

	////////// FICHIER //////////

	Menu fichier = new Menu(TR.tr("Fichier"));
	public NodeMenuItem fichier1Open = createMenuItem(TR.tr("Ouvrir un·des fichiers"), "ouvrir", new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ajoute un ou plusieurs fichiers dans le panneau des fichiers."));

	public NodeMenuItem fichier2OpenDir = createMenuItem(TR.tr("Ouvrir un dossier"), "directory", new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ajoute tous les fichiers PDF d'un dossier dans le panneau des fichiers"));

	NodeMenuItem fichier3Clear = createMenuItem(TR.tr("Vider la liste"), "vider", new KeyCodeCombination(KeyCode.W, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Vide la liste des fichiers"), false, true, false);

	NodeMenuItem fichier4Save = createMenuItem(TR.tr("Sauvegarder l'édition"), "sauvegarder", new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Sauvegarde les éléments d'édition du document courant. Le fichier PDF pré-existant ne sera pas modifié"), true, false, false);

	NodeMenuItem fichier5Delete = createMenuItem(TR.tr("Supprimer l'édition"), "supprimer", null,
			TR.tr("Supprime les éléments d'édition du document courant"), true, false, false);

	NodeMenuItem fichier6DeleteAll = createMenuItem(TR.tr("Supprimer les éditions des fichiers ouverts"), "supprimer", null,
			TR.tr("Supprime les éditions de tous les fichiers ouverts dans le panneau des fichiers"));

	NodeMenuItem fichier7Close = createMenuItem(TR.tr("Fermer le document"), "fermer", new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ferme la vue du document courant"), true, false, false);

	Menu fichier8SameName = createSubMenu(TR.tr("Éditions des documents du même nom"), "memenom",
			TR.tr("Déplace l'édition de ce document sur un autre document qui porte le même nom. Cette fonction peut être utilisée lorsqu'un fichier PDF a été déplacé. En effet, si un document PDF est déplacé dans un autre dossier, PDF4Teachers n'arrivera plus à récupérer son édition, sauf avec cette fonction"), true);

	MenuItem fichier8SameNameNull = new MenuItem(TR.tr("Aucune édition trouvée"));

	NodeMenuItem fichier9Export = createMenuItem(TR.tr("Exporter (Regénérer le PDF)"), "exporter", new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Crée un nouveau fichier PDF à partir du document ouvert, avec tous les éléments ajoutés"), true, false, false);

	NodeMenuItem fichier10ExportAll = createMenuItem(TR.tr("Tout exporter"), "exporter", new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Crée des nouveaux fichiers PDF à partir chacun des fichiers de la liste des fichiers, avec pour chaque fichier, tous les éléments de son édition"), false, true, false);

	NodeMenuItem fichier11Convert = createMenuItem(TR.tr("Convertir"), "convert", new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Permet de convertir des images en fichiers PDF"), false, false, false);

	////////// PREFS //////////

	Menu preferences = new Menu(TR.tr("Préférences"));

	NodeMenuItem preferences1Language = createMenuItem(TR.tr("Langage") + " (" + Main.settings.getLanguage() + ")", "language", null,
			TR.tr("Définit la langue de l'interface"), true);

	NodeRadioMenuItem preferences2Restore = createRadioMenuItem(TR.tr("Toujours restaurer la session précédente"), "recharger",
			TR.tr("Réouvre les derniers fichiers ouverts lors de l'ouverture de l'application."), true);

	NodeRadioMenuItem preferences3Update = createRadioMenuItem(TR.tr("Alerter quand une mise à jour est disponible"), "wifi",
			TR.tr("Fait apparaître une fenêtre à chaque démarrage si une nouvelle version est disponible. Même si cette option est désactivée, l'application vérifira si une nouvelle version est disponible et affichera le menu À propos en couleur"), true);

	NodeMenuItem preferences4Zoom = createMenuItem(TR.tr("Zoom lors de l'ouverture d'un document"), "zoom", null,
			TR.tr("Définit le zoom par défaut lors de l'ouverture d'un document. Le zoom est aussi contrôlé avec Ctrl+Molette ou pincement sur trackpad"), true);

	NodeRadioMenuItem preferences5Animation = createRadioMenuItem(TR.tr("Animations de zoom ou défilement"), "cloud",
			TR.tr("Permet des transitions fluides lors d'un zoom ou d'un défilement de la page. Il est possible de désactiver cette option si l'ordinateur est lent lors du zoom. Cette option est déconseillée aux utilisateurs de TrackPad"), true);

	NodeRadioMenuItem preferences6DarkTheme = createRadioMenuItem(TR.tr("Thème sombre"), "settings",
			TR.tr("Change les couleurs de l'interface vers un thème plus sombre."), true);


	NodeRadioMenuItem preferences7Save = createRadioMenuItem(TR.tr("Sauvegarder automatiquement"), "sauvegarder",
			TR.tr("Sauvegarde l'édition du document automatiquement lors de la fermeture du document ou de l'application."), true);

	NodeRadioMenuItem preferences8Regular = createRadioMenuItem(TR.tr("Sauvegarder régulièrement"), "sauvegarder-recharger",
			TR.tr("Sauvegarde l'édition du document automatiquement toutes les x minutes."), false);


	NodeRadioMenuItem preferences9RemoveWhenAdd = createRadioMenuItem(TR.tr("Supprimer l'élément des éléments précédents\nlorsqu'il est ajouté aux favoris"), "favoris",
			TR.tr("Dans la liste des derniers éléments textuels utilisés, retire automatiquement l'élément lorsqu'il est ajouté aux favoris."), true);

	NodeRadioMenuItem preferences10ShowStart = createRadioMenuItem(TR.tr("N'afficher que le début des éléments textuels"), "lines",
			TR.tr("Dans les liste des éléments textuels, n'affiche que les deux premières lignes de l'élément."), true);

	NodeRadioMenuItem preferences11SmallFont = createRadioMenuItem(TR.tr("Réduire la taille des éléments dans les listes"), "cursor",
			TR.tr("Dans les liste des éléments textuels, affiche les éléments en plus petit."), true);


	////////// OTHER //////////

	public Menu apropos = new Menu();

	Menu aide = new Menu(TR.tr("Aide"));
	MenuItem aide1Doc = new MenuItem(TR.tr("Charger le document d'aide"));
	MenuItem aide2Probleme = new MenuItem(TR.tr("Demander de l'aide ou signaler un Bug sur GitHub"));
	MenuItem aide3Twitter = new MenuItem(TR.tr("Nous contacter sur Twitter"));

	////////// OFF TOPIC //////////

	private static ColorAdjust colorAdjust = new ColorAdjust();
	static {
		if(StyleManager.ACCENT_STYLE == jfxtras.styles.jmetro.Style.DARK) colorAdjust.setBrightness(-0.5);
		else colorAdjust.setBrightness(-1);
	}

	public MenuBar(){
		setup();
	}
	public void setup(){

		////////// FICHIER //////////

		fichier8SameName.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
		fichier8SameName.getItems().add(fichier8SameNameNull);

		fichier.getItems().addAll(fichier1Open, fichier2OpenDir, fichier3Clear, new SeparatorMenuItem(), fichier4Save, fichier5Delete, fichier6DeleteAll, fichier7Close, fichier8SameName, new SeparatorMenuItem(), fichier9Export, fichier10ExportAll, fichier11Convert);

		////////// PREFS //////////

		// DEFINE DEFAULT STATE
		preferences2Restore.selectedProperty().set(Main.settings.isRestoreLastSession());
		preferences3Update.selectedProperty().set(Main.settings.isCheckUpdates());
		preferences5Animation.selectedProperty().set(Main.settings.isZoomAnimations());
		preferences6DarkTheme.selectedProperty().set(Main.settings.isDarkTheme());

		preferences7Save.selectedProperty().set(Main.settings.isAutoSave());
		preferences8Regular.selectedProperty().set(Main.settings.getRegularSaving() != -1);

		preferences9RemoveWhenAdd.selectedProperty().set(Main.settings.isRemoveElementInPreviousListWhenAddingToFavorites());
		preferences10ShowStart.selectedProperty().set(Main.settings.isShowOnlyStartInTextsList());
		preferences11SmallFont.selectedProperty().set(Main.settings.isSmallFontInTextsList());

		// BIND
		Main.settings.restoreLastSessionProperty().bind(preferences2Restore.selectedProperty());
		Main.settings.checkUpdatesProperty().bind(preferences3Update.selectedProperty());
		Main.settings.zoomAnimationsProperty().bind(preferences5Animation.selectedProperty());
		Main.settings.darkThemeProperty().bind(preferences6DarkTheme.selectedProperty());

		Main.settings.autoSavingProperty().bind(preferences7Save.selectedProperty());

		Main.settings.removeElementInPreviousListWhenAddingToFavoritesProperty().bind(preferences9RemoveWhenAdd.selectedProperty());
		Main.settings.showOnlyStartInTextsListProperty().bind(preferences10ShowStart.selectedProperty());
		Main.settings.smallFontInTextsListProperty().bind(preferences11SmallFont.selectedProperty());

		// ADD
		preferences.getItems().addAll(preferences1Language, preferences2Restore, preferences3Update, preferences4Zoom, preferences5Animation, preferences6DarkTheme,
				new SeparatorMenuItem(), preferences7Save, preferences8Regular,
				new SeparatorMenuItem(), preferences9RemoveWhenAdd, preferences10ShowStart, preferences11SmallFont);

		////////// OTHER //////////

		aide1Doc.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/info.png")+"", 0, 0, colorAdjust));
		aide2Probleme.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/partager.png")+"", 0, 0, colorAdjust));
		aide3Twitter.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/twitter.png")+"", 0, 0, colorAdjust));
		aide.getItems().addAll(aide1Doc, aide2Probleme, aide3Twitter);

		////////// SETUP ITEMS WIDTH ///////////

		NodeMenuItem.setupMenu(fichier);
		NodeMenuItem.setupMenu(preferences);

		////////// FICHIER //////////

		fichier1Open.setOnAction((ActionEvent actionEvent) -> {

			final FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(TR.tr("Fichier PDF"), "*.pdf"));
			chooser.setTitle(TR.tr("Sélectionner un ou plusieurs fichier"));
			chooser.setInitialDirectory((UserData.lastOpenDir.exists() ? UserData.lastOpenDir : new File(System.getProperty("user.home"))));

			List<File> listFiles = chooser.showOpenMultipleDialog(Main.window);
			if(listFiles != null){
				File[] files = new File[listFiles.size()];
				files = listFiles.toArray(files);
				MainWindow.lbFilesTab.openFiles(files);
				if(files.length == 1){
					MainWindow.mainScreen.openFile(files[0]);
				}
				UserData.lastOpenDir = files[0].getParentFile();

			}
		});
		fichier2OpenDir.setOnAction((ActionEvent actionEvent) -> {

			final DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle(TR.tr("Sélectionner un dossier"));
			chooser.setInitialDirectory((UserData.lastOpenDir.exists() ? UserData.lastOpenDir : new File(System.getProperty("user.home"))));

			File file = chooser.showDialog(Main.window);
			if(file != null) {
				MainWindow.lbFilesTab.openFiles(new File[]{file});
				UserData.lastOpenDir = file.getParentFile();
			}
		});
		fichier3Clear.setOnAction((ActionEvent actionEvent) -> {
			MainWindow.lbFilesTab.clearFiles();
		});
		fichier4Save.setOnAction((ActionEvent actionEvent) -> {
			if(MainWindow.mainScreen.hasDocument(true)){
				MainWindow.mainScreen.document.edition.save();
			}
		});
		fichier5Delete.setOnAction((ActionEvent e) -> {
			if(MainWindow.mainScreen.hasDocument(true)){
				MainWindow.mainScreen.document.edition.clearEdit(true);
			}
		});
		fichier6DeleteAll.setOnAction((ActionEvent e) -> {
			Alert dialog = Builders.getAlert(Alert.AlertType.WARNING, TR.tr("Supprimer les éditions"));
			dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir supprimer toutes les éditions des fichiers de la liste ?"));


			float yesButSize = FilesUtils.convertOctetToMo(FilesUtils.getSize(new File(Main.dataFolder + "editions")));
			float yesSize = 0L;
			for(File file : MainWindow.lbFilesTab.files.getItems()){
				File editFile = Edition.getEditFile(file);
				yesSize += FilesUtils.getSize(editFile);
			}yesSize = FilesUtils.convertOctetToMo((long) yesSize);

			ButtonType cancel = new ButtonType(TR.tr("Non"), ButtonBar.ButtonData.CANCEL_CLOSE);
			ButtonType yes = new ButtonType(TR.tr("Oui") + " (" + yesSize + "Mo)", ButtonBar.ButtonData.OK_DONE);
			ButtonType yesBut = new ButtonType(TR.tr("Supprimer l'ensemble des\néditions enregistrées") + " (" + yesButSize + "Mo)", ButtonBar.ButtonData.OTHER);
			dialog.getButtonTypes().setAll(yesBut, cancel, yes);

			Optional<ButtonType> option = dialog.showAndWait();
			float size = 0L;
			if(option.get() == yes){
				if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.edition.clearEdit(false);
				for(File file : MainWindow.lbFilesTab.files.getItems()){
					Edition.getEditFile(file).delete();
				}
				size = yesSize;
			}else if(option.get() == yesBut){
				if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.edition.clearEdit(false);
				for(File file : new File(Main.dataFolder + "editions").listFiles()){
					file.delete();
				}
				size = yesButSize;
			}else return;

			Alert alert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Supression terminée"));
			alert.setHeaderText(TR.tr("Vos éditions ont bien été supprimés."));
			alert.setContentText(TR.tr("Vous avez supprimé") + " " + size + "Mo");
			alert.show();
		});
		fichier7Close.setOnAction((ActionEvent e) -> {
			if(MainWindow.mainScreen.hasDocument(true)){
				MainWindow.mainScreen.closeFile(true);
			}
		});
		fichier8SameName.setOnShowing((Event event) -> {
			fichier8SameName.getItems().clear();
			int i = 0;
			for(Map.Entry<File, File> files : Edition.getEditFilesWithSameName(MainWindow.mainScreen.document.getFile()).entrySet()){

				MenuItem item = new MenuItem(files.getValue().getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + File.separator);
				fichier8SameName.getItems().add(item);
				item.setOnAction((ActionEvent actionEvent) -> {
					Alert dialog = Builders.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Charger une autre édition"));
					dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer l'édition courante par celle-ci ?"));

					ButtonType cancel = new ButtonType(TR.tr("Non"), ButtonBar.ButtonData.CANCEL_CLOSE);
					ButtonType yes = new ButtonType(TR.tr("Oui"), ButtonBar.ButtonData.OK_DONE);
					ButtonType yesAll = new ButtonType(TR.tr("Oui, répéter cette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
					dialog.getButtonTypes().setAll(cancel, yes, yesAll);

					Optional<ButtonType> option = dialog.showAndWait();
					if(option.get() == yes){
						if(MainWindow.mainScreen.hasDocument(true)){

							MainWindow.mainScreen.document.edition.clearEdit(false);
							Edition.mergeEditFileWithEditFile(files.getKey(), Edition.getEditFile(MainWindow.mainScreen.document.getFile()));
							MainWindow.mainScreen.document.loadEdition();
						}
					}else if(option.get() == yesAll){
						if(MainWindow.mainScreen.hasDocument(true)){

							MainWindow.mainScreen.document.edition.clearEdit(false);
							Edition.mergeEditFileWithEditFile(files.getKey(), Edition.getEditFile(MainWindow.mainScreen.document.getFile()));
							MainWindow.mainScreen.document.loadEdition();

							for(File otherFileDest : MainWindow.lbFilesTab.files.getItems()){
								if(otherFileDest.getParentFile().getAbsolutePath().equals(MainWindow.mainScreen.document.getFile().getParentFile().getAbsolutePath()) && !otherFileDest.equals(MainWindow.mainScreen.document.getFile())){
									File fromEditFile = Edition.getEditFile(new File(files.getValue().getParentFile().getAbsolutePath() + "/" + otherFileDest.getName()));

									if(fromEditFile.exists()){
										Edition.mergeEditFileWithEditFile(fromEditFile, Edition.getEditFile(otherFileDest));
									}else{
										Alert alert = Builders.getAlert(Alert.AlertType.ERROR, TR.tr("Fichier introuvable"));
										alert.setHeaderText(TR.tr("Le fichier") + " \"" + otherFileDest.getName() + "\" " + TR.tr("dans") + " \"" + files.getValue().getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + "\" " + TR.tr("n'a pas d'édition."));
										ButtonType ok = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.OK_DONE);
										ButtonType cancelAll = new ButtonType(TR.tr("Tout Arreter"), ButtonBar.ButtonData.CANCEL_CLOSE);
										alert.getButtonTypes().setAll(ok, cancelAll);

										Optional<ButtonType> option2 = alert.showAndWait();
										if(option2.get() == cancelAll) return;
									}
								}
							}
						}
					}
				});
				i++;
			}
			if(i == 0) fichier8SameName.getItems().add(fichier8SameNameNull);
			Builders.setMenuSize(fichier8SameName);
		});
		fichier9Export.setOnAction((ActionEvent actionEvent) -> {

			MainWindow.mainScreen.document.save();
			new ExportWindow(Collections.singletonList(MainWindow.mainScreen.document.getFile()));

		});
		fichier10ExportAll.setOnAction((ActionEvent actionEvent) -> {

			if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
			new ExportWindow(MainWindow.lbFilesTab.files.getItems());

		});
		fichier11Convert.setOnAction(e -> {
			new ConvertDocument();
		});

		////////// PREFS //////////

		preferences1Language.setOnAction(e -> {
			new LanguageWindow(value -> {
				if(!value.isEmpty()){
					Main.settings.setLanguage(value);
					TR.updateTranslation();

					MainWindow.userData.saveData();
					MainWindow.hasToClose = true;
					if(MainWindow.mainScreen.closeFile(true)){
						Main.window.close();
						MainWindow.hasToClose = false;
						Platform.runLater(Main::startMainWindow);
					}
					MainWindow.hasToClose = false;
				}
			});
		});
		preferences4Zoom.setOnAction((ActionEvent actionEvent) -> {


			List<Integer> choices = new ArrayList<>(Arrays.asList(50, 70, 80, 90, 100, 110, 120, 140, 160, 180, 200, 230, 250, 280, 300));
			ChoiceDialog<Integer> dialog = new ChoiceDialog<>(Main.settings.getDefaultZoom(), choices);
			Builders.setupDialog(dialog);

			dialog.setTitle(TR.tr("Zoom par défaut"));
			dialog.setHeaderText(TR.tr("Zoom par défaut lors de l'ouverture d'un document"));
			dialog.setContentText(TR.tr("Choisir un pourcentage :"));

			Optional<Integer> newZoom = dialog.showAndWait();
			if(!newZoom.isEmpty()){
				Main.settings.setDefaultZoom(newZoom.get());
			}

		});
		preferences8Regular.setOnAction((ActionEvent actionEvent) -> {

			Alert dialog = Builders.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Sauvegarde régulière"));

			HBox pane = new HBox();
			ComboBox<Integer> combo = new ComboBox<>(FXCollections.observableArrayList(1, 5, 10, 15, 20, 30, 45, 60));
			combo.getSelectionModel().select(Main.settings.getRegularSaving() == -1 ? (Integer) 5 : (Integer) Main.settings.getRegularSaving());
			combo.setStyle("-fx-padding-left: 20px;");
			CheckBox activated = new CheckBox(TR.tr("Activer"));
			activated.setSelected(Main.settings.getRegularSaving() != -1);
			pane.getChildren().add(0, activated);
			pane.getChildren().add(1, combo);
			HBox.setMargin(activated, new Insets(5, 0, 0, 10));
			HBox.setMargin(combo, new Insets(0, 0, 0, 30));

			combo.disableProperty().bind(activated.selectedProperty().not());
			dialog.setHeaderText(TR.tr("Définir le nombre de minutes entre deux sauvegardes automatiques."));

			dialog.getDialogPane().setContent(pane);

			ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
			ButtonType ok = new ButtonType(TR.tr("OK"), ButtonBar.ButtonData.OK_DONE);
			dialog.getButtonTypes().setAll(cancel, ok);

			Optional<ButtonType> option = dialog.showAndWait();
			if(option.get() == ok){
				if(activated.isSelected()){
					Main.settings.setRegularSaving(combo.getSelectionModel().getSelectedItem());
					preferences8Regular.setSelected(true);
				}else{
					Main.settings.setRegularSaving(-1);
					preferences8Regular.setSelected(false);
				}

			}


		});

		////////// OTHER //////////

		aide1Doc.setOnAction((ActionEvent actionEvent) -> {
			MainWindow.mainScreen.openFile(LanguageWindow.getDocFile());
		});
		aide2Probleme.setOnAction((ActionEvent actionEvent) -> {
			try{
				Desktop.getDesktop().browse(new URI("https://github.com/themsou/PDF4Teachers/issues/new"));
			}catch(IOException | URISyntaxException e){ e.printStackTrace(); }
		});
		aide3Twitter.setOnAction((ActionEvent t) -> {
			Main.hostServices.showDocument("https://twitter.com/PDF4Teachers");
		});

		fichier.setStyle("-fx-font-size: 12;");
		preferences.setStyle("-fx-font-size: 12;");
		aide.setStyle("-fx-font-size: 12;");

		Label name = new Label(TR.tr("À propos"));
		name.setStyle("-fx-font-size: 12;");
		name.setAlignment(Pos.CENTER_LEFT);
		name.setOnMouseClicked(event -> new AboutWindow());
		apropos.setGraphic(name);

		// UI Style
		setStyle("");
		StyleManager.putStyle(this, Style.ACCENT);
		getMenus().addAll(fichier, preferences, apropos, aide);

		for(Menu menu : getMenus()){
			Builders.setMenuSize(menu);
		}
	}

	public Menu createSubMenu(String name, String imgName, String toolTip, boolean disableIfNoDoc){

		Menu menu = new Menu();
		HBox pane = new HBox();

		Label text = new Label(name);
		text.setStyle("-fx-font-size: 13; -fx-padding: 2 0 2 10;"); // top - right - bottom - left


		ImageView icon = Builders.buildImage(getClass().getResource("/img/MenuBar/" + imgName + ".png")+"", 0, 0, colorAdjust);

		if(disableIfNoDoc){
			menu.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
		}

		Tooltip toolTipUI = Builders.genToolTip(toolTip);
		toolTipUI.setShowDuration(Duration.INDEFINITE);
		Tooltip.install(pane, toolTipUI);

		pane.getChildren().addAll(icon, text);
		menu.setGraphic(pane);

		return menu;
	}
	public NodeRadioMenuItem createRadioMenuItem(String text, String imgName, String toolTip, boolean autoUpdate){

		NodeRadioMenuItem menuItem = new NodeRadioMenuItem(new HBox(), text + "      ", true, autoUpdate);

		if(imgName != null) menuItem.setImage(Builders.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0, colorAdjust));
		if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);

		return menuItem;

	}
	public NodeMenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, boolean leftMargin){

		NodeMenuItem menuItem = new NodeMenuItem(new HBox(), text + "         ", true);


		if(imgName != null) menuItem.setImage(Builders.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0, colorAdjust));
		if(keyCombinaison != null) menuItem.setKeyCombinaison(keyCombinaison);
		if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);
		if(leftMargin) menuItem.setFalseLeftData();

		if(disableIfNoDoc){
			menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
		}if(disableIfNoList){
			menuItem.disableProperty().bind(Bindings.size(MainWindow.lbFilesTab.files.getItems()).isEqualTo(0));
		}

		return menuItem;

	}
	public NodeMenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip){
		return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, false);
	}
	public NodeMenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean leftMargin){
		return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, leftMargin);
	}
}
