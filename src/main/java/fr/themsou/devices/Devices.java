package fr.themsou.devices;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen;

public class Devices implements MouseListener, KeyListener, MouseWheelListener, MouseMotionListener{

	
	@Override
	public void keyPressed(KeyEvent e) {
		//System.out.println(e.getKeyCode());
		
		if(e.getKeyCode() == 17){
			Main.mainScreen.addMouseWheelListener(Main.devices);
		}
		
	}
	@Override
	public void keyReleased(KeyEvent e){
		
		if(e.getKeyCode() == 17){
			Main.mainScreen.removeMouseWheelListener(Main.devices);
		}
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
		
		if(e.getComponent() == Main.leftBarFiles){
			Main.leftBarFiles.mouseReleased();
		}
		
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		if(MainScreen.current != null){
			
			if(e.isControlDown()){
			
				if(e.getWheelRotation() == 1) MainScreen.zoom -= 5;
				if(e.getWheelRotation() == -1) MainScreen.zoom += 5;
				
				if(MainScreen.zoom <= 0) MainScreen.zoom = 5;
				else if(MainScreen.zoom >= 499) MainScreen.zoom = 500;
				
				Main.mainScreen.repaint();
				Main.footerBar.repaint();
				
				
			}else{
				
				
				
			}
		}
		
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		
		
		
	}

}
