package fr.clementgre.pdf4teachers.panel;

import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

@SuppressWarnings("serial")
public class FooterBar extends AnchorPane {

	public Text leftInfo = new Text("");
	public Text middleInfo = new Text("");
	public Text rightInfo = new Text("");

	public FooterBar(){
		StyleManager.putStyle(this, Style.ACCENT);
		setup();
	}

	public void repaint(){

		leftInfo.textProperty().bind(Bindings.createStringBinding(() -> TR.tr("footerBar.zoom", (int) (MainWindow.mainScreen.getZoomPercent())), MainWindow.mainScreen.pane.scaleXProperty()));



		if(MainWindow.mainScreen.getStatus() == MainScreen.Status.OPEN){

			if(MainWindow.mainScreen.document.getCurrentPage() == -1){
				rightInfo.setText(MainWindow.mainScreen.document.getFileName() + " - " + "?/" + MainWindow.mainScreen.document.totalPages);
			}else{
				rightInfo.setText(MainWindow.mainScreen.document.getFileName() + " - " + (MainWindow.mainScreen.document.getCurrentPage()+1) + "/" + MainWindow.mainScreen.document.totalPages);
			}
		}else{
			rightInfo.setText(TR.tr("footerBar.documentStatus.noDocument"));
		}
	}

	public void setup(){

		setPrefHeight(20);
		//setBackground(new Background(new BackgroundFill(Color.rgb(43, 43, 43), CornerRadii.EMPTY, Insets.EMPTY)));

		AnchorPane.setLeftAnchor(leftInfo, 10.0);
		AnchorPane.setRightAnchor(rightInfo, 10.0);

		AnchorPane.setBottomAnchor(leftInfo, 3.0);
		AnchorPane.setBottomAnchor(middleInfo, 3.0);
		AnchorPane.setBottomAnchor(rightInfo, 3.0);

		middleInfo.translateXProperty().bind(widthProperty().divide(2).subtract(leftInfo.getLayoutBounds().getWidth()));

		leftInfo.setStyle("-fx-font-size: 15;");
		middleInfo.setStyle("-fx-font-size: 15;");
		rightInfo.setStyle("-fx-font-size: 15;");

		getChildren().add(leftInfo);
		getChildren().add(middleInfo);
		getChildren().add(rightInfo);

		Platform.runLater(() -> MainWindow.leftBar.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> repaint()));

	}
}
