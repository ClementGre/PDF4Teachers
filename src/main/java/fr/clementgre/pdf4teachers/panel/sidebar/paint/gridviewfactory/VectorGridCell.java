package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.controlsfx.control.GridCell;

public class VectorGridCell extends GridCell<VectorGridElement>{
    
    private final Pane root = new Pane();
    private final DropShadow shadow = new DropShadow();
    
    private final ContextMenu menu = new ContextMenu();
    private final NodeMenuItem addNLink = new NodeMenuItem(TR.tr("textTab.listMenu.addNLink"));
    private final NodeMenuItem removeItem = new NodeMenuItem(TR.tr("actions.remove"));
    private final NodeMenuItem addToFavorites = new NodeMenuItem(TR.tr("elementMenu.addToFavouriteList"));
    
    public static final int PADDING = 2;
    
    private final VectorGridView gridView;
    public VectorGridCell(VectorGridView gridView){
        this.gridView = gridView;
    
        root.prefWidthProperty().bind(widthProperty().subtract(2*PADDING));
        root.prefHeightProperty().bind(heightProperty().subtract(2*PADDING));
        root.setTranslateX(PADDING);
        root.setTranslateY(PADDING);
        
        shadow.setColor(Color.web("#0078d7"));
        shadow.setSpread(.90);
        shadow.setOffsetY(0);
        shadow.setOffsetX(0);
        shadow.setRadius(0);
        setEffect(shadow);
        
        setOnMouseEntered((e) -> {
            shadow.setRadius(2);
        });
        setOnMouseExited((e) -> {
            shadow.setRadius(0);
        });
    }
    
    @Override
    protected void updateItem(VectorGridElement item, boolean empty) {
        super.updateItem(item, empty);
        
        if(empty){
            setGraphic(null);
            setOnMouseClicked(null);
        }else{
    
            // SETUP SVG
            double svgWidth = gridView.getCellWidth()-2*PADDING;
            if(item.getLastRenderWidth() != ((int) svgWidth)){
                item.renderSvgPath((int) svgWidth);
            }
            
            // MENU
            setContextMenu(menu);
            if(item.isFavorite()){
                menu.getItems().setAll(addNLink, removeItem);
            }else{
                menu.getItems().setAll(addNLink, removeItem, addToFavorites);
            }
            menu.setOnShowing((e) -> {
                addNLink.setDisable(!MainWindow.mainScreen.hasDocument(false));
    
                addNLink.setOnAction((event) -> item.addToDocument(true));
                removeItem.setOnAction((event) -> item.removeFromList(gridView));
                if(!item.isFavorite()) addToFavorites.setOnAction((event) -> item.addToFavorite());
            });
    
            root.getChildren().setAll(item.getSvgPath());
            setStyle("-fx-background-color: rgba(255, 555, 555, .5);"); // Prevent cell from taking the shape of the SVGPath
            setGraphic(root);
    
            System.out.println("rootWidt=" + root.getWidth() + ", cellWidth=" + gridView.getCellWidth() + ", currentCellWidth=" + getWidth());
            setOnMouseClicked((e) -> {
                if(e.getButton() == MouseButton.PRIMARY){
                    if(e.getClickCount() >= 2){
                        item.addToDocument(false);
                        updateListsSort();
                    }else if(e.getClickCount() == 1){
                        item.setAsToPlaceElement(false);
                    }
                }
            });
        }
    }
    
    public static void updateListsSort(){
        MainWindow.paintTab.favouriteVectors.getList().getSortManager().simulateCall();
        MainWindow.paintTab.lastVectors.getList().getSortManager().simulateCall();
    }
}
