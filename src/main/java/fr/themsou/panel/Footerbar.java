package fr.themsou.panel;

import fr.themsou.main.Main;
import fr.themsou.utils.StringDrawing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Footerbar extends JPanel{

	public void paintComponent(Graphics go){
		
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		switch (Main.leftBar.getSelectedIndex()){

			case 0:
				StringDrawing.fullCenterString(g, 0, getWidth(), 0, getHeight(), "Mode Fichiers", new Font("FreeSans", Font.PLAIN, 15));
			break;
			case 1:
				StringDrawing.fullCenterString(g, 0, getWidth(), 0, getHeight(), "Mode Texte", new Font("FreeSans", Font.PLAIN, 15));
			break;
			case 2:
				StringDrawing.fullCenterString(g, 0, getWidth(), 0, getHeight(), "Mode Notes", new Font("FreeSans", Font.PLAIN, 15));
			break;
			case 3:
				StringDrawing.fullCenterString(g, 0, getWidth(), 0, getHeight(), "Mode Dessin", new Font("FreeSans", Font.PLAIN, 15));
			break;

		}
		
		if(Main.mainScreen.status == -1){
			if(Main.mainScreen.document.currentPage == -1){
				StringDrawing.alignRightString(g, getWidth() - 4, 0, getHeight(), Main.mainScreen.document.getFileName() + " - " + "?/" + Main.mainScreen.document.totalPages, new Font("FreeSans", Font.PLAIN, 15));
			}else{
				StringDrawing.alignRightString(g, getWidth() - 4, 0, getHeight(), Main.mainScreen.document.getFileName() + " - " + (Main.mainScreen.document.currentPage+1) + "/" + Main.mainScreen.document.totalPages, new Font("FreeSans", Font.PLAIN, 15));
			}
		}else{
			StringDrawing.alignRightString(g, getWidth() - 4, 0, getHeight(), "Aucun fichier ouvert", new Font("FreeSans", Font.PLAIN, 15));
		}

		StringDrawing.alignLeftString(g, 4, 0, getHeight(), "zoom : " + Main.mainScreen.zoom + "%", new Font("FreeSans", Font.PLAIN, 15));
	
	}
	
	


}
