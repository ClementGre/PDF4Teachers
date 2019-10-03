package fr.themsou.panel;

import java.awt.*;
import java.io.File;
import fr.themsou.document.Document;
import fr.themsou.main.Main;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainScreen extends StackPane {

	public int zoom = 150;
	public int status = 0;

	public Document document;

	public void drawShapes(GraphicsContext g){

		int mouseX = 0;
		int mouseY = 0;
		
		g.setFill(Color.rgb(102, 102, 102));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(status != -1){

			g.setFont(new Font("FreeSans", 20));
			g.setFill(Color.WHITE);

			if(status == 0){
				g.fillText("Aucun document ouvert", getWidth()/2, getHeight()/2);
			}else if(status == 1){
				g.fillText("Chargement du document...", getWidth()/2, getHeight()/2);
			}else if(status == 2){
				g.fillText("Une erreur est survenue lors du chargement du document :/", getWidth()/2, getHeight()/2);
			}

		}else{

			// Measure page size

			int imgWidth; int imgHeight;
			if((double)Main.mainScreenScroll.getHeight() / (double)Main.mainScreenScroll.getWidth() > (double)document.rendered[0].getHeight(null) / (double)document.rendered[0].getWidth(null)){
				int maxSize = (int) Main.mainScreenScroll.getWidth();
				imgWidth = (int) ((((double) zoom) / 100.0) * ((double) maxSize) - 100);
				imgHeight = (int) (((double) imgWidth) / ((double) document.rendered[0].getWidth(null)) * document.rendered[0].getHeight(null));
			}else{
				int maxSize = (int) Main.mainScreenScroll.getHeight();
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
				
				g.drawImage(javafx.scene.image.Image.impl_fromPlatformImage(imgRendered), getWidth()/2-imgWidth/2, imgsHeight, imgWidth, imgHeight);
				
				imgsHeight += imgHeight + 40;
				page++;
			}

			updateAfterRender();

			// Update UI

			/*if(lastWidth != imgWidth || lastHeight != imgHeight){
				setPreferredSize(new Dimension(imgWidth + 80, imgsHeight));
				Main.mainScreenScroll.updateUI();
				lastWidth = imgWidth;
				lastHeight = imgHeight;
			}*/
			
			
			
		}
		
		
	}
	
	public void openFile(File file){
		
		closeFile(true);
		//paintComponent(getGraphics());
		//setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        status = 1;
		this.document = new Document(file);

		if(document.renderPDFPages()){
			status = -1;
			Main.window.setTitle("PDF Teacher - " + file.getName());
		}

		//setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		//repaint();
		//Main.footerBar.repaint();
		
	}
	public boolean closeFile(boolean confirm){


	    if(document != null){
	    	if(confirm){
	    		if(!document.save()) return false;
			}
			else document.edition.save();

            document = null;
        }

        Main.leftBarText.elementToEdit = null;

		status = 0;
		zoom = 150;
		//lastWidth = 0;
		//setPreferredSize(new Dimension(0, 0));
		//Main.mainScreenScroll.updateUI();
		Main.window.setTitle("PDF Teacher - Aucun document");

		return true;
	}

	public void updateAfterRender(){

		/*if(document.edition.editRender.current != null || document.edition.editRender.hand != null){
			if(getCursor() != Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR))
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}else{
			if(getCursor() != Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}*/

		document.edition.editRender.current = null;
		//if(document.currentPage == -1) Main.footerBar.repaint();

	}
	
}
