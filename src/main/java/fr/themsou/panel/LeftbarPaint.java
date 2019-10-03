package fr.themsou.panel;

import fr.themsou.main.Main;

import javax.swing.*;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

@SuppressWarnings("serial")
public class LeftbarPaint extends JPanel {

	public void paintComponent(Graphics go){

		//Main.footerBar.repaint();

		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
	}

	
}
