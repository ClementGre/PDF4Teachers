package fr.themsou.panel;

import fr.themsou.main.Main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LeftbarNote extends JPanel{

	public void paintComponent(Graphics go){

		Main.footerBar.repaint();
		
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
	}

}
