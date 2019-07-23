package fr.themsou.devices;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import fr.themsou.main.Main;

public class Devices implements MouseListener, KeyListener, MouseWheelListener{

	@Override
	public void keyPressed(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Main.click = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		
		Main.click = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		
		
	}

}
