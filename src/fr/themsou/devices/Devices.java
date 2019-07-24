package fr.themsou.devices;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import fr.themsou.main.Main;
import fr.themsou.panel.Mainscreen;

public class Devices implements MouseListener, KeyListener, MouseWheelListener{

	
	@Override
	public void keyPressed(KeyEvent e) {
		//System.out.println(e.getKeyCode());
		
		if(e.getKeyCode() == 17){
			Main.mainscreen.addMouseWheelListener(Main.devices);
		}
		
	}
	@Override
	public void keyReleased(KeyEvent e){
		
		if(e.getKeyCode() == 17){
			Main.mainscreen.removeMouseWheelListener(Main.devices);
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
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		if(Mainscreen.current != null){
			
			if(e.isControlDown()){
			
				System.out.println("Rotation=" + e.getWheelRotation() + "  ClicCount=" + e.getClickCount());
				
				if(e.getWheelRotation() == 1) Mainscreen.zoom -= 5 * e.getClickCount();
				
				else Mainscreen.zoom += 5;
				
				if(Mainscreen.zoom <= 0) Mainscreen.zoom = 5;
				else if(Mainscreen.zoom >= 499) Mainscreen.zoom = 500;
				
				
				Main.mainscreen.repaint();
				Main.footerBar.repaint();
				
				
			}else{
				
				
				
			}
		}
		
	}

}
