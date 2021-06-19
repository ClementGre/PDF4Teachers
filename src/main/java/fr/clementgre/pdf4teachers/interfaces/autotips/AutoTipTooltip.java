package fr.clementgre.pdf4teachers.interfaces.autotips;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import org.controlsfx.control.PopOver;

import java.awt.MouseInfo;

public class AutoTipTooltip extends PopOver{
    
    private String name;
    private String actionKey;
    private String prerequisiteKey;
    private String objectWhereDisplay;
    
    private final Label text = new Label();
    
    private boolean closedByAutoHide = false;
    private JMetro jMetro;
    
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
        setConsumeAutoHidingEvents(false); // When click on stage, the click event will not be consumed and can cause issues. (see #98)
        
        setTitle(TR.tr("autoTips.title"));
        text.setMaxWidth(300);
        text.setWrapText(true);
        
        HBox graphic = new HBox();
        graphic.getChildren().add(text);
        setContentNode(graphic);
        graphic.getStyleClass().addAll("tooltip-autotip-pane", "content-pane");
        getStyleClass().add("tooltip-autotip");
    
        setOnAutoHide((e) -> {
            closedByAutoHide = true;
        });
        setOnHidden((e) -> {
            if(!closedByAutoHide){
                AutoTipsManager.removeTip(name);
            }
        });
    }
    
    
    public void showAuto(Stage owner){
        if(owner == null) return;
        if(!owner.isFocused()) return;
        if(isShowing()) return;
        closedByAutoHide = false;
    
        jMetro = StyleManager.putStyle(getRoot(), Style.DEFAULT, jMetro);
        getRoot().getTransforms().setAll(new Scale(Main.settings.zoom.getValue(), Main.settings.zoom.getValue(), 0, 0));
        
        if(objectWhereDisplay.isEmpty()){
            
            int x = (int) MouseInfo.getPointerInfo().getLocation().getX();
            int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
            show(owner.getScene().getFocusOwner(), x, y);
            
        }else if(objectWhereDisplay.equals("auto")){
            show(owner.getScene().getFocusOwner());
        }else{
            switch(objectWhereDisplay){
                case "mainscreen" -> {
                    setArrowSize(0);
                    showOnPane(MainWindow.mainScreen, owner);
                }
                case "leftbar" -> {
                    setArrowSize(0);
                    showOnPane(MainWindow.leftBar, owner);
                }
                case "gallerycombobox" -> {
                    setArrowLocation(ArrowLocation.TOP_CENTER);
                    show(MainWindow.paintTab.galleryWindow.filter);
                }
                case "vectorEditModeButton" -> {
                    show(MainWindow.paintTab.vectorEditMode);
                }
            }
        }
    }
    
    private void showOnPane(Region region, Stage owner){
        if(region == null) return;
        final Scene scene = region.getScene();
        if((scene == null) || (scene.getWindow() == null)) return;
        
        super.show(region, owner.getX()*Main.settings.zoom.getValue() + region.getWidth() / 2, owner.getY()*Main.settings.zoom.getValue() + region.getHeight() / 2);
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
