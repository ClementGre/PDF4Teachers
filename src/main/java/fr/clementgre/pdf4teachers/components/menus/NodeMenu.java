package fr.clementgre.pdf4teachers.components.menus;

import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class NodeMenu extends Menu{
    
    public final HBox node;
    public final Group group = new Group();
    public final HBox root = new HBox(group);
    
    public NodeMenu(HBox node){
        this(node, null);
    }
    public NodeMenu(String text){
        this(new HBox(), text);
    }
    public NodeMenu(HBox node, String text){
        setGraphic(root);
        
        this.node = node;
        group.getChildren().add(node);
        setup(text);
    }
    
    private void setup(String text){
        PaneUtils.setupScalingWithoutPadding(node, true);
        
        node.setStyle("-fx-padding: 7 12;");
        
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        //                        Image       , Text
        node.getChildren().addAll(new Region(), new Region());
        setName(text);
    }
    
    public void setImage(Node image){
        Pane pane = new Pane();
        pane.setStyle("-fx-padding: 0 8 0 0;"); // top - right - bottom - left
        
        pane.getChildren().add(image);
        node.getChildren().set(0, pane);
    }
    
    public void setName(String text){
        if(text != null && !text.isEmpty()){
            Label textLabel = new Label(text);
            textLabel.setStyle("-fx-font-size: 13; -fx-padding: 0 5 0 0;"); // top - right - bottom - left
            node.getChildren().set(1, textLabel);
        }
    }
    
    public void setToolTip(String toolTip){
        Tooltip toolTipUI = PaneUtils.genWrappedToolTip(toolTip);
        toolTipUI.setShowDuration(Duration.INDEFINITE);
        Tooltip.install(root, toolTipUI);
    }
    
    public void hideAll(){
        hideParent(this);
    }
    private static void hideParent(Menu item){
        if(item.getParentMenu() != null){
            hideParent(item.getParentMenu());
        }else if(item.getParentPopup() != null){
            item.getParentPopup().hide();
        }else{
            item.hide();
        }
    }

}
