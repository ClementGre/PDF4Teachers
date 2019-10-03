package fr.themsou.main;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.io.File;

import fr.themsou.devices.Devices;
import fr.themsou.devices.FileDrop;
import fr.themsou.panel.Footerbar;
import fr.themsou.panel.LeftbarFiles;
import fr.themsou.panel.LeftbarNote;
import fr.themsou.panel.LeftbarPaint;
import fr.themsou.panel.LeftbarText;
import fr.themsou.panel.MainScreen;
import fr.themsou.panel.MenuBar;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro8.JMetro;

import javax.swing.*;

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

	public static LeftbarFiles leftBarFiles = new LeftbarFiles();
	public static Tab leftBarFilesTab = new Tab("", leftBarFiles);

	public static LeftbarText leftBarText = new LeftbarText();
	public static Tab leftBarTextTab = new Tab("", leftBarText);

	//public static DropTarget leftBarFilesDrop = new DropTarget(leftBarFiles, new FileDrop(2));


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
				//leftBarFiles.repaint();
			}else if(leftBar.getSelectionModel().getSelectedIndex() == 1){
				leftBarText.setup();
			}
			//mainScreen.repaint();


		}
		
	}
	
	public static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

	@Override
	public void start(Stage window) throws Exception {

		Main.window = window;
		BorderPane root = new BorderPane();
		//VBox root = new VBox();
		Scene scene = new Scene(root);

		new JMetro(JMetro.Style.LIGHT).applyTheme(root);

		window.setTitle("PDF Teacher - Aucun document");
		window.setScene(scene);
		root.setMinSize(700, 393);
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




//		LEFT BAR
		leftBarFilesTab.setClosable(false);
		leftBarFilesTab.setGraphic(buildImage(getClass().getResource("/img/PDF-Document.png") + ""));
		leftBar.getTabs().add(0, leftBarFilesTab);

		leftBarTextTab.setClosable(false);
		leftBarTextTab.setGraphic(buildImage(getClass().getResource("/img/Text.png") + ""));
		leftBar.getTabs().add(1, leftBarTextTab);

		/*leftBar.add(leftBarTextScroll, new ImageIcon(Main.devices.getClass().getResource("/img/Text.png")));
		leftBar.add(new LeftbarNote(), new ImageIcon(Main.devices.getClass().getResource("/img/Note.png")));
		leftBar.add(new LeftbarPaint(), new ImageIcon(Main.devices.getClass().getResource("/img/Paint.png")));*/
		leftBar.setPrefWidth(230);


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
		window.toBack();
		window.toFront();

	}
	private static ImageView buildImage(String imgPatch) {
		Image i = new Image(imgPatch);
		ImageView imageView = new ImageView();
		//You can set width and height
		imageView.setFitWidth(18);
		imageView.setFitHeight(26);
		imageView.setImage(i);
		return imageView;
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


}
