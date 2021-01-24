package fr.clementgre.pdf4teachers.panel.sidebar;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.scene.control.Tab;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

public class SideTab extends Tab {

    public SideTab(boolean defaultLeft, String iconPath, int width, int height, int[] ratio){

        setClosable(false);

        setGraphic(SVGPathIcons.generateImage(iconPath, "gray", 0, width, height, 0, ratio, ImageUtils.defaultGrayColorAdjust));

        setupDragAndDrop(iconPath);
        if(defaultLeft) MainWindow.leftBar.getTabs().add(this);
        else MainWindow.rightBar.getTabs().add(this);
    }


    public void setupDragAndDrop(String iconPath){

        getGraphic().setOnDragDetected(event -> {
            Dragboard dragboard = getGraphic().startDragAndDrop(TransferMode.MOVE);

            dragboard.setDragView(SVGPathIcons.generateNonSvgImage(iconPath, Color.GRAY, ImageUtils.defaultGrayColorAdjust, .08));

            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(SideBar.DRAG_TAB_KEY);
            dragboard.setContent(clipboardContent);
            SideBar.draggingTab = this;
            event.consume();
        });

    }

}
