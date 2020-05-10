package fr.themsou.document.editions.elements;

import fr.themsou.document.render.PageRenderer;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public interface Element {

	ObservableList<String> fonts = FXCollections.observableArrayList("Arial", "Lato", "Lato Light", "Calibri", "Calibri Light", "Roboto", "Times New Roman", "Segoe Print", "Arrows");
	ObservableList<Integer> sizes = FXCollections.observableArrayList(6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30, 34, 38, 42, 46, 50);

	// Size for A4 - 200dpi (Static)
	float GRID_WIDTH = 1654;
	float GRID_HEIGHT = 2339;

	void checkSwitchLocation(double itemX, double itemY);
	void checkLocation(double itemX, double itemY);

	// SELECT - DELETE - SWITCH PAGE
	void select();
	void delete();
	void switchPage(int page);

	// READER AND WRITERS
	HashMap<Object, Object> getYAMLData();

	// COORDINATES GETTERS ANS SETTERS
	int getRealX();
	IntegerProperty RealXProperty();
	void setRealX(int x);
	int getRealY();
	IntegerProperty RealYProperty();
	void setRealY(int y);

	// PAGE GETTERS ANS SETTERS
	PageRenderer getPage();
	int getPageNumber();
	void setPage(PageRenderer page);
	void setPage(int pageNumber);

	// TRANSFORMATIONS
	Element clone();

	// STATIC FUNCTIONS

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
				System.err.println("Erreur : impossible de charger le font : " + family + " en bold=" + bold + " et italic=" + italic + " (fileFontName = " + fileFontName + " )");
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
