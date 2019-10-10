package fr.themsou.devices;
import java.awt.event.KeyEvent;

import fr.themsou.main.Main;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class Devices{



	public void addKeyHandler(Scene scene){
		scene.setOnKeyPressed(e -> {
			KeyCode keyCode = e.getCode();

			if(keyCode.equals(KeyCode.CONTROL)){
				addScrollHandler(Main.mainScreen.getScene());
			}
		});
	}

	public void addMousePresedHandler(Scene scene) {
		scene.setOnMousePressed(e -> {
			Main.click = true;
			if(e.getSource() == Main.mainScreen){

			}
		});
	}
	public void addMouseReleasedHandler(Scene scene) {
		scene.setOnMouseReleased(e -> {
			Main.click = false;
			if(e.getSource() == Main.lbFilesTab){
			}
		});
	}


	public void addScrollHandler(Scene scene) {

		scene.setOnScroll(e -> {
			System.out.println("scroll");
			if(Main.mainScreen.getStatus() == -1){

				if(e.isControlDown()){
					if(e.getDeltaY() > 0) Main.mainScreen.zoomLess();
					if(e.getDeltaY() < 0) Main.mainScreen.zoomMore();

				}
			}
		});
	}
}
