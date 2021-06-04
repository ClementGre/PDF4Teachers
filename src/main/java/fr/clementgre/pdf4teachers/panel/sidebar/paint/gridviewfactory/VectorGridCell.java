package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jfxtras.styles.jmetro.JMetroStyleClass;
import org.controlsfx.control.GridCell;

public class VectorGridCell extends GridCell<VectorGridElement>{
    
    private final Pane root = new Pane();
    private final DropShadow shadow = new DropShadow();
    
    private final ContextMenu menu = new ContextMenu();
    private final NodeMenuItem addNLink = new NodeMenuItem(TR.tr("textTab.listMenu.addNLink"), false);
    private final NodeMenuItem removeItem = new NodeMenuItem(TR.tr("actions.remove"), false);
    private final NodeMenuItem addToFavorites = new NodeMenuItem(TR.tr("elementMenu.addToFavouriteList"), false);
    private final NodeMenuItem addToLast = new NodeMenuItem(TR.tr("elementMenu.addToPreviousList"), false);
    
    public static final int PADDING = 2;
    
    private final boolean favorite;
    public VectorGridCell(boolean favorite){
        this.favorite = favorite;
    
        root.prefWidthProperty().bind(widthProperty().subtract(2*PADDING));
        root.prefHeightProperty().bind(heightProperty().subtract(2*PADDING));
        root.maxWidthProperty().bind(widthProperty().subtract(2*PADDING));
        root.maxHeightProperty().bind(heightProperty().subtract(2*PADDING));
        root.setTranslateX(PADDING);
        //root.setTranslateY(PADDING);
    
        // Prevent cell from taking the shape of the SVGPath
        root.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        
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
    
        NodeMenuItem.setupMenu(menu);
    }
    
    @Override
    protected void updateItem(VectorGridElement item, boolean empty) {
        super.updateItem(item, empty);
        
        if(empty || item == null){
            setGraphic(null);
            setOnMouseClicked(null);
            setContextMenu(null);
        }else{
    
            // SETUP SVG
            double svgWidth = getGridView().getCellWidth()-2*PADDING;
            if(item.getLastDisplayWidth() != ((int) svgWidth)){
                item.layoutSVGPath(svgWidth);
            }
            
            // MENU
            setContextMenu(menu);
            if(favorite){
                menu.getItems().setAll(addNLink, removeItem, addToLast);
            }else{
                menu.getItems().setAll(addNLink, removeItem, addToFavorites);
            }
            menu.setOnShowing((e) -> {
                addNLink.setDisable(!MainWindow.mainScreen.hasDocument(false));
                addNLink.setOnAction((event) -> item.addToDocument(true));
                
                removeItem.setOnAction((event) -> item.removeFromList((VectorGridView) getGridView()));
                addToFavorites.setOnAction((event) -> item.addToFavorite((VectorGridView) getGridView()));
                addToLast.setOnAction((event) -> item.addToLast((VectorGridView) getGridView()));
            });
    
            root.getChildren().setAll(item.getSvgPath());
            
            setGraphic(root);
    
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
