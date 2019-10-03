package fr.themsou.panel;

import fr.themsou.main.Main;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

@SuppressWarnings("serial")
public class Footerbar extends StackPane {

	public void drawShapes(GraphicsContext g){

		g.setFill(Color.WHITE);
		g.fillRect(0, 0, (int)getWidth(), (int)getHeight());

		g.setFill(Color.BLACK);
		g.setFont(new Font("FreeSans", 15));
		g.setTextAlign(TextAlignment.CENTER);

		switch (Main.leftBar.getSelectionModel().getSelectedIndex()){

			case 0:
				g.fillText("Mode Fichiers", getWidth()/2, getHeight()/2);
			break;
			case 1:
				g.fillText("Mode Texte", getWidth()/2, getHeight()/2);
			break;
			case 2:
				g.fillText("Mode Notes", getWidth()/2, getHeight()/2);
			break;
			case 3:
				g.fillText("Mode Dessin", getWidth()/2, getHeight()/2);
			break;

		}

		g.setTextAlign(TextAlignment.LEFT);
		if(Main.mainScreen.status == -1){

			if(Main.mainScreen.document.currentPage == -1){
				g.fillText(Main.mainScreen.document.getFileName() + " - " + "?/" + Main.mainScreen.document.totalPages, getWidth() - 4, getHeight()/2);
			}else{
				g.fillText(Main.mainScreen.document.getFileName() + " - " + (Main.mainScreen.document.currentPage+1) + "/" + Main.mainScreen.document.totalPages, getWidth() - 4, getHeight()/2);
			}
		}else{
			g.fillText("Aucun fichier ouvert", getWidth() - 4, getHeight()/2);
		}

		g.setTextAlign(TextAlignment.RIGHT);
		g.fillText("zoom : " + Main.mainScreen.zoom + "%", 4, getHeight()/2);

	
	}
	
	


}
