package fr.themsou.main;

import java.awt.*;
import java.io.File;

import fr.themsou.devices.Devices;
import fr.themsou.panel.Footerbar;
import fr.themsou.panel.LeftBar.LBFilesTab;
import fr.themsou.panel.LeftBar.LBNoteTab;
import fr.themsou.panel.LeftBar.LBPaintTab;
import fr.themsou.panel.LeftBar.LBTextTab;
import fr.themsou.panel.MainScreen;
import fr.themsou.panel.MenuBar;
import fr.themsou.utils.Builders;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro8.JMetro;

public class Main extends Application {

	public static Stage window;

	public static Devices devices = new Devices();
	public static Settings settings = new Settings();

	public static boolean click = false;
	
//		MAIN
	
	public static MainScreen mainScreen = new MainScreen();
	public static StackPane mainScreenScroll = new StackPane();
	//public static DropTarget mainScreenDrop = new DropTarget(mainScreen, new FileDrop(1));
	
//		LEFT BAR
	
	public static TabPane leftBar = new TabPane();
	public static LBFilesTab lbFilesTab = new LBFilesTab();
	public static LBTextTab lbTextTab = new LBTextTab();
	public static LBNoteTab lbNoteTab = new LBNoteTab();
	public static LBPaintTab lbPaintTab = new LBPaintTab();

	//public static DropTarget lbFilesDrop = new DropTarget(lbFiles, new FileDrop(2));


	public static ScrollPane leftBarTextScroll = new ScrollPane();

//		FOOTER-HEADER BAR
	
	public static Footerbar footerBar = new Footerbar();
	public static MenuBar menuBar = new MenuBar();
	
	public static void main(String[] args){
		launch(args);

		while(true){
			try{
				Thread.sleep(30);
			}catch(InterruptedException e){ e.printStackTrace(); }

			if(leftBar.getSelectionModel().getSelectedIndex() == 0){
				//lbFiles.repaint();
			}else if(leftBar.getSelectionModel().getSelectedIndex() == 1){
				lbTextTab.setup();
			}
			//mainScreen.repaint();
		}
		
	}
	


	@Override
	public void start(Stage window) throws Exception {

		Main.window = window;
		BorderPane root = new BorderPane();
		//VBox root = new VBox();
		Scene scene = new Scene(root, 1200, 675);
		window.setMinWidth(700);
		window.setMinHeight(393);

		new JMetro(JMetro.Style.LIGHT).applyTheme(root);

		window.setTitle("PDF Teacher - Aucun document");
		window.setScene(scene);
		window.setResizable(true);
		window.setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
			@Override
			public void handle(javafx.stage.WindowEvent windowEvent) {
				if(mainScreen.document != null){
					if(mainScreen.document.save()){
						System.exit(0);
					}
				}else{
					System.exit(0);
				}
			}
		});
		devices.addKeyHandler(window.getScene());
		devices.addMousePresedHandler(window.getScene());
		devices.addMouseReleasedHandler(window.getScene());
		devices.addScrollHandler(window.getScene());

		/*leftBar.add(leftBarTextScroll, new ImageIcon(Main.devices.getClass().getResource("/img/Text.png")));
		leftBar.add(new LeftbarNote(), new ImageIcon(Main.devices.getClass().getResource("/img/Note.png")));
		leftBar.add(new LeftbarPaint(), new ImageIcon(Main.devices.getClass().getResource("/img/Paint.png")));*/
		leftBar.setPrefWidth(270);


//		FOOTER-HEADER BAR

		menuBar.setup();
		footerBar.setPrefHeight(20);
		System.setProperty("apple.laf.useScreenMenuBar", "true");

//		PANEL
		root.setCenter(mainScreenScroll);
		root.setTop(menuBar);
		root.setBottom(footerBar);
		root.setLeft(leftBar);
		mainScreenScroll.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		window.show();

	}

    /*
    		while(true){
    			String input = JOptionPane.showInputDialog(null, "Entrez le nom de x", <Panel>);
				if(input == null){
					break; // Annulera
				}
				if(input.isEmpty()){
					JOptionPane.showMessageDialog(null, "Vous devez saisir un nom");
					continue; // Erreur
				}
				if(input != null){ // GOOD

					break;
				}
    		}

    		String[] values = new String[]{"1  580px/861px", "2  1160px/1722px", "3  1740px/2583px", "4  2320px/3444px", "5  29000px/4305px"};
    		String input = (String) JOptionPane.showInputDialog(null, "Choisissez une valeur", "NOM de la fenêtre", JOptionPane.QUESTION_MESSAGE, null, values, values[2]);
			if(input != null){ // GOOD

			}

    		int i = JOptionPane.showConfirmDialog(null, "Etes vous sur de vouloir supprimer ???");
			if(i == 0){ // YES

			}
     */

	public static String getFileExtension(File file) {
		String fileName = file.getName();
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".")+1);
		else return "";
	}


}
