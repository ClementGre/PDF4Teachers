package fr.themsou.panel;

import fr.themsou.main.Main;
import fr.themsou.utils.TR;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
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

		leftInfo.setText(TR.tr("zoom") + " : " + Main.mainScreen.getZoom() + "%");

		switch (Main.leftBar.getSelectionModel().getSelectedIndex()){
			case 0:
				middleInfo.setText(TR.tr("Mode Fichiers")); break;
			case 1:
				middleInfo.setText(TR.tr("Mode Texte")); break;
			case 2:
				middleInfo.setText(TR.tr("Mode Notes")); break;
			case 3:
				middleInfo.setText(TR.tr("Mode Dessin")); break;
		}

		if(Main.mainScreen.getStatus() == MainScreen.Status.OPEN){

			if(Main.mainScreen.document.getCurrentPage() == -1){
				rightInfo.setText(Main.mainScreen.document.getFileName() + " - " + "?/" + Main.mainScreen.document.totalPages);
			}else{
				rightInfo.setText(Main.mainScreen.document.getFileName() + " - " + (Main.mainScreen.document.getCurrentPage()+1) + "/" + Main.mainScreen.document.totalPages);
			}
		}else{
			rightInfo.setText(TR.tr("Aucun fichier ouvert"));
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

		Main.leftBar.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
				repaint();
			}
		});

	}
}
