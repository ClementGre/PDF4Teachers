package fr.themsou.panel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.io.File;

import javax.swing.JPanel;

import fr.themsou.document.Document;
import fr.themsou.main.Main;
import fr.themsou.utils.StringDrawing;

public class MainScreen extends JPanel{

	public int zoom = 150;
	public int status = 0;

	private int lastWidth = getWidth();
	private int lastHeight = getHeight();

	public Document document;
	
	public void paintComponent(Graphics go){
		
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;
		
		g.setColor(new Color(102, 102, 102));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(status != -1){
			
			if(status == 0){
				g.setColor(Color.WHITE);
				StringDrawing.fullCenterString(g, 0, getWidth(), 0, getHeight(), "Aucun document ouvert", new Font("FreeSans", Font.BOLD, 20));
			}else if(status == 1){
				g.setColor(Color.WHITE);
				StringDrawing.fullCenterString(g, 0, getWidth(), 0, getHeight(), "Chargement du document...", new Font("FreeSans", Font.BOLD, 20));
			}else if(status == 2){
				g.setColor(Color.WHITE);
				StringDrawing.fullCenterString(g, 0, getWidth(), 0, getHeight(), "Une erreur est survenue lors du chargement du document :/", new Font("FreeSans", Font.BOLD, 20));
			}

		}else{

			// Measure page size

			int imgWidth; int imgHeight;
			if((double)Main.mainScreenScroll.getHeight() / (double)Main.mainScreenScroll.getWidth() > (double)document.rendered[0].getHeight(null) / (double)document.rendered[0].getWidth(null)){
				int maxSize = Main.mainScreenScroll.getWidth();
				imgWidth = (int) ((((double) zoom) / 100.0) * ((double) maxSize) - 100);
				imgHeight = (int) (((double) imgWidth) / ((double) document.rendered[0].getWidth(null)) * document.rendered[0].getHeight(null));
			}else{
				int maxSize = Main.mainScreenScroll.getHeight();
				imgHeight = (int) (( ((double) zoom) / 100.0) * ((double) maxSize) -100);
				imgWidth = (int) (((double) imgHeight) / ((double) document.rendered[0].getHeight(null)) * document.rendered[0].getWidth(null));
			}

			// render

			int page = 0;
			int imgsHeight = 40;
			int imgMouseX;
			int imgMouseY;
			for(Image img : document.rendered){
				
				imgMouseX = (int) (((double) (mouseX - (getWidth()/2-imgWidth/2))) / ((double)imgWidth) * img.getWidth(null));
				imgMouseY = (int) (((double) (mouseY - imgsHeight)) / ((double)imgHeight) * img.getHeight(null));
				
				Image imgRendered = document.edition.editRender.render(img, page, imgMouseX, imgMouseY);
				
				g.drawImage(imgRendered, getWidth()/2-imgWidth/2, imgsHeight, imgWidth, imgHeight, null);
				
				imgsHeight += imgHeight + 40;
				page++;
			}

			updateAfterRender();

			// Update UI

			if(lastWidth != imgWidth || lastHeight != imgHeight){
				setPreferredSize(new Dimension(imgWidth + 80, imgsHeight));
				Main.mainScreenScroll.updateUI();
				lastWidth = imgWidth;
				lastHeight = imgHeight;
			}
			
			
			
		}
		
		
	}
	
	public void openFile(File file){
		
		closeFile();
		paintComponent(getGraphics());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        status = 1;
		this.document = new Document(file);

		if(document.renderPDFPages()){
			status = -1;
			Main.window.setTitle("PDF Teacher - " + file.getName());
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		repaint();
		Main.footerBar.repaint();
		
	}
	public void closeFile(){

	    if(document != null){
            document.save();
            document = null;
        }

		status = 0;
		zoom = 150;
		lastWidth = 0;
		setPreferredSize(new Dimension(0, 0));
		Main.mainScreenScroll.updateUI();
		Main.window.setTitle("PDF Teacher - Aucun document");
	}

	public void updateAfterRender(){

		if(document.edition.editRender.current != null || document.edition.editRender.hand != null){
			if(getCursor() != Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR))
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}else{
			if(getCursor() != Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		document.edition.editRender.current = null;
		if(document.currentPage == -1) Main.footerBar.repaint();

	}
	
}
