package fr.themsou.panel;

import fr.themsou.main.Main;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

@SuppressWarnings("serial")
public class Footerbar extends AnchorPane {

	Text leftInfo = new Text("");
	Text middleInfo = new Text("");
	Text rightInfo = new Text("");

	public Footerbar(){


		setup();
	}

	public void repaint(){

		leftInfo.setText("zoom : " + Main.mainScreen.getZoom() + "%");

		switch (Main.leftBar.getSelectionModel().getSelectedIndex()){
			case 0:
				middleInfo.setText("Mode Fichiers"); break;
			case 1:
				middleInfo.setText("Mode Texte"); break;
			case 2:
				middleInfo.setText("Mode Notes"); break;
			case 3:
				middleInfo.setText("Mode Dessin"); break;
		}

		if(Main.mainScreen.getStatus() == -1){

			if(Main.mainScreen.document.getCurrentPage() == -1){
				rightInfo.setText(Main.mainScreen.document.getFileName() + " - " + "?/" + Main.mainScreen.document.totalPages);
			}else{
				rightInfo.setText(Main.mainScreen.document.getFileName() + " - " + (Main.mainScreen.document.getCurrentPage()+1) + "/" + Main.mainScreen.document.totalPages);
			}
		}else{
			rightInfo.setText("Aucun fichier ouvert");
		}



	}

	public void setup(){

		setPrefHeight(20);

		setBackground(new Background(new BackgroundFill(Color.rgb(43, 43, 43), CornerRadii.EMPTY, Insets.EMPTY)));

		AnchorPane.setLeftAnchor(leftInfo, 10.0);
		AnchorPane.setRightAnchor(rightInfo, 10.0);

		AnchorPane.setBottomAnchor(leftInfo, 3.0);
		AnchorPane.setBottomAnchor(middleInfo, 3.0);
		AnchorPane.setBottomAnchor(rightInfo, 3.0);

		leftInfo.setFill(Color.WHITE);
		middleInfo.setFill(Color.WHITE);
		rightInfo.setFill(Color.WHITE);
		middleInfo.translateXProperty().bind(widthProperty().divide(2).subtract(leftInfo.getLayoutBounds().getWidth()));

		leftInfo.setFont(new Font("FreeSans", 15));
		middleInfo.setFont(new Font("FreeSans", 15));
		rightInfo.setFont(new Font("FreeSans", 15));

		getChildren().add(leftInfo);
		getChildren().add(middleInfo);
		getChildren().add(rightInfo);

	}
}
