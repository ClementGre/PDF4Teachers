package fr.themsou.panel;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.EditionUtils;
import fr.themsou.document.render.convert.ConvertDocument;
import fr.themsou.document.render.display.PageEditPane;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.UserData;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.utils.components.NodeMenuItem;
import fr.themsou.utils.components.NodeRadioMenuItem;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.main.Main;
import fr.themsou.utils.*;
import fr.themsou.windows.LanguageWindow;
import fr.themsou.windows.LogWindow;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
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

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar{

	////////// FILE //////////

	Menu file = new Menu(TR.tr("Fichier"));
	public NodeMenuItem file1Open = createMenuItem(TR.tr("Ouvrir un ou plusieurs fichiers"), "ouvrir", new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ajoute un ou plusieurs fichiers dans le panneau des fichiers."));

	public NodeMenuItem file2OpenDir = createMenuItem(TR.tr("Ouvrir un dossier"), "directory", new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ajoute tous les fichiers PDF d'un dossier dans le panneau des fichiers"));

	NodeMenuItem file3Clear = createMenuItem(TR.tr("Vider la liste"), "vider", new KeyCodeCombination(KeyCode.W, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Vide la liste des fichiers"), false, true, false);

	NodeMenuItem file4Save = createMenuItem(TR.tr("Sauvegarder l'édition"), "sauvegarder", new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Sauvegarde les éléments d'édition du document courant. Le fichier PDF pré-existant ne sera pas modifié"), true, false, false);

	NodeMenuItem file5Delete = createMenuItem(TR.tr("Supprimer l'édition"), "supprimer", null,
			TR.tr("Supprime les éléments d'édition du document courant"), true, false, false);

	NodeMenuItem file6Close = createMenuItem(TR.tr("Fermer le document"), "fermer", new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ferme la vue du document courant"), true, false, false);

	NodeMenuItem file7Export = createMenuItem(TR.tr("Exporter (Regénérer le PDF)"), "export", new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Crée un nouveau fichier PDF à partir du document ouvert, avec tous les éléments ajoutés"), true, false, false);

	NodeMenuItem file8ExportAll = createMenuItem(TR.tr("Tout exporter"), "export-all", new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Crée des nouveaux fichiers PDF à partir chacun des fichiers de la liste des fichiers, avec pour chaque fichier, tous les éléments de son édition"), false, true, false);


	////////// TOOLS //////////

	Menu tools = new Menu(TR.tr("Outils"));

	NodeMenuItem tools1Convert = createMenuItem(TR.tr("Convertir"), "convert", new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Permet de convertir des images en fichiers PDF"), false, false, false);

	NodeMenuItem tools2QRCode = createMenuItem(TR.tr("Générer un QR Code"), "qrcode", null,
			TR.tr("Permet d'ajouter un QR Code généré par l'application au document PDF ouvert"), true, false, false);

	Menu tools3AddPages = createSubMenu(TR.tr("Ajouter des pages"), "more",
			TR.tr("Ajouter des pages à ce document PDF. Cette option est aussi disponible avec les boutons aux pieds de pages"), true);

	NodeMenuItem tools4DeleteAllEdits = createMenuItem(TR.tr("Supprimer les éditions des fichiers ouverts"), "delete", null,
			TR.tr("Supprime les éditions de tous les fichiers ouverts dans le panneau des fichiers"));

	Menu tools5SameNameEditions = createSubMenu(TR.tr("Éditions des documents du même nom"), "cross-way",
			TR.tr("Déplace l'édition de ce document sur un autre document qui porte le même nom. Cette fonction peut être utilisée lorsqu'un fichier PDF a été déplacé. En effet, si un document PDF est déplacé dans un autre dossier, PDF4Teachers n'arrivera plus à récupérer son édition, sauf avec cette fonction"), true);
	MenuItem tools5SameNameEditionsNull = new MenuItem(TR.tr("Aucune édition trouvée"));

	Menu tools6ExportEdition = createSubMenu(TR.tr("Exporter l'édition"), "export",
			TR.tr("Générer un fichier qui peut être enregistré sur votre ordinateur à partir de l'édition de ce document"), true);

		NodeMenuItem tools6ExportEdition1All = createMenuItem(TR.tr("Exporter l'édition"), null, null,
				TR.tr("Génère un fichier contenant l'édition du document"), true, false, false, false);
		NodeMenuItem tools6ExportEdition2Grades = createMenuItem(TR.tr("Exporter le barème"), null, null,
				TR.tr("Remplace le barème du document ouvert par celui d'un fichier de barème"), true, false, false, false);

	Menu tools7ImportEdition = createSubMenu(TR.tr("Importer une édition"), "import",
			TR.tr("Remplace l'édition du document ouvert par celle d'un fichier d'édition"), true);

		NodeMenuItem tools7ImportEdition1All = createMenuItem(TR.tr("Importer une édition"), null, null,
				TR.tr("Remplace l'édition du document ouvert par celle d'un fichier d'édition"), true, false, false, false);
		NodeMenuItem tools7ImportEdition2Grades = createMenuItem(TR.tr("Importer un barème"), null, null,
				TR.tr("Remplace le barème du document ouvert par celle d'un fichier de barème"), true, false, false, false);

	Menu tools8Debug = createSubMenu(TR.tr("Débug"), "command-prompt",
			TR.tr("Options plus complexes qui vous demandent une certaine connaissance en informatique."), false);

		NodeMenuItem tools8Debug1OpenConsole = createMenuItem(TR.tr("Ouvrir la console d'exécution") + " (" + (Main.COPY_CONSOLE ? "Activée" : "Désactivée") + ")", null, new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHORTCUT_DOWN),
				TR.tr("Ouvre la console de l'application"), false, false, false, false);
		NodeMenuItem tools8Debug2OpenAppFolder = createMenuItem(TR.tr("Ouvrir le dossier de données"), null, null,
				TR.tr("Ouvre le dossier où PDF4Teachers enregistre toutes ses données"), false, false, false, false);
		NodeMenuItem tools8Debug3OpenEditionFile = createMenuItem(TR.tr("Ouvrir le fichier d'édition"), null, null,
				TR.tr("Ouvre le fichier qui contient les données de l'édition actuelle"), true, false, false, false);

	////////// SETTINGS //////////

	Menu settings = new Menu(TR.tr("Préférences"));

	NodeMenuItem settings1Language = createMenuItem(TR.tr("Langage") + " (" + Main.settings.getLanguage() + ")", "language", null,
			TR.tr("Définit la langue de l'interface"), true);

	NodeRadioMenuItem settings2AlwaysRestore = createRadioMenuItem(TR.tr("Toujours restaurer la session précédente"), "recharger",
			TR.tr("Réouvre les derniers fichiers ouverts lors de l'ouverture de l'application."), true);

	NodeRadioMenuItem settings3AlertUpdate = createRadioMenuItem(TR.tr("Alerter quand une mise à jour est disponible"), "wifi",
			TR.tr("Fait apparaître une fenêtre à chaque démarrage si une nouvelle version est disponible. Même si cette option est désactivée, l'application vérifira si une nouvelle version est disponible et affichera le menu À propos en couleur"), true);

	NodeMenuItem settings4DefaultZoom = createMenuItem(TR.tr("Zoom lors de l'ouverture d'un document"), "zoom", null,
			TR.tr("Définit le zoom par défaut lors de l'ouverture d'un document. Le zoom est aussi contrôlé avec Ctrl+Molette ou pincement sur trackpad"), true);

	NodeRadioMenuItem settings5Animation = createRadioMenuItem(TR.tr("Animations de zoom ou défilement"), "cloud",
			TR.tr("Permet des transitions fluides lors d'un zoom ou d'un défilement de la page. Il est possible de désactiver cette option si l'ordinateur est lent lors du zoom. Cette option est déconseillée aux utilisateurs de TrackPad"), true);

	NodeRadioMenuItem settings6DarkTheme = createRadioMenuItem(TR.tr("Thème sombre"), "settings",
			TR.tr("Change les couleurs de l'interface vers un thème plus sombre."), true);


	NodeRadioMenuItem settings7AutoSave = createRadioMenuItem(TR.tr("Sauvegarder automatiquement"), "sauvegarder",
			TR.tr("Sauvegarde l'édition du document automatiquement lors de la fermeture du document ou de l'application."), true);

	NodeRadioMenuItem settings8RegularSave = createRadioMenuItem(TR.tr("Sauvegarder régulièrement"), "sauvegarder-recharger",
			TR.tr("Sauvegarde l'édition du document automatiquement toutes les x minutes."), false);


	NodeRadioMenuItem settings9TextAutoRemove = createRadioMenuItem(TR.tr("Supprimer l'élément des éléments précédents\nlorsqu'il est ajouté aux favoris"), "favoris",
			TR.tr("Dans la liste des derniers éléments textuels utilisés, retire automatiquement l'élément lorsqu'il est ajouté aux favoris."), true);

	NodeRadioMenuItem settings10TextOnlyStart = createRadioMenuItem(TR.tr("N'afficher que le début des éléments textuels"), "lines",
			TR.tr("Dans les liste des éléments textuels, n'affiche que les deux premières lignes de l'élément."), true);

	NodeRadioMenuItem settings11TextSmall = createRadioMenuItem(TR.tr("Réduire la taille des éléments dans les listes"), "cursor",
			TR.tr("Dans les liste des éléments textuels, affiche les éléments en plus petit."), true);


	////////// ABOUT / HELP //////////

	public Menu about = new Menu();

	Menu help = new Menu(TR.tr("Aide"));
	MenuItem help1LoadDoc = new MenuItem(TR.tr("Charger le document d'aide"));
	MenuItem help2GitHubIssue = new MenuItem(TR.tr("Demander de l'aide ou signaler un Bug sur GitHub"));
	MenuItem help3Twitter = new MenuItem(TR.tr("Nous contacter sur Twitter"));
	MenuItem help4Website = new MenuItem(TR.tr("Site Web de PDF4Teachers"));

	////////// ICONS COLOR //////////

	private static ColorAdjust colorAdjust = new ColorAdjust();
	static {
		if(StyleManager.ACCENT_STYLE == jfxtras.styles.jmetro.Style.DARK) colorAdjust.setBrightness(-0.5);
		else colorAdjust.setBrightness(-1);
	}

	public MenuBar(){
		setup();
	}
	public void setup(){

		////////// FILE //////////

		file.getItems().addAll(file1Open, file2OpenDir, file3Clear, new SeparatorMenuItem(), file4Save, file5Delete, file6Close, new SeparatorMenuItem(), file7Export, file8ExportAll);

		////////// TOOLS //////////

		tools3AddPages.getItems().add(new MenuItem());
		tools6ExportEdition.getItems().addAll(tools6ExportEdition1All, tools6ExportEdition2Grades);
		tools7ImportEdition.getItems().addAll(tools7ImportEdition1All, tools7ImportEdition2Grades);
		tools5SameNameEditions.getItems().add(tools5SameNameEditionsNull);
		tools8Debug.getItems().addAll(tools8Debug1OpenConsole, tools8Debug2OpenAppFolder, tools8Debug3OpenEditionFile);

		tools.getItems().addAll(tools1Convert, /*tools2QRCode,*/ tools3AddPages, new SeparatorMenuItem(), tools4DeleteAllEdits, tools5SameNameEditions, tools6ExportEdition, tools7ImportEdition, new SeparatorMenuItem(), tools8Debug);

		////////// SETTINGS //////////

		// DEFINE DEFAULT STATE
		settings2AlwaysRestore.selectedProperty().set(Main.settings.isRestoreLastSession());
		settings3AlertUpdate.selectedProperty().set(Main.settings.isCheckUpdates());
		settings5Animation.selectedProperty().set(Main.settings.isZoomAnimations());
		settings6DarkTheme.selectedProperty().set(Main.settings.isDarkTheme());

		settings7AutoSave.selectedProperty().set(Main.settings.isAutoSave());
		settings8RegularSave.selectedProperty().set(Main.settings.getRegularSaving() != -1);

		settings9TextAutoRemove.selectedProperty().set(Main.settings.isRemoveElementInPreviousListWhenAddingToFavorites());
		settings10TextOnlyStart.selectedProperty().set(Main.settings.isShowOnlyStartInTextsList());
		settings11TextSmall.selectedProperty().set(Main.settings.isSmallFontInTextsList());

		// BIND
		Main.settings.restoreLastSessionProperty().bind(settings2AlwaysRestore.selectedProperty());
		Main.settings.checkUpdatesProperty().bind(settings3AlertUpdate.selectedProperty());
		Main.settings.zoomAnimationsProperty().bind(settings5Animation.selectedProperty());
		Main.settings.darkThemeProperty().bind(settings6DarkTheme.selectedProperty());

		Main.settings.autoSavingProperty().bind(settings7AutoSave.selectedProperty());

		Main.settings.removeElementInPreviousListWhenAddingToFavoritesProperty().bind(settings9TextAutoRemove.selectedProperty());
		Main.settings.showOnlyStartInTextsListProperty().bind(settings10TextOnlyStart.selectedProperty());
		Main.settings.smallFontInTextsListProperty().bind(settings11TextSmall.selectedProperty());

		// ADD
		settings.getItems().addAll(settings1Language, settings2AlwaysRestore, settings3AlertUpdate, settings4DefaultZoom, settings5Animation, settings6DarkTheme,
				new SeparatorMenuItem(), settings7AutoSave, settings8RegularSave,
				new SeparatorMenuItem(), settings9TextAutoRemove, settings10TextOnlyStart, settings11TextSmall);

		////////// HELP //////////

		help1LoadDoc.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/info.png")+"", 0, 0, colorAdjust));
		help2GitHubIssue.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/partager.png")+"", 0, 0, colorAdjust));
		help3Twitter.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/twitter.png")+"", 0, 0, colorAdjust));
		help4Website.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/language.png")+"", 0, 0, colorAdjust));
		help.getItems().addAll(help1LoadDoc, help2GitHubIssue, help3Twitter, help4Website);

		////////// SETUP ITEMS WIDTH ///////////

		NodeMenuItem.setupMenu(file);
		NodeMenuItem.setupMenu(tools);
		NodeMenuItem.setupMenu(settings);

		////////// FILE //////////

		file1Open.setOnAction((ActionEvent actionEvent) -> {

			File[] files = Builders.showFilesDialog(true, true, TR.tr("Fichier PDF"), "*.pdf");
			if(files != null){
				MainWindow.lbFilesTab.openFiles(files);
				if(files.length == 1){
					MainWindow.mainScreen.openFile(files[0]);
				}
			}
		});
		file2OpenDir.setOnAction((ActionEvent actionEvent) -> {

			File directory = Builders.showDirectoryDialog(true);
			if(directory != null) {
				MainWindow.lbFilesTab.openFiles(new File[]{directory});
			}
		});
		file3Clear.setOnAction((ActionEvent actionEvent) -> {
			MainWindow.lbFilesTab.clearFiles();
		});
		file4Save.setOnAction((ActionEvent actionEvent) -> {
			if(MainWindow.mainScreen.hasDocument(true)){
				MainWindow.mainScreen.document.edition.save();
			}
		});
		file5Delete.setOnAction((ActionEvent e) -> {
			if(MainWindow.mainScreen.hasDocument(true)){
				MainWindow.mainScreen.document.edition.clearEdit(true);
			}
		});
		file6Close.setOnAction((ActionEvent e) -> {
			if(MainWindow.mainScreen.hasDocument(true)){
				MainWindow.mainScreen.closeFile(true);
			}
		});
		file7Export.setOnAction((ActionEvent actionEvent) -> {

			MainWindow.mainScreen.document.save();
			new ExportWindow(Collections.singletonList(MainWindow.mainScreen.document.getFile()));

		});
		file8ExportAll.setOnAction((ActionEvent actionEvent) -> {

			if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
			new ExportWindow(MainWindow.lbFilesTab.files.getItems());

		});

		////////// TOOLS //////////

		tools1Convert.setOnAction(e -> {
			new ConvertDocument();
		});

		tools2QRCode.setOnAction(e -> {

		});

		tools3AddPages.setOnShowing(e -> {
			tools3AddPages.getItems().setAll(PageEditPane.getNewPageMenu(0, MainWindow.mainScreen.document.totalPages));
			Builders.setMenuSize(tools3AddPages);
		});

		tools4DeleteAllEdits.setOnAction((ActionEvent e) -> {
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
		tools5SameNameEditions.setOnShowing((Event event) -> {
			tools5SameNameEditions.getItems().clear();
			int i = 0;
			for(Map.Entry<File, File> files : Edition.getEditFilesWithSameName(MainWindow.mainScreen.document.getFile()).entrySet()){

				MenuItem item = new MenuItem(files.getValue().getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + File.separator);
				tools5SameNameEditions.getItems().add(item);
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
			if(i == 0) tools5SameNameEditions.getItems().add(tools5SameNameEditionsNull);
			Builders.setMenuSize(tools5SameNameEditions);
		});

		tools6ExportEdition1All.setOnAction((e) -> EditionUtils.showExportDialog(false));
		tools6ExportEdition2Grades.setOnAction((e) -> EditionUtils.showExportDialog(true));
		tools7ImportEdition1All.setOnAction((e) -> EditionUtils.showImportDialog(false));
		tools7ImportEdition2Grades.setOnAction((e) -> EditionUtils.showImportDialog(true));

		tools8Debug1OpenConsole.setOnAction((e) -> new LogWindow());
		tools8Debug2OpenAppFolder.setOnAction((e) -> Main.hostServices.showDocument(Main.dataFolder));
		tools8Debug3OpenEditionFile.setOnAction((e) -> Main.hostServices.showDocument(Edition.getEditFile(MainWindow.mainScreen.document.getFile()).getAbsolutePath()));


		////////// SETTINGS //////////

		settings1Language.setOnAction(e -> {
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
		settings4DefaultZoom.setOnAction((ActionEvent actionEvent) -> {


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
		settings8RegularSave.setOnAction((ActionEvent actionEvent) -> {

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
					settings8RegularSave.setSelected(true);
				}else{
					Main.settings.setRegularSaving(-1);
					settings8RegularSave.setSelected(false);
				}

			}


		});

		////////// ABOUT / HELP //////////

		Label name = new Label(TR.tr("À propos"));
		name.setAlignment(Pos.CENTER_LEFT);
		name.setOnMouseClicked(event -> {
			try{ FXMLLoader.load(getClass().getResource("/fxml/AboutWindow.fxml")); }catch(IOException e){ e.printStackTrace(); }
		});
		about.setGraphic(name);

		help1LoadDoc.setOnAction((ActionEvent actionEvent) -> MainWindow.mainScreen.openFile(LanguageWindow.getDocFile()));
		help2GitHubIssue.setOnAction((ActionEvent actionEvent) -> {
			try{
				Desktop.getDesktop().browse(new URI("https://github.com/themsou/PDF4Teachers/issues/new"));
			}catch(IOException | URISyntaxException e){ e.printStackTrace(); }
		});
		help3Twitter.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://twitter.com/PDF4Teachers"));
		help4Website.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://pdf4teachers.org"));



		////////// ABOUT / HELP //////////

		// UI Style
		setStyle("");
		StyleManager.putStyle(this, Style.ACCENT);
		getMenus().addAll(file, tools, settings, about, help);

		for(Menu menu : getMenus()){
			Builders.setMenuSize(menu);
		}
	}
	public Menu createSubMenu(String name, String imgName, String toolTip, boolean disableIfNoDoc){
		return createSubMenu(name, imgName, toolTip, disableIfNoDoc, true);
	}
	public Menu createSubMenu(String name, String imgName, String toolTip, boolean disableIfNoDoc, boolean fat){

		Menu menu = new Menu();
		HBox pane = new HBox();

		Label text = new Label(name);

		if(imgName != null){
			ImageView icon = Builders.buildImage(getClass().getResource("/img/MenuBar/" + imgName + ".png")+"", 0, 0, colorAdjust);
			pane.getChildren().add(icon);

			if(fat) text.setStyle("-fx-font-size: 13; -fx-padding: 2 0 2 10;"); // top - right - bottom - left
			else text.setStyle("-fx-font-size: 13; -fx-padding: -15 0 -15 10;");
		}else{
			if(fat) text.setStyle("-fx-font-size: 13; -fx-padding: 2 0 2 0;"); // top - right - bottom - left
			else text.setStyle("-fx-font-size: 13; -fx-padding: -15 0 -15 0;");
		}
		pane.getChildren().add(text);

		if(disableIfNoDoc){
			menu.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
		}

		Tooltip toolTipUI = Builders.genToolTip(toolTip);
		toolTipUI.setShowDuration(Duration.INDEFINITE);
		Tooltip.install(pane, toolTipUI);

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
		return createMenuItem(text, imgName, keyCombinaison, toolTip, disableIfNoDoc, disableIfNoList, leftMargin, true);
	}
	public NodeMenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, boolean leftMargin, boolean fat){

		NodeMenuItem menuItem = new NodeMenuItem(new HBox(), text + "         ", fat);


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