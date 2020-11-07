package fr.themsou.document.render.display;

import fr.themsou.utils.Builders;
import fr.themsou.utils.components.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

public class PageEditPane extends HBox {

    private static final Image ascendImage = new Image(PageEditPane.class.getResource("/img/Pages/ascend.png")+"");
    private static final Image descendImage = new Image(PageEditPane.class.getResource("/img/Pages/descend.png")+"");
    private static final Image rotateLeftImage = new Image(PageEditPane.class.getResource("/img/Pages/rotate-left.png")+"");
    private static final Image rotateRightImage = new Image(PageEditPane.class.getResource("/img/Pages/rotate-right.png")+"");
    private static final Image deleteImage = new Image(PageEditPane.class.getResource("/img/Pages/delete.png")+"");
    private static final Image newImage = new Image(PageEditPane.class.getResource("/img/Pages/new.png")+"");

    Button ascendButton = getCustomButton(ascendImage, "Monte cette page au dessus de la page précédente");
    Button descendButton = getCustomButton(descendImage, "Descend cette page au dessous de la page suivante");
    Button rotateLeftButton = getCustomButton(rotateLeftImage, "Tourne la page de 90° vers la gauche");
    Button rotateRightButton = getCustomButton(rotateRightImage, "Tourne la page de 90° vers la droite");
    Button deleteButton = getCustomButton(deleteImage, "Supprime cette page");
    Button newButton = getCustomButton(newImage, "Ajoute une page blanche ou une/des images converties en PDF en dessous de cette page");

    ContextMenu menu = new ContextMenu();

    private PageRenderer page;
    private int buttonNumber = 6;
    public PageEditPane(PageRenderer page){
        this.page = page;



        ascendButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.ascendPage(page));

        descendButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.descendPage(page));

        rotateLeftButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.rotateLeftPage(page));

        rotateRightButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.rotateRightPage(page));

        deleteButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.deletePage(page));

        newButton.setOnMouseClicked((e) -> {

            menu.hide();
            menu.getItems().clear();
            menu.getItems().addAll(getNewPageMenu(page.getPage(), 0));
            NodeMenuItem.setupMenu(menu);
            menu.show(page, e.getScreenX(), e.getScreenY());
        });

        getChildren().addAll(ascendButton, descendButton, rotateLeftButton, rotateRightButton, deleteButton, newButton);

        updateVisibility();
        updatePosition();
        page.getChildren().add(this);

    }

    public static ArrayList<MenuItem> getNewPageMenu(int page, int addAtTheEnd){
        System.out.println("ask");
        ArrayList<MenuItem> menus = new ArrayList<>();
        if(page == 0){
            NodeMenuItem addTopBlank = new NodeMenuItem(new HBox(), TR.tr("Ajouter une page blanche au dessus"), false);
            NodeMenuItem addTopConvert = new NodeMenuItem(new HBox(), TR.tr("Ajouter des pages converties au dessus"), false);
            NodeMenuItem addTopPdf = new NodeMenuItem(new HBox(), TR.tr("Ajouter les pages d'un fichier PDF au dessus"), false);
            menus.add(addTopBlank);
            menus.add(addTopConvert);
            menus.add(addTopPdf);
            menus.add(new SeparatorMenuItem());

            addTopBlank.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newBlankPage(page, page));
            addTopConvert.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newConvertPage(page, page));
            addTopPdf.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newPdfPage(page));
        }

        NodeMenuItem addBlank = new NodeMenuItem(new HBox(), TR.tr("Ajouter une page blanche"), false);
        NodeMenuItem addConvert = new NodeMenuItem(new HBox(), TR.tr("Ajouter des pages converties"), false);
        NodeMenuItem addTopPdf = new NodeMenuItem(new HBox(), TR.tr("Ajouter les pages d'un fichier PDF"), false);
        menus.add(addBlank);
        menus.add(addConvert);
        menus.add(addTopPdf);
        int index = (addAtTheEnd != 0) ? addAtTheEnd : page+1;
        addBlank.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newBlankPage(page, index));
        addConvert.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newConvertPage(page, index));
        addTopPdf.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newPdfPage(index));

        System.out.println("return menus");
        return menus;
    }

    private Button getCustomButton(Image image, String nonTranslatedToolTip){
        Button button = new Button();
        button.setStyle("-fx-background-color: white;");
        Builders.setHBoxPosition(button, 30, 30, 0);
        button.setCursor(Cursor.HAND);
        button.setGraphic(Builders.buildImage(image, 30, 30));
        button.setTooltip(Builders.genToolTip(TR.tr(nonTranslatedToolTip)));
        return button;
    }

    public void updatePosition(){
        setLayoutY(page.getHeight() - 15/2D);
        setLayoutX(page.getWidth() - 30*buttonNumber + 30*buttonNumber/4D);
        setScaleX(0.5);
        setScaleY(0.5);
    }

    public void updateVisibility() {
        ascendButton.setDisable(page.getPage() == 0);
        descendButton.setDisable(page.getPage() == MainWindow.mainScreen.document.totalPages - 1);
        deleteButton.setDisable(MainWindow.mainScreen.document.totalPages == 1);
    }
}
