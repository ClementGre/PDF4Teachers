package fr.themsou.document.editions.elements;

import fr.themsou.document.render.PageRenderer;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Element {

	// Size for A4 - 200dpi (Static)
	float GRID_WIDTH = 1654;
	float GRID_HEIGHT = 2339;

	void writeSimpleData(DataOutputStream writer) throws IOException;


	void select();
	void delete();

	int getPageNumber();
	int getCurrentPageNumber();
	Element clone();

	PageRenderer getPage();
	void setPage(PageRenderer page);

	static Font getFont(String family, boolean italic, boolean bold, double size){

		return Font.loadFont(getFontFile(family, italic, bold), size);
	}
	static InputStream getFontFile(String family, boolean italic, boolean bold){

		String fileFontName = getFontFileName(italic, bold);

		InputStream fontFile = TextElement.class.getResourceAsStream("/fonts/" + family + "/" + fileFontName + ".ttf");

		while(fontFile == null){
			if(fileFontName.equals("bold") || fileFontName.equals("italic")){
				fileFontName = "regular";
			}else if(fileFontName.equals("bolditalic")){
				if(TextElement.class.getResourceAsStream("/fonts/" + family + "/italic.ttf") != null) fileFontName = "italic";
				else if(TextElement.class.getResourceAsStream("/fonts/" + family + "/bold.ttf") != null) fileFontName = "bold";
				else fileFontName = "regular";
			}else{
				System.out.println("Erreur : impossible de charger le font : " + family + " en bold=" + bold + " et italic=" + italic + " (fileFontName = " + fileFontName + " )");
				return null;
			}

			fontFile = TextElement.class.getResourceAsStream("/fonts/" + family + "/" + fileFontName + ".ttf");
		}

		return fontFile;
	}
	static String getFontFileName(boolean italic, boolean bold){

		String fileName = "";
		if(bold) fileName += "bold";
		if(italic) fileName += "italic";
		if(fileName.isEmpty()) fileName = "regular";

		return fileName;
	}

	static FontWeight getFontWeight(Font font) {

		String[] style = font.getStyle().split(" ");
		if(style.length >= 1){
			if(style[0].equals("Bold")){
				return FontWeight.BOLD;
			}
		}

		return FontWeight.NORMAL;
	}
	static FontPosture getFontPosture(Font font) {

		String[] style = font.getStyle().split(" ");
		if(style.length == 1){
			if(style[0].equals("Italic")){
				return FontPosture.ITALIC;
			}
		}else if(style.length == 2){
			if(style[1].equals("Italic")){
				return FontPosture.ITALIC;
			}
		}

		return FontPosture.REGULAR;
	}


}
