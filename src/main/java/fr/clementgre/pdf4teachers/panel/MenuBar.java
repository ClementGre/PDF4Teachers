package fr.clementgre.pdf4teachers.panel;

import fr.clementgre.pdf4teachers.datasaving.settings.Setting;
import fr.clementgre.pdf4teachers.datasaving.settings.SettingObject;
import fr.clementgre.pdf4teachers.datasaving.settings.Settings;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.EditionExporter;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.display.PageEditPane;
import fr.clementgre.pdf4teachers.document.render.export.ExportWindow;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.LogWindow;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguageWindow;
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
import javafx.util.Duration;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar{

	////////// ICONS COLOR //////////

	private static ColorAdjust colorAdjust = new ColorAdjust();
	static {
		if(StyleManager.ACCENT_STYLE == jfxtras.styles.jmetro.Style.DARK) colorAdjust.setBrightness(-0.5);
		else colorAdjust.setBrightness(-1);
	}

	////////// FILE //////////

	Menu file = new Menu(TR.tr("Fichier"));
	public MenuItem file1Open = createMenuItem(TR.tr("Ouvrir un ou plusieurs fichiers"), SVGPathIcons.PDF_FILE, new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ajoute un ou plusieurs fichiers dans le panneau des fichiers."));

	public MenuItem file2OpenDir = createMenuItem(TR.tr("Ouvrir un dossier"), SVGPathIcons.FOLDER, new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ajoute tous les fichiers PDF d'un dossier dans le panneau des fichiers"));

	MenuItem file3Clear = createMenuItem(TR.tr("Vider la liste"), SVGPathIcons.LIST, new KeyCodeCombination(KeyCode.W, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Vide la liste des fichiers"), false, true, false);

	MenuItem file4Save = createMenuItem(TR.tr("Sauvegarder l'édition"), SVGPathIcons.SAVE_LITE, new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Sauvegarde les éléments d'édition du document courant. Le fichier PDF pré-existant ne sera pas modifié"), true, false, false);

	MenuItem file5Delete = createMenuItem(TR.tr("Supprimer l'édition"), SVGPathIcons.TRASH, null,
			TR.tr("Supprime les éléments d'édition du document courant"), true, false, false);

	MenuItem file6Close = createMenuItem(TR.tr("Fermer le document"), SVGPathIcons.CROSS, new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Ferme la vue du document courant"), true, false, false);

	MenuItem file7Export = createMenuItem(TR.tr("Exporter (Regénérer le PDF)"), SVGPathIcons.EXPORT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Crée un nouveau fichier PDF à partir du document ouvert, avec tous les éléments ajoutés"), true, false, false);

	MenuItem file8ExportAll = createMenuItem(TR.tr("Tout exporter"), SVGPathIcons.EXPORT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Crée des nouveaux fichiers PDF à partir chacun des fichiers de la liste des fichiers, avec pour chaque fichier, tous les éléments de son édition"), false, true, false);


	////////// TOOLS //////////

	public Menu tools = new Menu(TR.tr("Outils"));

	MenuItem tools1Convert = createMenuItem(TR.tr("Convertir"), SVGPathIcons.PICTURES, new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
			TR.tr("Permet de convertir des images en fichiers PDF"), false, false, false);

	MenuItem tools2QRCode = createMenuItem(TR.tr("Générer un QR Code"), "qrcode", null,
			TR.tr("Permet d'ajouter un QR Code généré par l'application au document PDF ouvert"), true, false, false);

	Menu tools3AddPages = createSubMenu(TR.tr("Ajouter des pages"), SVGPathIcons.PLUS,
			TR.tr("Ajouter des pages à ce document PDF. Cette option est aussi disponible avec les boutons aux pieds de pages"), true);

	MenuItem tools4DeleteAllEdits = createMenuItem(TR.tr("Supprimer les éditions des fichiers ouverts"), SVGPathIcons.TRASH, null,
			TR.tr("Supprime les éditions de tous les fichiers ouverts dans le panneau des fichiers"));

	Menu tools5SameNameEditions = createSubMenu(TR.tr("Éditions des documents du même nom"), SVGPathIcons.EXCHANGE,
			TR.tr("Déplace l'édition de ce document sur un autre document qui porte le même nom. Cette fonction peut être utilisée lorsqu'un fichier PDF a été déplacé. En effet, si un document PDF est déplacé dans un autre dossier, PDF4Teachers n'arrivera plus à récupérer son édition, sauf avec cette fonction"), true);
	MenuItem tools5SameNameEditionsNull = new MenuItem(TR.tr("Aucune édition trouvée"));

	Menu tools6ExportEdition = createSubMenu(TR.tr("Exporter l'édition/barème"), SVGPathIcons.EXPORT,
			TR.tr("Générer un fichier qui peut être enregistré sur votre ordinateur à partir de l'édition de ce document"), true);

		MenuItem tools6ExportEdition1All = createMenuItem(TR.tr("Exporter l'édition"), null, null,
				TR.tr("Génère un fichier contenant l'édition du document"), true, false, false, false);
		MenuItem tools6ExportEdition2Grades = createMenuItem(TR.tr("Exporter le barème"), null, null,
				TR.tr("Remplace le barème du document ouvert par celui d'un fichier de barème"), true, false, false, false);

	Menu tools7ImportEdition = createSubMenu(TR.tr("Importer une édition/barème"), SVGPathIcons.IMPORT,
			TR.tr("Remplace l'édition du document ouvert par celle d'un fichier d'édition"), true);

		MenuItem tools7ImportEdition1All = createMenuItem(TR.tr("Importer une édition"), null, null,
				TR.tr("Remplace l'édition du document ouvert par celle d'un fichier d'édition"), true, false, false, false);
		MenuItem tools7ImportEdition2Grades = createMenuItem(TR.tr("Importer un barème"), null, null,
				TR.tr("Remplace le barème du document ouvert par celle d'un fichier de barème"), true, false, false, false);

	MenuItem tools8FullScreen = createMenuItem(TR.tr("Mode plein écran"), SVGPathIcons.FULL_SCREEN, null,
			TR.tr("Passe l'application en mode plein écran"));

	Menu tools9Debug = createSubMenu(TR.tr("Débug"), SVGPathIcons.COMMAND_PROMPT,
			TR.tr("Options plus complexes qui vous demandent une certaine connaissance en informatique."), false);

		MenuItem tools9Debug1OpenConsole = createMenuItem(TR.tr("Ouvrir la console d'exécution") + " (" + (Main.COPY_CONSOLE ? "Activée" : "Désactivée") + ")", null, new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHORTCUT_DOWN),
				TR.tr("Ouvre la console de l'application"), false, false, false, false);
		MenuItem tools9Debug2OpenAppFolder = createMenuItem(TR.tr("Ouvrir le dossier de données"), null, null,
				TR.tr("Ouvre le dossier où PDF4Teachers enregistre toutes ses données"), false, false, false, false);
		MenuItem tools9Debug3OpenEditionFile = createMenuItem(TR.tr("Ouvrir le fichier d'édition"), null, null,
				TR.tr("Ouvre le fichier qui contient les données de l'édition actuelle"), true, false, false, false);

	////////// SETTINGS //////////

	public Menu settings = new Menu(TR.tr("Préférences"));


	////////// ABOUT / HELP //////////

	public Menu about = new Menu();

	Menu help = new Menu(TR.tr("Aide"));
	MenuItem help1LoadDoc = new MenuItem(TR.tr("Charger la documentation"));
	MenuItem help2GitHubIssue = new MenuItem(TR.tr("Demander de l'aide ou signaler un Bug sur GitHub"));
	MenuItem help3Twitter = new MenuItem(TR.tr("Nous contacter sur Twitter"));
	MenuItem help4Website = new MenuItem(TR.tr("Site Web de PDF4Teachers"));

	public MenuBar(){
		setup();
	}
	public static boolean isSystemMenuBarSupported(){
		return Main.isOSX();
	}
	public void setup(){
		if(isSystemMenuBarSupported()) setUseSystemMenuBar(true);

		////////// FILE //////////

		file.getItems().addAll(file1Open, file2OpenDir, file3Clear, new SeparatorMenuItem(), file4Save, file5Delete, file6Close, new SeparatorMenuItem(), file7Export, file8ExportAll);

		////////// TOOLS //////////

		tools3AddPages.getItems().add(new MenuItem());
		tools6ExportEdition.getItems().addAll(tools6ExportEdition1All, tools6ExportEdition2Grades);
		tools7ImportEdition.getItems().addAll(tools7ImportEdition1All, tools7ImportEdition2Grades);
		tools5SameNameEditions.getItems().add(tools5SameNameEditionsNull);
		tools9Debug.getItems().addAll(tools9Debug1OpenConsole, tools9Debug2OpenAppFolder, tools9Debug3OpenEditionFile);

		tools.getItems().addAll(tools1Convert, /*tools2QRCode,*/ tools3AddPages, new SeparatorMenuItem(), tools4DeleteAllEdits, tools5SameNameEditions, tools6ExportEdition, tools7ImportEdition, new SeparatorMenuItem(), tools8FullScreen, new SeparatorMenuItem(), tools9Debug);

		////////// SETTINGS //////////

		Settings s = Main.settings;
		for(Field field : s.getClass().getDeclaredFields()){
			if(field.isAnnotationPresent(SettingObject.class)){
				try{
					((Setting) field.get(s)).setupMenuItem();
				}catch(Exception e){ e.printStackTrace(); }
			}
		}
		settings.getItems().addAll(
				s.language.getMenuItem(), s.checkUpdates.getMenuItem(), s.sendStats.getMenuItem(),
				new SeparatorMenuItem(), s.restoreLastSession.getMenuItem(), s.defaultZoom.getMenuItem(), s.zoomAnimations.getMenuItem(), s.darkTheme.getMenuItem(),
				new SeparatorMenuItem(), s.autoSave.getMenuItem(), s.regularSave.getMenuItem(),
				new SeparatorMenuItem(), s.textAutoRemove.getMenuItem(), s.textOnlyStart.getMenuItem(), s.textSmall.getMenuItem(),
				new SeparatorMenuItem(), s.allowAutoTips.getMenuItem());

		////////// HELP //////////

		if(!isSystemMenuBarSupported()){
			help1LoadDoc.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.INFO, "white", 0, 15, 15, colorAdjust));
			help2GitHubIssue.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.GITHUB, "white", 0, 15, 15, colorAdjust));
			help3Twitter.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TWITTER, "white", 0, 15, 15, colorAdjust));
			help4Website.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.GLOBE, "white", 0, 15, 15, colorAdjust));
		}
		help.getItems().addAll(help1LoadDoc, help2GitHubIssue, help3Twitter, help4Website);

		////////// SETUP ITEMS WIDTH ///////////

		NodeMenuItem.setupMenu(file);
		NodeMenuItem.setupMenu(tools);
		NodeMenuItem.setupMenu(settings);

		////////// FILE //////////

		file1Open.setOnAction((ActionEvent actionEvent) -> {

			File[] files = DialogBuilder.showFilesDialog(true);
			if(files != null){
				MainWindow.filesTab.openFiles(files);
				if(files.length == 1){
					MainWindow.mainScreen.openFile(files[0]);
				}
			}
		});
		file2OpenDir.setOnAction((ActionEvent actionEvent) -> {

			File directory = DialogBuilder.showDirectoryDialog(true);
			if(directory != null) {
				MainWindow.filesTab.openFiles(new File[]{directory});
			}
		});
		file3Clear.setOnAction((ActionEvent actionEvent) -> {
			MainWindow.filesTab.clearFiles();
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
			new ExportWindow(MainWindow.filesTab.files.getItems());

		});

		////////// TOOLS //////////

		tools1Convert.setOnAction(e -> {
			new ConvertDocument();
		});

		tools2QRCode.setOnAction(e -> {

		});

		tools3AddPages.setOnShowing(e -> {
			tools3AddPages.getItems().setAll(PageEditPane.getNewPageMenu(0, MainWindow.mainScreen.document.totalPages, isSystemMenuBarSupported()));
			PaneUtils.setMenuSize(tools3AddPages);
		});

		tools4DeleteAllEdits.setOnAction((ActionEvent e) -> {
			Alert dialog = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("Supprimer les éditions"));
			dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir supprimer toutes les éditions des fichiers de la liste ?"));


			float yesButSize = FilesUtils.convertOctetToMo(FilesUtils.getSize(new File(Main.dataFolder + "editions")));
			float yesSize = 0L;
			for(File file : MainWindow.filesTab.files.getItems()){
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
				for(File file : MainWindow.filesTab.files.getItems()){
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

			Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Supression terminée"));
			alert.setHeaderText(TR.tr("Vos éditions ont bien été supprimés."));
			alert.setContentText(TR.tr("Vous avez supprimé") + " " + size + "Mo");
			alert.show();
		});
		tools5SameNameEditions.setOnShowing((Event event) -> {
			tools5SameNameEditions.getItems().clear();
			int i = 0;
			for(Map.Entry<File, File> files : Edition.getEditFilesWithSameName(MainWindow.mainScreen.document.getFile()).entrySet()){

				MenuItem item = new MenuItem(files.getValue().getAbsolutePath());
				if(files.getValue().getParentFile() != null){
					item.setText(files.getValue().getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + File.separator);
				}


				tools5SameNameEditions.getItems().add(item);
				item.setOnAction((ActionEvent actionEvent) -> {
					Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Charger une autre édition"));
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

							for(File otherFileDest : MainWindow.filesTab.files.getItems()){
								if(otherFileDest.getParentFile().getAbsolutePath().equals(MainWindow.mainScreen.document.getFile().getParentFile().getAbsolutePath()) && !otherFileDest.equals(MainWindow.mainScreen.document.getFile())){
									File fromEditFile = Edition.getEditFile(new File(files.getValue().getParentFile().getAbsolutePath() + "/" + otherFileDest.getName()));

									if(fromEditFile.exists()){
										Edition.mergeEditFileWithEditFile(fromEditFile, Edition.getEditFile(otherFileDest));
									}else{
										Alert alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.tr("Fichier introuvable"));
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
			PaneUtils.setMenuSize(tools5SameNameEditions);
		});

		tools6ExportEdition1All.setOnAction((e) -> EditionExporter.showExportDialog(false));
		tools6ExportEdition2Grades.setOnAction((e) -> EditionExporter.showExportDialog(true));
		tools7ImportEdition1All.setOnAction((e) -> EditionExporter.showImportDialog(false));
		tools7ImportEdition2Grades.setOnAction((e) -> EditionExporter.showImportDialog(true));

		tools8FullScreen.setOnAction((e) -> {
			Main.window.setFullScreen(!Main.window.isFullScreen());
		});

		tools9Debug1OpenConsole.setOnAction((e) -> new LogWindow());
		tools9Debug2OpenAppFolder.setOnAction((e) -> PlatformUtils.openDirectory(Main.dataFolder));
		tools9Debug3OpenEditionFile.setOnAction((e) -> PlatformUtils.openFile(Edition.getEditFile(MainWindow.mainScreen.document.getFile()).getAbsolutePath()));


		////////// SETTINGS //////////

		s.language.getMenuItem().setOnAction(e -> {
			new LanguageWindow(value -> {
				if(!value.isEmpty()){
					Main.settings.language.setValue(value);
					Main.window.restart();
				}
			});
		});
		s.defaultZoom.getMenuItem().setOnAction((ActionEvent actionEvent) -> {


			List<Integer> choices = new ArrayList<>(Arrays.asList(50, 70, 80, 90, 100, 110, 120, 140, 160, 180, 200, 230, 250, 280, 300));
			ChoiceDialog<Integer> dialog = DialogBuilder.getChoiceDialog(Main.settings.defaultZoom.getValue(), choices);

			dialog.setTitle(TR.tr("Zoom par défaut"));
			dialog.setHeaderText(TR.tr("Zoom par défaut lors de l'ouverture d'un document"));
			dialog.setContentText(TR.tr("Choisir un pourcentage :"));

			Optional<Integer> newZoom = dialog.showAndWait();
			if(!newZoom.isEmpty()){
				Main.settings.defaultZoom.setValue(newZoom.get());
			}

		});
		s.regularSave.getMenuItem().setOnAction((ActionEvent actionEvent) -> {

			Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Sauvegarde régulière"));

			HBox pane = new HBox();
			ComboBox<Integer> combo = new ComboBox<>(FXCollections.observableArrayList(1, 5, 10, 15, 20, 30, 45, 60));
			combo.getSelectionModel().select(Main.settings.regularSave.getValue() == -1 ? (Integer) 5 : Main.settings.regularSave.getValue());
			combo.setStyle("-fx-padding-left: 20px;");
			CheckBox activated = new CheckBox(TR.tr("Activer"));
			activated.setSelected(Main.settings.regularSave.getValue() != -1);
			pane.getChildren().add(0, activated);
			pane.getChildren().add(1, combo);
			HBox.setMargin(activated, new Insets(5, 0, 0, 10));
			HBox.setMargin(combo, new Insets(0, 0, 0, 30));

			combo.disableProperty().bind(activated.selectedProperty().not());
			dialog.setHeaderText(TR.tr("Définir le nombre de minutes entre deux sauvegardes automatiques."));
			dialog.getDialogPane().setContent(pane);

			//ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
			//ButtonType ok = new ButtonType(TR.tr("OK"), ButtonBar.ButtonData.OK_DONE);
			//dialog.getButtonTypes().setAll(cancel, ok);

			Optional<ButtonType> option = dialog.showAndWait();
			if(option.get() == ButtonType.OK){
				s.regularSave.setRadioSelected(activated.isSelected());
				if(activated.isSelected()) s.regularSave.setValue(combo.getSelectionModel().getSelectedItem());
				else s.regularSave.setValue(-1);
			}
		});

		////////// ABOUT / HELP //////////

		if(isSystemMenuBarSupported()){
			about.setText(TR.tr("À propos"));
			MenuItem triggerItem = new MenuItem(TR.tr("Fenêtre à Propos"));
			about.getItems().add(triggerItem);
			triggerItem.setOnAction((event) -> {
				try{ FXMLLoader.load(getClass().getResource("/fxml/AboutWindow.fxml")); }catch(IOException e){ e.printStackTrace(); }
			});
		}else{
			Label name = new Label(TR.tr("À propos"));
			name.setAlignment(Pos.CENTER_LEFT);
			name.setOnMouseClicked(event -> {
				try{ FXMLLoader.load(getClass().getResource("/fxml/AboutWindow.fxml")); }catch(IOException e){ e.printStackTrace(); }
			});
			about.setGraphic(name);
		}

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
		getMenus().addAll(file, tools, settings, help, about);

		if(!isSystemMenuBarSupported()){
			for(Menu menu : getMenus()){
				menu.setStyle("-fx-padding: 5 7 5 7;");
			}
		}

	}
	public static Menu createSubMenu(String name, String image, String toolTip, boolean disableIfNoDoc){

		if(isSystemMenuBarSupported()){
			Menu menu = new Menu(name);
			//if(imgName != null)menu.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/" + imgName + ".png")+"", 0, 0));

			if(disableIfNoDoc){
				menu.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
			}

			return menu;
		}else{
			Menu menu = new Menu();
			HBox pane = new HBox();

			Label text = new Label(name);

			if(image != null){
				if(image.length() >= 30){
					pane.getChildren().add(SVGPathIcons.generateImage(image, "white", 0, 16, 16, colorAdjust));
				}else{
					if(MenuBar.class.getResource("/img/MenuBar/"+ image + ".png") == null) System.err.println("MenuBar image " + image + " does not exist");
					else pane.getChildren().add(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/"+ image + ".png")+"", 0, 0, colorAdjust));
				}

				text.setStyle("-fx-font-size: 13; -fx-padding: 0 0 0 10;"); // top - right - bottom - left
			}else{
				text.setStyle("-fx-font-size: 13; -fx-padding: 2 0 2 0;");
			}
			pane.getChildren().add(text);

			if(disableIfNoDoc){
				menu.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
			}

			Tooltip toolTipUI = PaneUtils.genToolTip(toolTip);
			toolTipUI.setShowDuration(Duration.INDEFINITE);
			Tooltip.install(pane, toolTipUI);

			menu.setGraphic(pane);
			return menu;
		}
	}
	public static MenuItem createRadioMenuItem(String text, String image, String toolTip, boolean autoUpdate){

		if(isSystemMenuBarSupported()){
			RadioMenuItem menuItem = new RadioMenuItem(text);
			//if(imgName != null) menuItem.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));

			//OSX selects radioMenuItems upon click, but doesn't unselect it on click :
			AtomicBoolean selected = new AtomicBoolean(false);
			menuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
				Platform.runLater(() -> {
					selected.set(newValue);
				});
			});
			menuItem.setOnAction((e) -> {
				menuItem.setSelected(!selected.get());
			});

			return menuItem;

		}else{
			NodeRadioMenuItem menuItem = new NodeRadioMenuItem(new HBox(), text + "      ", true, autoUpdate);


			if(image != null){
				if(image.length() >= 30){
					menuItem.setImage(SVGPathIcons.generateImage(image, "white", 0, 16, 16, colorAdjust));
				}else{
					if(MenuBar.class.getResource("/img/MenuBar/"+ image + ".png") == null) System.err.println("MenuBar image " + image + " does not exist");
					else menuItem.setImage(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/"+ image + ".png")+"", 0, 0, colorAdjust));
				}

			}
			if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);

			return menuItem;
		}


	}
	public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, boolean leftMargin){
		return createMenuItem(text, imgName, keyCombinaison, toolTip, disableIfNoDoc, disableIfNoList, leftMargin, true);
	}
	public static MenuItem createMenuItem(String text, String image, KeyCombination keyCombinaison, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, boolean leftMargin, boolean fat){
		if(isSystemMenuBarSupported()){
			MenuItem menuItem = new MenuItem(text);
			//if(imgName != null) menuItem.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));
			if(keyCombinaison != null) menuItem.setAccelerator(keyCombinaison);
			if(disableIfNoDoc){
				menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
			}if(disableIfNoList){
				menuItem.disableProperty().bind(Bindings.size(MainWindow.filesTab.getOpenedFiles()).isEqualTo(0));
			}
			return menuItem;
		}else{
			NodeMenuItem menuItem = new NodeMenuItem(new HBox(), text + "         ", fat);

			if(image != null){
				if(image.length() >= 30){
					menuItem.setImage(SVGPathIcons.generateImage(image, "white", 0, 16, 16, colorAdjust));
				}else{
					if(MenuBar.class.getResource("/img/MenuBar/"+ image + ".png") == null) System.err.println("MenuBar image " + image + " does not exist");
					else menuItem.setImage(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/"+ image + ".png")+"", 0, 0, colorAdjust));
				}

			}
			if(keyCombinaison != null) menuItem.setKeyCombinaison(keyCombinaison);
			if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);
			if(leftMargin) menuItem.setFalseLeftData();

			if(disableIfNoDoc){
				menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
			}if(disableIfNoList){
				menuItem.disableProperty().bind(Bindings.size(MainWindow.filesTab.getOpenedFiles()).isEqualTo(0));
			}

			return menuItem;
		}
	}
	public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip){
		return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, false);
	}
	public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean leftMargin){
		return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, leftMargin);
	}
}