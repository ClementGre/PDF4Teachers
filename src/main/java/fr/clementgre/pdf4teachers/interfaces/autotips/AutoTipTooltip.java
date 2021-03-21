package fr.clementgre.pdf4teachers.interfaces.autotips;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.awt.*;

public class AutoTipTooltip extends PopOver{
    
    private String name;
    private String actionKey;
    private String prerequisiteKey;
    private String objectWhereDisplay;
    
    private Label text = new Label();
    
    private boolean closedByAutoHide = false;
    
    public AutoTipTooltip(String name, String actionKey, String prerequisiteKey, String objectWhereDisplay){
        String contentText = Main.isOSX()
                ? TR.tr("autoTips." + name).replace("Ctrl+", "Cmd+").replace("ctrl+", "cmd+")
                : TR.tr("autoTips." + name);
        text.setText(contentText);
        
        this.name = name;
        this.actionKey = actionKey;
        this.prerequisiteKey = prerequisiteKey;
        this.objectWhereDisplay = objectWhereDisplay;
        
        setAutoHide(true);
        setDetachable(false);
        setHeaderAlwaysVisible(true);
        setArrowIndent(0);
        setCornerRadius(10);
        setTitle(TR.tr("autoTips.title"));
        
        text.setMaxWidth(300);
        text.setWrapText(true);
        
        HBox graphic = new HBox();
        graphic.getChildren().add(text);
        setContentNode(graphic);
        graphic.getStyleClass().add("tooltip-autotip-pane");
        getStyleClass().add("tooltip-autotip");
        
        setOnAutoHide((e) -> {
            closedByAutoHide = true;
        });
        setOnHidden((e) -> {
            if(!closedByAutoHide) AutoTipsManager.removeTip(name);
        });
        
    }
    
    
    public void showAuto(Window owner){
        if(owner == null) return;
        if(!owner.isFocused()) return;
        closedByAutoHide = false;
        StyleManager.putStyle(getRoot(), Style.DEFAULT);
        
        if(objectWhereDisplay.isEmpty()){
            
            int x = (int) MouseInfo.getPointerInfo().getLocation().getX();
            int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
            show(owner.getScene().getFocusOwner(), x, y);
            
        }else if(objectWhereDisplay.equals("auto")){
            show(owner.getScene().getFocusOwner());
        }else{
            setArrowSize(0);
            switch(objectWhereDisplay){
                case "mainscreen" -> showOnPane(MainWindow.mainScreen);
                case "leftbar" -> showOnPane(MainWindow.leftBar);
            }
        }
    }
    
    private void showOnPane(Region region){
        if(region == null) return;
        final Scene scene = region.getScene();
        if((scene == null) || (scene.getWindow() == null)) return;
        
        super.show(region, Main.window.getX() + region.getWidth() / 2, Main.window.getY() + region.getHeight() / 2);
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getActionKey(){
        return actionKey;
    }
    
    public void setActionKey(String actionKey){
        this.actionKey = actionKey;
    }
    
    public String getPrerequisiteKey(){
        return prerequisiteKey;
    }
    
    public void setPrerequisiteKey(String prerequisiteKey){
        this.prerequisiteKey = prerequisiteKey;
    }
    
    public String getObjectWhereDisplay(){
        return objectWhereDisplay;
    }
    
    public void setObjectWhereDisplay(String objectWhereDisplay){
        this.objectWhereDisplay = objectWhereDisplay;
    }
}
