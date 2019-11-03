package fr.themsou.main;

import java.io.File;
import java.io.InputStream;

import fr.themsou.devices.Devices;
import fr.themsou.panel.Footerbar;
import fr.themsou.panel.LeftBar.LBFilesTab;
import fr.themsou.panel.LeftBar.LBNoteTab;
import fr.themsou.panel.LeftBar.LBPaintTab;
import fr.themsou.panel.LeftBar.LBTextTab;
import fr.themsou.panel.MainScreen;
import fr.themsou.panel.MenuBar;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class Main extends Application {

	public static Stage window;

	public static Devices devices;
	public static Settings settings;
	public static UserData userData;

	public static boolean click = false;
	
//		MAIN
	
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


		Main.window = window;
		BorderPane root = new BorderPane(); // = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("main.fxml")));


		Scene scene = new Scene(root, 1200, 675);

		window.setMinWidth(700);
		window.setMinHeight(393);
		window.setTitle("PDF Teacher - Aucun document");
		window.setScene(scene);
		window.setResizable(true);
		window.setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
			@Override
			public void handle(javafx.stage.WindowEvent e) {
				if(!mainScreen.closeFile(!settings.isAutoSave())){
					userData.saveData();
					e.consume();
					return;
				}
				userData.saveData();
				System.exit(0);
			}
		});

//		SETUPS

		settings = new Settings();
		userData = new UserData();
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
		lbPaintTab.repaint();
		lbNoteTab.repaint();

//		PANELS

		leftBar.setPrefWidth(270);

		root.setCenter(mainScreen);
		root.setTop(menuBar);
		root.setBottom(footerBar);
		root.setLeft(leftBar);

//		SETUP DEVICES

		devices.addKeyHandler(window.getScene());
		devices.addMousePresedHandler(window.getScene());
		devices.addMouseReleasedHandler(window.getScene());

		System.setProperty("apple.laf.useScreenMenuBar", "true");

//		THEME

		new JMetro(root, Style.LIGHT);
		new JMetro(menuBar, Style.DARK);

//		SHOWING

		window.show();
		userDataSaver.start();
		mainScreen.repaint();

	}

	public static String getFileExtension(File file) {
		String fileName = file.getName();
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".")+1);
		else return "";
	}


}
