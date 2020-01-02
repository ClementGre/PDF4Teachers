package fr.themsou.devices;
import java.awt.event.KeyEvent;

import fr.themsou.main.Main;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class Devices{



	public void addMousePresedHandler(Scene scene) {
		scene.setOnMousePressed(e -> {
			Main.click = true;
		});
	}
	public void addMouseReleasedHandler(Scene scene) {
		scene.setOnMouseReleased(e -> {
			Main.click = false;
		});
	}
}
