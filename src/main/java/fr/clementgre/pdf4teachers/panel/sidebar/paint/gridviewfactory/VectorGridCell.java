package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.exceptions.PathParseException;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    private final boolean hasContextMenu;
    public VectorGridCell(boolean favorite, boolean hasContextMenu){
        this.favorite = favorite;
        this.hasContextMenu = hasContextMenu;
    
        root.prefWidthProperty().bind(widthProperty().subtract(2*PADDING));
        root.prefHeightProperty().bind(heightProperty().subtract(2*PADDING));
        root.maxWidthProperty().bind(widthProperty().subtract(2*PADDING));
        root.maxHeightProperty().bind(heightProperty().subtract(2*PADDING));
        root.setTranslateX(PADDING);
        //root.setTranslateY(PADDING);
    
        // Prevent cell from taking the shape of the SVGPath
        root.getStyleClass().add(JMetroStyleClass.BACKGROUND);
    
        shadow.setColor(Color.TRANSPARENT);
        shadow.setSpread(.90);
        shadow.setOffsetY(0);
        shadow.setOffsetX(0);
        shadow.setRadius(2);
        setEffect(shadow);
    
        setOnMouseEntered((e) -> {
            if(!getItem().isFake()) shadow.setColor(Color.web("#0078d7"));
            else{
                if(Main.settings.darkTheme.getValue()) setEffect(new ColorAdjust(0, 0, .3, 0));
                else setEffect(new ColorAdjust(0, 0, -.3, 0));
            }
        });
        setOnMouseExited((e) -> {
            if(!getItem().isFake()) shadow.setColor(Color.TRANSPARENT);
            else setEffect(null);
        });
    
        if(hasContextMenu) NodeMenuItem.setupMenu(menu);
    }
    
    @Override
    protected void updateItem(VectorGridElement item, boolean empty) {
        super.updateItem(item, empty);
        
        if(empty || item == null){
            setGraphic(null);
            setOnMouseClicked(null);
            setContextMenu(null);
            setOnDragDetected(null);
        }else if(item.isFake()){
            setGraphic(null);
            setOnMouseClicked(null);
            setContextMenu(null);
            setOnDragDetected(null);
    
            Region icon = SVGPathIcons.generateImage(SVGPathIcons.PENCIL, "#0078d7", (int) getHeight()/2, (int) getWidth()/2);
            icon.maxWidthProperty().bind(icon.heightProperty());
            
            Text label = new Text(TR.tr("actions.new"));
            label.setStyle("-fx-font-size: " + (12 * getWidth()/65) + "; -fx-background-color: red;");
            label.setTextAlignment(TextAlignment.CENTER);
            
            VBox root = new VBox(icon, label);
            root.setAlignment(Pos.CENTER);
            
            setGraphic(root);
            setOnMouseClicked((e) -> {
                MainWindow.paintTab.select();
                Platform.runLater(() -> MainWindow.paintTab.newVectorDrawing());
            });
        }else{
    
            // SETUP SVG
            double svgWidth = getGridView().getCellWidth()-2*PADDING;
            if(item.getLastDisplayWidth() != ((int) svgWidth)){
                item.layoutSVGPath(svgWidth);
            }
            
            // MENU
            if(hasContextMenu){
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
            }
            
    
            root.getChildren().setAll(item.getSvgPath());
            
            setGraphic(root);
    
            setOnMouseClicked((e) -> {
                if(e.getButton() == MouseButton.PRIMARY){
                    if(e.getClickCount() >= 2){
                        item.addToDocument(e.isShiftDown());
                        updateListsSort();
                    }else if(e.getClickCount() == 1){
                        item.setAsToPlaceElement(e.isShiftDown());
                    }
                }
            });
            setOnDragDetected(e -> {
                Dragboard dragboard = startDragAndDrop(TransferMode.COPY);
                try{
                    Image snapshot = snapshot(item.getVectorData());
                    dragboard.setDragView(snapshot, snapshot.getWidth()/2, snapshot.getHeight()/2);
                }catch(PathParseException ex){
                    ex.printStackTrace();
                }
                
        
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.put(Main.INTERNAL_FORMAT, PaintTab.PAINT_ITEM_DRAG_KEY);
                dragboard.setContent(clipboardContent);
        
                PaintTab.draggingItem = item;
                e.consume();
            });
        }
    }
    
    private Image snapshot(VectorData data) throws PathParseException{
        
        SVGPath noScaledSvgPath = new SVGPath();
        noScaledSvgPath.setContent(data.getPath());
        double ratio = noScaledSvgPath.getLayoutBounds().getWidth() / noScaledSvgPath.getLayoutBounds().getHeight();
        
        float elementWidth = StringUtils.clamp(data.getWidth() / Element.GRID_WIDTH * PageRenderer.PAGE_WIDTH, 25, 100);
        float elementHeight = data.getHeight() * elementWidth/data.getWidth();
        double graphicsWidth = elementWidth + VectorElement.getClipPadding(data)*2;
        double graphicsHeight = elementHeight + VectorElement.getClipPadding(data)*2;
        
        if(data.getRepeatMode() == GraphicElement.RepeatMode.CROP){
            if(graphicsWidth > graphicsHeight * ratio){ // Crop Y
                elementHeight = (float) (graphicsWidth / ratio);
            }else{ // Crop X
                elementWidth = (float) (graphicsHeight * ratio);
            }
        }
    
        SVGPath svgPath = new SVGPath();
        if(data.getRepeatMode() == GraphicElement.RepeatMode.MULTIPLY){
            svgPath.setContent(VectorElement.getRepeatedPath(data.getPath(), noScaledSvgPath, elementWidth, elementHeight, (float) VectorElement.getSVGPadding(data),
                    data.isInvertX(), data.isInvertY(), data.getArrowLength(), -1));
        }else{
            svgPath.setContent(VectorElement.getScaledPath(data.getPath(), noScaledSvgPath, elementWidth, elementHeight, (float) VectorElement.getSVGPadding(data),
                    data.isInvertX(), data.isInvertY(), data.getArrowLength(), -1));
        }
    
        // SVGPath specs
        svgPath.setStroke(data.getStroke());
        svgPath.setStrokeWidth(data.getStrokeWidth());
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
        if(data.isDoFill()) svgPath.setFill(data.getFill());
        else svgPath.setFill(null);
        
        // SNAPSHOT
        SnapshotParameters sn = new SnapshotParameters();
        sn.setFill(Color.TRANSPARENT);
        return svgPath.snapshot(sn, null);
    }
    
    public static void updateListsSort(){
        MainWindow.paintTab.favouriteVectors.getList().getSortManager().simulateCall();
        MainWindow.paintTab.lastVectors.getList().getSortManager().simulateCall();
    }
}
