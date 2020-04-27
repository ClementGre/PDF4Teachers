package fr.themsou.main;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;

import fr.themsou.panel.FooterBar;
import fr.themsou.panel.leftBar.files.LBFilesTab;
import fr.themsou.panel.leftBar.notes.LBNoteTab;
import fr.themsou.panel.leftBar.paint.LBPaintTab;
import fr.themsou.panel.leftBar.texts.LBTextTab;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.MenuBar;
import fr.themsou.utils.TR;
import fr.themsou.windows.LicenseWindow;
import fr.themsou.windows.UpdateWindow;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main extends Application {

	public static Stage window;
	public static Settings settings;
	public static UserData userData;

	public static boolean hasToClose = false;

	public static DecimalFormat format = new DecimalFormat("0.#");
	
//		MAIN

	public static SplitPane mainPane = new SplitPane();
	public static MainScreen mainScreen;
	
//		LEFT BAR
	
	public static TabPane leftBar = new TabPane();
	public static LBFilesTab lbFilesTab;
	public static LBTextTab lbTextTab;
	public static LBNoteTab lbNoteTab;
	public static LBPaintTab lbPaintTab;

//		FOOTER-HEADER BAR
	
	public static FooterBar footerBar;
	public static MenuBar menuBar;

	public static HostServices hostServices;

	public static String dataFolder = System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator;
	public static final String VERSION_CODE = "010100";
	public static final String VERSION = "Snapshot 1.1.0";

	Thread userDataSaver = new Thread(new Runnable() {
		@Override public void run() {

			while(true){
				try{
					Thread.sleep(300000);
				}catch(InterruptedException e){ e.printStackTrace(); }

				userData.saveData();
			}
		}
	}, "userData AutoSaver");

	public static void main(String[] args){
		launch(args);

	}
	@Override
	public void start(Stage window) throws Exception {
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			dataFolder = System.getenv("APPDATA") + File.separator + "PDF4Teachers" + File.separator;
		}
		boolean firstLaunch = !new File(dataFolder + File.separator + "settings.yml").exists();

		hostServices = getHostServices();

		Main.window = window;
		BorderPane root = new BorderPane();

		Scene scene = new Scene(root, 1200, 675);

		window.setMinWidth(700);
		window.setMinHeight(393);
		window.setTitle(TR.tr("PDF4Teachers - Aucun document"));
		window.getIcons().add(new Image(getClass().getResource("/logo.png")+""));
		window.setScene(scene);
		window.setResizable(true);
		window.setOnCloseRequest(e -> {
			hasToClose = true;
			if (!mainScreen.closeFile(!settings.isAutoSave())) {
				userData.saveData();
				e.consume();
				hasToClose = false;
				return;
			}
			userData.saveData();
			System.exit(0);
		});

//		SETUPS

		settings = new Settings();

		mainScreen = new MainScreen();
		footerBar = new FooterBar();

		lbFilesTab = new LBFilesTab();
		lbTextTab = new LBTextTab();
		lbNoteTab = new LBNoteTab();
		lbPaintTab = new LBPaintTab();

		menuBar = new MenuBar();

		mainScreen.repaint();
		footerBar.repaint();

//		PANELS

		window.show();

		mainPane.getItems().addAll(leftBar, mainScreen);
		mainPane.setDividerPositions(270 / root.getWidth());
		mainPane.getDividers().get(0).positionProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			double width = newValue.doubleValue() * root.getWidth();
			if(width >= 400){
				mainPane.setDividerPositions(400 / root.getWidth());
			}
		});

		root.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			double width = mainPane.getDividerPositions()[0] * oldValue.doubleValue();
			mainPane.setDividerPositions(width / newValue.doubleValue());
		});

		root.setCenter(mainPane);
		root.setTop(menuBar);
		root.setBottom(footerBar);

		root.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.TAB){
				if(leftBar.getSelectionModel().getSelectedIndex() == 1) leftBar.getSelectionModel().select(2);
				else leftBar.getSelectionModel().select(1);
				e.consume();
			}
		});

//		THEME

		new JMetro(root, Style.LIGHT);
		new JMetro(menuBar, Style.DARK);

//		SHOWING

		userDataSaver.start();
		mainScreen.repaint();

		File doc = new File(Main.dataFolder + "Documentation - PDF4Teachers.pdf");
		InputStream docRes = getClass().getResourceAsStream("/Documentation - PDF4Teachers.pdf");
		Files.copy(docRes, doc.getAbsoluteFile().toPath(), REPLACE_EXISTING);

		if(firstLaunch){
			Main.mainScreen.openFile(doc);
		}

		// load data
		userData = new UserData();

		if(firstLaunch){
			new LicenseWindow();
		}

		// Open the last file
		// + check version
		new Thread(() -> {
			Platform.runLater(() -> {
				if(settings.getOpenedFile() != null){
					mainScreen.openFile(settings.getOpenedFile());
				}
			});

			if(UpdateWindow.checkVersion()){
				Platform.runLater(() -> {
					menuBar.apropos.setStyle("-fx-background-color: #ba6800;");
					Tooltip.install(menuBar.apropos.getGraphic(), new Tooltip(TR.tr("Une nouvelle version est disponible !")));

					if(settings.isCheckUpdates()){
						new UpdateWindow();
					}
				});
			}
		}).start();

	}


}
