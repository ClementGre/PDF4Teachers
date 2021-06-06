package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

public class VectorElementPageDrawer extends Pane{
    
    private ContextMenu menu = new ContextMenu();
    private PageRenderer page;
    
    private VectorElement vector;
    private SVGPath svgPath = new SVGPath();
    
    public VectorElementPageDrawer(PageRenderer page){
        this.page = page;
        setup();
        updateVisibility();
    }
    
    private void setup(){
        
        // UI
        setBorder(new Border(new BorderStroke(Color.color(255 / 255.0, 50 / 255.0, 50 / 255.0),
                BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, new BorderWidths(1.5))));
        
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
        
        // MENU
        NodeMenuItem quitEditMode = new NodeMenuItem("actions.quitEditMode", false);
        NodeMenuItem undo = new NodeMenuItem(TR.tr("paintTab.vectorElements.undo"), false);
        
        quitEditMode.setOnAction((e) -> {
            quitEditMode();
        });
        undo.setOnAction((e) -> {
            MainWindow.paintTab.vectorUndoPath.fire();
        });
        
        menu.getItems().addAll(quitEditMode, undo);
        
        // Bindings
        prefWidthProperty().bind(page.widthProperty());
        prefHeightProperty().bind(page.heightProperty());
        
        // Events
        setOnMousePressed((e) -> {
            e.consume();
        });
        setOnMouseReleased((e) -> {
            e.consume();
        });
        setOnMouseClicked((e) -> {
            e.consume();
            if(e.getButton() == MouseButton.PRIMARY & e.getClickCount() == 2){
                if(vector != null) vector.quitEditMode();
                else quitEditMode();
            }
        });
        setOnMouseDragged((e) -> {
            e.consume();
        });
    }
    public void delete(){
        this.page = null;
        this.menu = null;
    
        prefWidthProperty().unbind();
        prefHeightProperty().unbind();
    }
    
    public void updateVisibility(){
        if(!page.isVisible() && isEditMode()){
            quitEditMode();
        }
    }
    
    public void enterEditMode(VectorElement vector){
        if(isEditMode()) quitEditMode();
        page.getChildren().add(this);
        toFront();
        requestFocus();
        
        this.vector = vector;
        vector.setVisible(false);
        vector.formatNoScaledSvgPathToPage();
    
        svgPath.setLayoutX(vector.getLayoutX() + vector.getSVGPadding());
        svgPath.setLayoutY(vector.getLayoutY() + vector.getSVGPadding());
    
        svgPath.contentProperty().bindBidirectional(vector.getNoScaledSvgPath().contentProperty());
        svgPath.fillProperty().bind(vector.getSvgPath().fillProperty());
        svgPath.strokeProperty().bind(vector.getSvgPath().strokeProperty());
        svgPath.strokeWidthProperty().bind(vector.getSvgPath().strokeWidthProperty());
        svgPath.strokeMiterLimitProperty().bind(vector.getSvgPath().strokeMiterLimitProperty());
        
        getChildren().setAll(svgPath);
    }
    public void quitEditMode(){
        getChildren().clear();
        page.getChildren().remove(this);
        
        if(vector != null){
            vector.setVisible(true);
            svgPath.contentProperty().unbindBidirectional(vector.getNoScaledSvgPath().contentProperty());
        }else{
            svgPath.contentProperty().unbind();
        }
    
        
        svgPath.fillProperty().unbind();
        svgPath.strokeProperty().unbind();
        svgPath.strokeWidthProperty().unbind();
        svgPath.strokeMiterLimitProperty().unbind();
        
        this.vector = null;
    }
    
    public void onCreateCurve(){
    
    }
    
    public boolean isEditMode(){
        return page.getChildren().contains(this);
    }
    private boolean isPointMode(){
        return MainWindow.paintTab.vectorModePoint.isSelected();
    }
    private boolean isLineMode(){
        return MainWindow.paintTab.vectorStraightLineMode.isSelected();
    }
    
    public VectorElement getVectorElement(){
        return vector;
    }
}
