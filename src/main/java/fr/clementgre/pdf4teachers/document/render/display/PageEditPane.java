package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public class PageEditPane extends VBox{
    
    private final Button ascendButton = getCustomButton(SVGPathIcons.FORWARD_ARROWS, TR.tr("document.pageActions.moveUp.tooltip"), -90);
    private final Button descendButton = getCustomButton(SVGPathIcons.FORWARD_ARROWS, TR.tr("document.pageActions.moveDown.tooltip"), 90);
    private final Button rotateLeftButton = getCustomButton(SVGPathIcons.UNDO, TR.tr("document.pageActions.rotateLeft.tooltip"));
    private final Button rotateRightButton = getCustomButton(SVGPathIcons.REDO, TR.tr("document.pageActions.rotateRight.tooltip"));
    private final Button deleteButton = getCustomButton(SVGPathIcons.PLUS, TR.tr("document.pageActions.delete.tooltip"), 45);
    private final Button newButton = getCustomButton(SVGPathIcons.PLUS, TR.tr("document.pageActions.addPages.tooltip"));
    private final Button captureButton = getCustomButton(SVGPathIcons.FULL_SCREEN, TR.tr("document.pageActions.capture.tooltip"));
    
    private ContextMenu menu = new ContextMenu();
    
    private PageRenderer page;
    
    public PageEditPane(PageRenderer page){
        this.page = page;
        
        ascendButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.ascendPage(page));
        
        descendButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.descendPage(page));
        
        rotateLeftButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.rotateLeftPage(page, true));
        
        rotateRightButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.rotateRightPage(page, true));
        
        deleteButton.setOnAction((e) -> MainWindow.mainScreen.document.pdfPagesRender.editor.deletePage(page));
        
        newButton.setOnMouseClicked((e) -> {
            menu.hide();
            menu.getItems().clear();
            menu.getItems().addAll(getNewPageMenu(page.getPage(), 0, false));
            NodeMenuItem.setupMenu(menu);
            menu.show(newButton, e.getScreenX(), e.getScreenY());
        });
        
        captureButton.setOnMouseClicked((e) -> {
            menu.hide();
            menu.getItems().clear();
            menu.getItems().addAll(getCaptureMenu(page, false));
            NodeMenuItem.setupMenu(menu);
            menu.show(captureButton, e.getScreenX(), e.getScreenY());
        });
        
        getChildren().addAll(ascendButton, descendButton, rotateLeftButton, rotateRightButton, deleteButton, newButton, captureButton);
        
        updateVisibility();
        updatePosition();
        page.getChildren().add(this);
        
    }
    
    public static ArrayList<MenuItem> getNewPageMenu(int page, int addAtTheEnd, boolean vanillaMenu){
        ArrayList<MenuItem> menus = new ArrayList<>();
        if(page == 0){
            MenuItem addTopBlank = getMenuItem(TR.tr("document.pageActions.addPages.blank.above"), vanillaMenu);
            MenuItem addTopConvert = getMenuItem(TR.tr("document.pageActions.addPages.converted.above"), vanillaMenu);
            MenuItem addTopPdf = getMenuItem(TR.tr("document.pageActions.addPages.pdf.above"), vanillaMenu);
            menus.add(addTopBlank);
            menus.add(addTopConvert);
            menus.add(addTopPdf);
            menus.add(new SeparatorMenuItem());
            
            addTopBlank.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newBlankPage(page, page));
            addTopConvert.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newConvertPage(page, page));
            addTopPdf.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newPdfPage(page));
        }
        
        MenuItem addBlank = getMenuItem(TR.tr("document.pageActions.addPages.blank"), vanillaMenu);
        MenuItem addConvert = getMenuItem(TR.tr("document.pageActions.addPages.converted"), vanillaMenu);
        MenuItem addTopPdf = getMenuItem(TR.tr("document.pageActions.addPages.pdf"), vanillaMenu);
        menus.add(addBlank);
        menus.add(addConvert);
        menus.add(addTopPdf);
        int index = (addAtTheEnd != 0) ? addAtTheEnd : page + 1;
        addBlank.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newBlankPage(page, index));
        addConvert.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newConvertPage(page, index));
        addTopPdf.setOnAction(ignored -> MainWindow.mainScreen.document.pdfPagesRender.editor.newPdfPage(index));
        
        return menus;
    }
    
    public static ArrayList<MenuItem> getCaptureMenu(PageRenderer page, boolean vanillaMenu){
        ArrayList<MenuItem> menus = new ArrayList<>();
        
        MenuItem capturePage = getMenuItem(TR.tr("document.pageActions.capture.allPage"), vanillaMenu);
        menus.add(capturePage);
        capturePage.setOnAction(ignored -> {
            MainWindow.mainScreen.document.pdfPagesRender.editor.capture(page.getPage(), null);
        });
        
        
        MenuItem captureSelection = getMenuItem(TR.tr("document.pageActions.capture.selectArea"), vanillaMenu);
        menus.add(captureSelection);
        captureSelection.setOnAction(ignored -> {
            PageZoneSelector recorder = page.getPageCursorRecord();
            recorder.setSelectionZoneType(PageZoneSelector.SelectionZoneType.PDF_ON_DARK);
            recorder.setupSelectionZoneOnce(positionDimensions -> {
                MainWindow.mainScreen.document.pdfPagesRender.editor.capture(page.getPage(), positionDimensions);
            });
            recorder.setShow(true);
        });
        
        
        if(MainWindow.mainScreen.document.totalPages != 1){
            MenuItem captureDocument = getMenuItem(TR.tr("document.pageActions.capture.allDocument"), vanillaMenu);
            menus.add(captureDocument);
            
            captureDocument.setOnAction(ignored -> {
                MainWindow.mainScreen.document.pdfPagesRender.editor.capture(-1, null);
            });
        }
        
        return menus;
    }
    
    private static MenuItem getMenuItem(String title, boolean vanillaItem){
        if(vanillaItem) return new MenuItem(title);
        else return new NodeMenuItem(title, false);
    }
    
    private Button getCustomButton(String path, String text){
        return getCustomButton(path, text, 0);
    }
    
    private Button getCustomButton(String path, String text, int rotate){
        Button button = new Button();
        button.setStyle("-fx-background-color: white;");
        PaneUtils.setHBoxPosition(button, 30, 30, 0);
        button.setCursor(Cursor.HAND);
        button.setGraphic(SVGPathIcons.generateImage(path, "#dc3e3e", 3, 30, 30, rotate));
        button.setTooltip(PaneUtils.genWrappedToolTip(text));
        return button;
    }
    
    public void updatePosition(){
        if(this.page == null) return;
        
        int buttonNumber = 7;
        double factor = StringUtils.clamp(.5 / MainWindow.mainScreen.getZoomFactor(), .5, 6);
        double height = 30 * buttonNumber;
        double width = 30;
    
        setLayoutY((height * factor - height) / 2d);
        setLayoutX(page.getWidth() + (width * factor - width) / 2d);
        
        setScaleX(factor);
        setScaleY(factor);
    }
    
    public void delete(){
        this.page = null;
        this.menu = null;
    
        ascendButton.setOnAction(null);
        descendButton.setOnAction(null);
        rotateLeftButton.setOnAction(null);
        rotateRightButton.setOnAction(null);
        deleteButton.setOnAction(null);
        newButton.setOnMouseClicked(null);
        captureButton.setOnMouseClicked(null);
        
    }
    
    public void updateVisibility(){
        ascendButton.setDisable(page.getPage() == 0);
        descendButton.setDisable(page.getPage() == MainWindow.mainScreen.document.totalPages - 1);
        deleteButton.setDisable(MainWindow.mainScreen.document.totalPages == 1);
    }
    
    // Hide pane but only if no menu are visible.
    public void checkMouseExited(){
        if(!menu.isShowing()){
            setVisible(false);
        }
    }
}
