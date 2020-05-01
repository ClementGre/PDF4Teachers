package fr.themsou.panel;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.UserData;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.windows.AboutWindow;
import fr.themsou.main.Main;
import fr.themsou.utils.*;
import fr.themsou.windows.LanguageWindow;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
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
	NodeMenuItem fichier1Open = createMenuItem(TR.tr("Ouvrir un·des fichiers"), "ouvrir", "Ctrl+O",
			TR.tr("Ajoute un ou plusieurs fichiers dans le panneau des fichiers."));

	NodeMenuItem fichier2OpenDir = createMenuItem(TR.tr("Ouvrir un dossier"), "directory", "Ctrl+Shift+O",
			TR.tr("Ajoute tous les fichiers PDF d'un dossier dans le panneau des fichiers"));

	NodeMenuItem fichier3Clear = createMenuItem(TR.tr("Vider la liste"), "vider", "Ctrl+Shift+W",
			TR.tr("Vide la liste des fichiers"), false, true, 0);

	NodeMenuItem fichier4Save = createMenuItem(TR.tr("Sauvegarder l'édition"), "sauvegarder", "Ctrl+S",
			TR.tr("Sauvegarde les éléments d'édition du document courant. Le fichier PDF pré-existant ne sera pas modifié"), true, false, 0);

	NodeMenuItem fichier5Delete = createMenuItem(TR.tr("Supprimer l'édition"), "supprimer", "",
			TR.tr("Supprime les éléments d'édition du document courant"), true, false, 0);

	NodeMenuItem fichier6DeleteAll = createMenuItem(TR.tr("Supprimer les éditions des fichiers ouverts"), "supprimer", "",
			TR.tr("Supprime les éditions de tous les fichiers ouverts dans le panneau des fichiers"));

	NodeMenuItem fichier7Close = createMenuItem(TR.tr("Fermer le document"), "fermer", "Ctrl+W",
			TR.tr("Ferme la vue du document courant"), true, false, 0);

	Menu fichier8SameName = createSubMenu(TR.tr("Éditions des documents du même nom"), "memenom",
			TR.tr("Déplace l'édition de ce document sur un autre document qui porte le même nom. Cette fonction peut être utilisée lorsqu'un fichier PDF a été déplacé. En effet, si un document PDF est déplacé dans un autre dossier, PDF4Teachers n'arrivera plus à récupérer son édition, sauf avec cette fonction"), true);

	MenuItem fichier8SameNameNull = new MenuItem(TR.tr("Aucune édition trouvée"));

	NodeMenuItem fichier9Export = createMenuItem(TR.tr("Exporter (Regénérer le PDF)"), "exporter", "Ctrl+E",
			TR.tr("Crée un nouveau fichier PDF à partir du document ouvert, avec tous les éléments ajoutés"), true, false, 0);

	NodeMenuItem fichier10ExportAll = createMenuItem(TR.tr("Tout exporter"), "exporter", "Ctrl+Shift+E",
			TR.tr("Crée des nouveaux fichiers PDF à partir chacun des fichiers de la liste des fichiers, avec pour chaque fichier, tous les éléments de son édition"), false, true, 0);

	////////// PREFS //////////

	Menu preferences = new Menu(TR.tr("Préférences"));

	NodeMenuItem preferences1Language = createMenuItem(TR.tr("Langage") + " (" + Main.settings.getLanguage() + ")", "language", "",
			TR.tr("Définit la langue de l'interface"), 30);

	NodeRadioMenuItem preferences2Restore = createRadioMenuItem(TR.tr("Toujours restaurer la session précédente"), "recharger",
			TR.tr("Réouvre les derniers fichiers ouverts lors de l'ouverture de l'application."), true);

	NodeRadioMenuItem preferences3Update = createRadioMenuItem(TR.tr("Alerter quand une nouvelle version est disponible"), "wifi",
			TR.tr("Fait apparaître une fenêtre à chaque démarrage si une nouvelle version est disponible. Même si cette option est désactivée, l'application vérifira si une nouvelle version est disponible et affichera le menu À propos en couleur"), true);

	NodeMenuItem preferences4Zoom = createMenuItem(TR.tr("Zoom lors de l'ouverture d'un document"), "zoom", "",
			TR.tr("Définit le zoom par défaut lors de l'ouverture d'un document. Le zoom est aussi contrôlé avec Ctrl+Molette ou pincement sur trackpad"), 30);

	NodeRadioMenuItem preferences5Animation = createRadioMenuItem(TR.tr("Animations de zoom ou défilement"), "cloud",
			TR.tr("Permet des transitions fluides lors d'un zoom ou d'un défilement de la page. Il est possible de désactiver cette option si l'ordinateur est lent lors du zoom. Cette option est déconseillée aux utilisateurs de TrackPad"), true);



	NodeRadioMenuItem preferences6Save = createRadioMenuItem(TR.tr("Sauvegarder automatiquement"), "sauvegarder",
			TR.tr("Sauvegarde l'édition du document automatiquement lors de la fermeture du document ou de l'application."), true);

	NodeRadioMenuItem preferences7Regular = createRadioMenuItem(TR.tr("Sauvegarder régulièrement"), "sauvegarder-recharger",
			TR.tr("Sauvegarde l'édition du document automatiquement toutes les x minutes."), false);



	NodeRadioMenuItem preferences8RemoveWhenAdd = createRadioMenuItem(TR.tr("Supprimer l'élément des éléments précédents\nlorsqu'il est ajouté aux favoris"), "favoris",
			TR.tr("Dans la liste des derniers éléments textuels utilisés, retire automatiquement l'élément lorsqu'il est ajouté aux favoris."), true);

	NodeRadioMenuItem preferences9ShowStart = createRadioMenuItem(TR.tr("N'afficher que le début des éléments textuels"), "lines",
			TR.tr("Dans les liste des éléments textuels, n'affiche que les deux premières lignes de l'élément."), true);

	NodeRadioMenuItem preferences10SmallFont = createRadioMenuItem(TR.tr("Réduire la taille des éléments dans les listes"), "cursor",
			TR.tr("Dans les liste des éléments textuels, affiche les éléments en plus petit."), true);


	////////// OTHER //////////

	public Menu apropos = new Menu();
	Menu aide = new Menu(TR.tr("Aide"));
	MenuItem aide1Doc = new MenuItem(TR.tr("Charger le document d'aide") + "     ");
	MenuItem aide2Probleme = new MenuItem(TR.tr("Demander de l'aide ou signaler un Bug") + "     ");

	public MenuBar(){
		setup();
	}

	public void setup(){

		////////// FICHIER //////////

		fichier8SameName.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
		fichier8SameName.getItems().add(fichier8SameNameNull);

		fichier.getItems().addAll(fichier1Open, fichier2OpenDir, fichier3Clear, new SeparatorMenuItem(), fichier4Save, fichier5Delete, fichier6DeleteAll, fichier7Close, fichier8SameName, new SeparatorMenuItem(), fichier9Export, fichier10ExportAll);

		////////// PREFS //////////

		// DEFINE DEFAULT STATE
		preferences2Restore.selectedProperty().set(Main.settings.isRestoreLastSession());
		preferences3Update.selectedProperty().set(Main.settings.isCheckUpdates());
		preferences5Animation.selectedProperty().set(Main.settings.isZoomAnimations());

		preferences6Save.selectedProperty().set(Main.settings.isAutoSave());
		preferences7Regular.selectedProperty().set(Main.settings.getRegularSaving() != -1);

		preferences8RemoveWhenAdd.selectedProperty().set(Main.settings.isRemoveElementInPreviousListWhenAddingToFavorites());
		preferences9ShowStart.selectedProperty().set(Main.settings.isShowOnlyStartInTextsList());
		preferences10SmallFont.selectedProperty().set(Main.settings.isSmallFontInTextsList());

		// BIND
		Main.settings.restoreLastSessionProperty().bind(preferences2Restore.selectedProperty());
		Main.settings.checkUpdatesProperty().bind(preferences3Update.selectedProperty());
		Main.settings.zoomAnimationsProperty().bind(preferences5Animation.selectedProperty());

		Main.settings.autoSavingProperty().bind(preferences6Save.selectedProperty());

		Main.settings.removeElementInPreviousListWhenAddingToFavoritesProperty().bind(preferences8RemoveWhenAdd.selectedProperty());
		Main.settings.showOnlyStartInTextsListProperty().bind(preferences9ShowStart.selectedProperty());
		Main.settings.smallFontInTextsListProperty().bind(preferences10SmallFont.selectedProperty());

		// ADD
		preferences.getItems().addAll(preferences1Language, preferences2Restore, preferences3Update, preferences4Zoom, preferences5Animation,
				new SeparatorMenuItem(), preferences6Save, preferences7Regular,
				new SeparatorMenuItem(), preferences8RemoveWhenAdd, preferences9ShowStart, preferences10SmallFont);

		////////// OTHER //////////

		aide1Doc.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/info.png")+"", 0, 0));
		aide2Probleme.setGraphic(Builders.buildImage(getClass().getResource("/img/MenuBar/partager.png")+"", 0, 0));
		aide.getItems().addAll(aide1Doc, aide2Probleme);

		/////////////////////////////

		////////// FICHIER //////////

		fichier1Open.setOnAction((ActionEvent actionEvent) -> {

			final FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(TR.tr("Fichier PDF"), "*.pdf"));
			chooser.setTitle(TR.tr("Selectionner un ou plusieurs fichier"));
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
			chooser.setTitle(TR.tr("Selectionner un dossier"));
			chooser.setInitialDirectory((UserData.lastOpenDir.exists() ? UserData.lastOpenDir : new File(System.getProperty("user.home"))));

			File file = chooser.showDialog(Main.window);
			if(file != null) {
				MainWindow.lbFilesTab.openFiles(new File[]{file});
				UserData.lastOpenDir = file.getParentFile();
			}
		});
		fichier3Clear.setOnAction((ActionEvent actionEvent) -> {
			MainWindow.lbFilesTab.clearFiles(true);
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
			Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
			new JMetro(dialog.getDialogPane(), Style.LIGHT);
			Builders.secureAlert(dialog);
			dialog.setTitle(TR.tr("Supprimer les éditions"));
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

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			new JMetro(alert.getDialogPane(), Style.LIGHT);
			Builders.secureAlert(alert);
			alert.setTitle(TR.tr("Supression terminée"));
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
			for(File file : Edition.getEditFilesWithSameName(MainWindow.mainScreen.document.getFile())) {

				MenuItem item = new MenuItem(file.getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + File.separator);
				fichier8SameName.getItems().add(item);
				item.setOnAction((ActionEvent actionEvent) -> {
					Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
					new JMetro(dialog.getDialogPane(), Style.LIGHT);
					Builders.secureAlert(dialog);
					dialog.setTitle(TR.tr("Charger une autre édition"));
					dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer l'édition courante par celle-ci ?"));

					ButtonType cancel = new ButtonType(TR.tr("Non"), ButtonBar.ButtonData.CANCEL_CLOSE);
					ButtonType yes = new ButtonType(TR.tr("Oui"), ButtonBar.ButtonData.OK_DONE);
					ButtonType yesAll = new ButtonType(TR.tr("Oui, répéter cette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
					dialog.getButtonTypes().setAll(cancel, yes, yesAll);

					Optional<ButtonType> option = dialog.showAndWait();
					if(option.get() == yes){
						if(MainWindow.mainScreen.hasDocument(true)){
							MainWindow.mainScreen.document.edition.clearEdit(false);
							Edition.mergeEditFileWithDocFile(file, MainWindow.mainScreen.document.getFile());
							MainWindow.mainScreen.document.loadEdition();
						}
					}else if(option.get() == yesAll){
						if(MainWindow.mainScreen.hasDocument(true)){
							MainWindow.mainScreen.document.edition.clearEdit(false);
							Edition.mergeEditFileWithDocFile(file, MainWindow.mainScreen.document.getFile());
							MainWindow.mainScreen.document.loadEdition();

							for(File otherFileDest : MainWindow.lbFilesTab.files.getItems()){
								if(otherFileDest.getParentFile().getAbsolutePath().equals(MainWindow.mainScreen.document.getFile().getParentFile().getAbsolutePath()) && !otherFileDest.equals(MainWindow.mainScreen.document.getFile())){
									File fromEditFile = Edition.getEditFile(new File(file.getParentFile().getAbsolutePath() + "/" + otherFileDest.getName()));
									if(fromEditFile.exists()){
										Edition.mergeEditFileWithEditFile(fromEditFile, Edition.getEditFile(otherFileDest));
									}else{
										Alert alert = new Alert(Alert.AlertType.ERROR);
										new JMetro(alert.getDialogPane(), Style.LIGHT);
										alert.setTitle(TR.tr("Fichier introuvable"));
										alert.setHeaderText(TR.tr("Le fichier") + " \"" + otherFileDest.getName() + "\" " + TR.tr("dans") + " \"" + file.getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + "\" " + TR.tr("n'a pas d'édition."));
										ButtonType ok = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.OK_DONE);
										ButtonType cancelAll = new ButtonType(TR.tr("Tout Arreter"), ButtonBar.ButtonData.CANCEL_CLOSE);
										alert.getButtonTypes().setAll(ok, cancelAll);
										Builders.secureAlert(alert);
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
			new JMetro(dialog.getDialogPane(), Style.LIGHT);
			Builders.secureAlert(dialog);
			dialog.setTitle(TR.tr("Zoom par défaut"));
			dialog.setHeaderText(TR.tr("Zoom par défaut lors de l'ouverture d'un document"));
			dialog.setContentText(TR.tr("Choisir un pourcentage :"));

			Optional<Integer> newZoom = dialog.showAndWait();
			if(!newZoom.isEmpty()){
				Main.settings.setDefaultZoom(newZoom.get());
			}

		});
		preferences7Regular.setOnAction((ActionEvent actionEvent) -> {

			Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);

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

			new JMetro(dialog.getDialogPane(), Style.LIGHT);
			Builders.secureAlert(dialog);

			dialog.setTitle(TR.tr("Sauvegarde régulière"));
			dialog.setHeaderText(TR.tr("Définir le nombre de minutes entre deux sauvegardes automatiques."));

			dialog.getDialogPane().setContent(pane);

			ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
			ButtonType ok = new ButtonType(TR.tr("OK"), ButtonBar.ButtonData.OK_DONE);
			dialog.getButtonTypes().setAll(cancel, ok);

			Optional<ButtonType> option = dialog.showAndWait();
			if(option.get() == ok){
				if(activated.isSelected()){
					Main.settings.setRegularSaving(combo.getSelectionModel().getSelectedItem());
					preferences7Regular.setSelected(true);
				}else{
					Main.settings.setRegularSaving(-1);
					preferences7Regular.setSelected(false);
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

		Label name = new Label(TR.tr("À propos"));
		name.setAlignment(Pos.CENTER_LEFT);
		name.setOnMouseClicked(event -> new AboutWindow());
		apropos.setGraphic(name);

		// UI Style
		setStyle("-fx-background-color: #2B2B2B;");
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


		ImageView icon = Builders.buildImage(getClass().getResource("/img/MenuBar/" + imgName + ".png")+"", 0, 0);

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

		NodeRadioMenuItem menuItem = new NodeRadioMenuItem(new HBox(), text, 350, true, autoUpdate);

		if(imgName != null) menuItem.setImage(Builders.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));
		if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);

		return menuItem;

	}
	public NodeMenuItem createMenuItem(String text, String imgName, String accelerator, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, double leftMargin){

		if(System.getProperty("os.name").toLowerCase().contains("mac")){
			accelerator = accelerator.replaceAll(Pattern.quote("Ctrl"), "Meta");
		}

		NodeMenuItem menuItem = new NodeMenuItem(new HBox(), text, 400, true);

		if(!imgName.isBlank()) menuItem.setImage(Builders.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));
		if(!accelerator.isBlank()) menuItem.setAccelerator(accelerator);
		if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);
		if(leftMargin != 0) menuItem.setFalseLeftData(leftMargin);

		if(disableIfNoDoc){
			menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
		}if(disableIfNoList){
			menuItem.disableProperty().bind(Bindings.size(MainWindow.lbFilesTab.files.getItems()).isEqualTo(0));
		}

		return menuItem;

	}
	public NodeMenuItem createMenuItem(String text, String imgName, String accelerator, String toolTip){
		return createMenuItem(text, imgName, accelerator, toolTip, false, false, 0);
	}
	public NodeMenuItem createMenuItem(String text, String imgName, String accelerator, String toolTip, double leftMargin){
		return createMenuItem(text, imgName, accelerator, toolTip, false, false, leftMargin);
	}
}
