package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class PageEditPane extends VBox {

    Button ascendButton = getCustomButton(SVGPathIcons.FORWARD_ARROWS, "Monte cette page au dessus de la page précédente", -90);
    Button descendButton = getCustomButton(SVGPathIcons.FORWARD_ARROWS, "Descend cette page au dessous de la page suivante", 90);
    Button rotateLeftButton = getCustomButton(SVGPathIcons.UNDO, "Tourne la page de 90° vers la gauche");
    Button rotateRightButton = getCustomButton(SVGPathIcons.REDO, "Tourne la page de 90° vers la droite");
    Button deleteButton = getCustomButton(SVGPathIcons.PLUS, "Supprime cette page", 45);
    Button newButton = getCustomButton(SVGPathIcons.PLUS, "Ajoute une page blanche ou une/des images converties en PDF en dessous de cette page");
    Button captureButton = getCustomButton(SVGPathIcons.SCREEN_CORNERS, "Capturer la page sous forme d'image");

    ContextMenu menu = new ContextMenu();

    private PageRenderer page;

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
            menu.getItems().addAll(getNewPageMenu(page.getPage(), 0, false));
            NodeMenuItem.setupMenu(menu);
            menu.show(page, e.getScreenX(), e.getScreenY());
        });

        captureButton.setOnAction((e) -> {});

        getChildren().addAll(ascendButton, descendButton, rotateLeftButton, rotateRightButton, deleteButton, newButton, captureButton);

        updateVisibility();
        updatePosition();
        page.getChildren().add(this);

    }

    public static ArrayList<MenuItem> getNewPageMenu(int page, int addAtTheEnd, boolean vanillaMenu){
        ArrayList<MenuItem> menus = new ArrayList<>();
        if(page == 0){
            MenuItem addTopBlank = getMenuItem(TR.tr("Ajouter une page blanche au dessus"), vanillaMenu);
            MenuItem addTopConvert = getMenuItem(TR.tr("Ajouter des pages converties au dessus"), vanillaMenu);
            MenuItem addTopPdf = getMenuItem(TR.tr("Ajouter les pages d'un fichier PDF au dessus"), vanillaMenu);
            menus.add(addTopBlank);
            menus.add(addTopConvert);
            menus.add(addTopPdf);
            menus.add(new SeparatorMenuItem());

            addTopBlank.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newBlankPage(page, page));
            addTopConvert.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newConvertPage(page, page));
            addTopPdf.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newPdfPage(page));
        }

        MenuItem addBlank = getMenuItem(TR.tr("Ajouter une page blanche"), vanillaMenu);
        MenuItem addConvert = getMenuItem(TR.tr("Ajouter des pages converties"), vanillaMenu);
        MenuItem addTopPdf = getMenuItem(TR.tr("Ajouter les pages d'un fichier PDF"), vanillaMenu);
        menus.add(addBlank);
        menus.add(addConvert);
        menus.add(addTopPdf);
        int index = (addAtTheEnd != 0) ? addAtTheEnd : page+1;
        addBlank.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newBlankPage(page, index));
        addConvert.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newConvertPage(page, index));
        addTopPdf.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newPdfPage(index));

        return menus;
    }
    private static MenuItem getMenuItem(String title, boolean vanillaItem){
        if(vanillaItem) return new MenuItem(title);
        else return new NodeMenuItem(new HBox(), title, false);
    }
    private Button getCustomButton(String path, String nonTranslatedToolTip){
        return getCustomButton(path, nonTranslatedToolTip, 0);
    }
    private Button getCustomButton(String path, String nonTranslatedToolTip, int rotate){
        Button button = new Button();
        button.setStyle("-fx-background-color: white;");
        PaneUtils.setHBoxPosition(button, 30, 30, 0);
        button.setCursor(Cursor.HAND);
        button.setGraphic(SVGPathIcons.generateImage(path, "#dc3e3e", 3, 30, 30, rotate));
        button.setTooltip(PaneUtils.genToolTip(TR.tr(nonTranslatedToolTip)));
        return button;
    }

    public void updatePosition(){
        int buttonNumber = 7;
        setLayoutY(0 - 30* buttonNumber /4D);
        setLayoutX(page.getWidth() - 30/4D);
        setScaleX(0.5);
        setScaleY(0.5);
    }

    public void updateVisibility() {
        ascendButton.setDisable(page.getPage() == 0);
        descendButton.setDisable(page.getPage() == MainWindow.mainScreen.document.totalPages - 1);
        deleteButton.setDisable(MainWindow.mainScreen.document.totalPages == 1);
    }
}