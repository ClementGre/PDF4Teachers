package fr.themsou.main;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import fr.themsou.devices.Devices;
import fr.themsou.panel.Footerbar;
import fr.themsou.panel.LeftBar.LBFilesTab;
import fr.themsou.panel.LeftBar.LBNoteTab;
import fr.themsou.panel.LeftBar.LBPaintTab;
import fr.themsou.panel.LeftBar.LBTextTab;
import fr.themsou.panel.MainScreen;
import fr.themsou.panel.MenuBar;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class Main extends Application {

	public static Stage window;

	public static Devices devices;
	public static Settings settings;
	public static UserData userData;

	public static boolean click = false;
	public static boolean hasToClose = false;
	
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
	
	public static Footerbar footerBar;
	public static MenuBar menuBar;

	public static HostServices hostServices;

	public static String dataFolder = System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator;

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
		BorderPane root = new BorderPane(); // = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("main.fxml")));

		Scene scene = new Scene(root, 1200, 675);

		window.setMinWidth(700);
		window.setMinHeight(393);
		window.setTitle("PDF4Teachers - Aucun document");
		window.getIcons().add(new Image(getClass().getResource("/logo.png")+""));
		window.setScene(scene);
		window.setResizable(true);
		window.setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
			@Override
			public void handle(javafx.stage.WindowEvent e) {
				hasToClose = true;
				if(!mainScreen.closeFile(!settings.isAutoSave())){
					userData.saveData();
					e.consume();
					hasToClose = false;
					return;
				}
				userData.saveData();
				System.exit(0);
			}
		});

//		SETUPS

		settings = new Settings();
		devices = new Devices();

		mainScreen = new MainScreen((int) (21 * 37.795275591));
		footerBar = new Footerbar();

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

//		SETUP DEVICES

		devices.addMousePresedHandler(window.getScene());
		devices.addMouseReleasedHandler(window.getScene());

//		THEME

		new JMetro(root, Style.LIGHT);
		new JMetro(menuBar, Style.DARK);

//		SHOWING

		userDataSaver.start();
		mainScreen.repaint();

		if(firstLaunch){

			File doc = new File(Main.dataFolder + "Documentation - PDF4Teachers.pdf");
			if(!doc.exists()){
				InputStream docRes = getClass().getResourceAsStream("/Documentation - PDF4Teachers.pdf");
				Files.copy(docRes, doc.getAbsoluteFile().toPath());
			}
			Main.mainScreen.openFile(doc);

		}


		if(settings.getOpenedFile() != null){
			mainScreen.openFile(settings.getOpenedFile());
		}

		// load data
		userData = new UserData();

		if(firstLaunch){
			new License();
		}

	}


}
