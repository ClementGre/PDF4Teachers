package fr.clementgre.pdf4teachers.components.menus;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class NodeMenuItem extends CustomMenuItem{
    
    public final HBox node;
    public final Group group = new Group();
    public final HBox root = new HBox(group);
    
    public NodeMenuItem(HBox node){
        this(node, null, true);
    }
    public NodeMenuItem(){
        this(new HBox(), null, true);
    }
    public NodeMenuItem(String text){
        this(new HBox(), text, true);
    }
    public NodeMenuItem(HBox node, String text){
        this(node, text, true);
    }
    public NodeMenuItem(HBox node, String text, boolean hideOnClick){
        setContent(root);
        setHideOnClick(hideOnClick);
        
        this.node = node;
        group.getChildren().add(node);
        setup(text);
    }
    
    private void setup(String text){
        getStyleClass().add("custom-menu-item");
        PaneUtils.setupScalingWithoutPadding(node, true);
    
        node.setStyle("-fx-padding: " + 7 + " " + 12 + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        //                             LeftData    , Image       , Text        , spacer, Accelerator
        getNode().getChildren().addAll(new Region(), new Region(), new Region(), spacer, new Region());
        setName(text);
    }
    
    public void setLeftData(Node data){
        Pane pane = new Pane();
        pane.setStyle("-fx-font-size: 13; -fx-padding: 0 8 0 0;"); // top - right - bottom - left
        
        pane.getChildren().add(data);
        getNode().getChildren().set(0, pane);
    }
    
    public void setFalseLeftData(){
        Region spacer = new Region();
        spacer.setPrefWidth(16+5);
        getNode().getChildren().set(0, spacer);
    }
    
    public void setImage(Node image){
        Pane pane = new Pane();
        pane.setStyle("-fx-padding: 0 8 0 0;"); // top - right - bottom - left
        
        pane.getChildren().add(image);
        getNode().getChildren().set(1, pane);
    }
    
    public void setName(String text){
        if(text != null && !text.isEmpty()){
            Label textLabel = new Label(text);
            textLabel.setStyle("-fx-font-size: 13; -fx-padding: 0 5 0 0;"); // top - right - bottom - left
            getNode().getChildren().set(2, textLabel);
        }
    }
    
    public void setKeyCombinaison(KeyCombination keyCombinaison){
        
        Label acceleratorLabel = new Label(keyCombinaison.getDisplayText());
        acceleratorLabel.setStyle("-fx-font-size: 13; -fx-padding: 0 0 0 25; -fx-text-fill: #d0d0d0;");  // top - right - bottom - left
        getNode().getChildren().set(4, acceleratorLabel);
        
        setAccelerator(keyCombinaison);
    }
    
    public void setToolTip(String toolTip){
        Tooltip toolTipUI = PaneUtils.genWrappedToolTip(toolTip);
        toolTipUI.setShowDuration(Duration.INDEFINITE);
        Tooltip.install(root, toolTipUI);
    }
    
    // GETTERS / SETTERS
    
    public HBox getNode(){
        return node;
    }
    
    // STATIC UTILS
    
    public static void setupMenu(Menu menu){
        
        AtomicBoolean firstRun = new AtomicBoolean(true);
        menu.setOnShown(e -> {
            if(firstRun.get()){
                setupMenuNow(menu);
                firstRun.set(false);
            }
        });
    }
    public static void setupMenuNow(Menu menu){
        
        double maxWidth = 0;
        int extra = 0;
        for(MenuItem item : menu.getItems()){
            if(item instanceof NodeMenuItem nodeItem){
                if(nodeItem.getNode().getWidth() > maxWidth)
                    maxWidth = nodeItem.getNode().getWidth();
            }else if(item instanceof Menu){
                extra = (int) (20*Main.settings.zoom.getValue()); // Menus has a little Arrow, this add some px
                
                if(item.getStyleableNode() != null){
                    Node arrow = item.getStyleableNode().lookup(".right-container > .arrow");
                    if(arrow instanceof Region region){
                        region.setScaleX(Main.settings.zoom.getValue());
                        region.setScaleY(Main.settings.zoom.getValue());
                    }
                }
            }
        }
        for(MenuItem item : menu.getItems()){
            if(item instanceof NodeMenuItem nodeItem){
                nodeItem.getNode().setPrefWidth((maxWidth + extra));
            }
        }
    }
    
    public static void setupMenu(ContextMenu menu){
        menu.setOnShown(e -> {
            setupMenuNow(menu);
        });
    }
    public static void setupMenuNow(ContextMenu menu){
        double maxWidth = 0;
        int extra = 0;
        for(MenuItem item : menu.getItems()){
            if(item instanceof NodeMenuItem nodeItem){
                if(nodeItem.getNode().getWidth() > maxWidth)
                    maxWidth = nodeItem.getNode().getWidth();
            }else if(item instanceof Menu){
                extra = (int) (20*Main.settings.zoom.getValue()); // Menus has a little Arrow, this add some px
            
                if(item.getStyleableNode() != null){
                    Node arrow = item.getStyleableNode().lookup(".right-container > .arrow");
                    if(arrow instanceof Region region){
                        region.setScaleX(Main.settings.zoom.getValue());
                        region.setScaleY(Main.settings.zoom.getValue());
                    }
                }
            }
        }
        for(MenuItem item : menu.getItems()){
            if(item instanceof NodeMenuItem nodeItem){
                nodeItem.getNode().setPrefWidth((maxWidth + extra));
            }
        }
    }
}
