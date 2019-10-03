package fr.themsou.devices;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import fr.themsou.main.Main;
import fr.themsou.panel.LeftbarText;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class Devices{


	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == 17){
			//Main.mainScreen.addMouseWheelListener(Main.devices);
		}
	}
	public void keyReleased(KeyEvent e){
		if(e.getKeyCode() == 17){
			//Main.mainScreen.removeMouseWheelListener(Main.devices);
		}
	}


	public void addKeyHandler(Scene scene){
		scene.setOnKeyPressed(e -> {
			KeyCode keyCode = e.getCode();

			if(keyCode.equals(KeyCode.S)){
				return;
			}
			if(keyCode.equals(KeyCode.R)){
				return;
			}
			if(keyCode.equals(KeyCode.P)){
				return;
			}
			if(keyCode.equals(KeyCode.Q) || keyCode.equals(KeyCode.ESCAPE)) {
				return;
			}

		});
	}

	public void addMousePresedHandler(Scene scene) {
		scene.setOnMousePressed(e -> {
			Main.click = true;
			if(e.getSource() == Main.mainScreen){
				Main.mainScreen.requestFocus();
			}
		});
	}
	public void addMouseReleasedHandler(Scene scene) {
		scene.setOnMouseReleased(e -> {
			Main.click = false;
			if(e.getSource() == Main.leftBarFiles){
				Main.leftBarFiles.mouseReleased();
			}
		});
	}


	public void addScrollHandler(Scene scene) {
		scene.setOnScroll(e -> {

			if(Main.mainScreen.status == -1){

				if(e.isControlDown()){

					if(e.getDeltaY() == 1) Main.mainScreen.zoom -= 5;
					if(e.getDeltaY() == -1) Main.mainScreen.zoom += 5;

					if(Main.mainScreen.zoom <= 9) Main.mainScreen.zoom = 10;
					else if(Main.mainScreen.zoom >= 399) Main.mainScreen.zoom = 400;

					//Main.mainScreen.repaint();
					//Main.footerBar.repaint();


				}
			}
		});
	}
}
