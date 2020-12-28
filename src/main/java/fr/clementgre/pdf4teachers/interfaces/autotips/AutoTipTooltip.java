package fr.clementgre.pdf4teachers.interfaces.autotips;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javafx.util.Duration;

import java.awt.*;

public class AutoTipTooltip extends Tooltip{

    private String name;
    private String actionKey;
    private String prerequisiteKey;
    private String objectWhereDisplay;

    private Button okButton = new Button(TR.tr("OK"));

    public AutoTipTooltip(String name, String actionKey, String prerequisiteKey, String objectWhereDisplay, String text) {
        super(Main.isOSX()
                ? TR.tr(text).replace("Ctrl+", "Cmd+").replace("ctrl+", "cmd+")
                : TR.tr(text));
        this.name = name;
        this.actionKey = actionKey;
        this.prerequisiteKey = prerequisiteKey;
        this.objectWhereDisplay = objectWhereDisplay;

        setAutoHide(false);
        setOpacity(0.95);
        setHideDelay(Duration.INDEFINITE);

        setMaxWidth(300);
        setWrapText(true);

        Pane graphic = new Pane();
        graphic.getChildren().add(okButton);
        graphic.setPadding(new Insets(0, 10, 0, 0));
        setGraphic(graphic);

        getStyleClass().add("tooltip-autotip");

        okButton.setOnMouseClicked((e) -> {
            hide();
            AutoTipsManager.removeTip(name);
        });

    }


    @Override
    public void show(Window owner){
        if(owner == null) return;
        if(!owner.isFocused()) return;

        if(objectWhereDisplay.isEmpty()){

            int x = (int) MouseInfo.getPointerInfo().getLocation().getX();
            int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
            super.show(owner, x, y);

        }else switch(objectWhereDisplay){
            case "mainscreen" -> showOnPane(MainWindow.mainScreen);
            case "leftbar" -> showOnPane(MainWindow.leftBar);
        }
    }

    private void showOnPane(Region region){
        if(region == null) return;
        final Scene scene = region.getScene();
        if((scene == null) || (scene.getWindow() == null)) return;

        super.show(region, Main.window.getX() + region.getWidth()/2, Main.window.getY() + region.getHeight()/2);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getActionKey() {
        return actionKey;
    }
    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }
    public String getPrerequisiteKey() {
        return prerequisiteKey;
    }
    public void setPrerequisiteKey(String prerequisiteKey) {
        this.prerequisiteKey = prerequisiteKey;
    }
    public String getObjectWhereDisplay() {
        return objectWhereDisplay;
    }
    public void setObjectWhereDisplay(String objectWhereDisplay) {
        this.objectWhereDisplay = objectWhereDisplay;
    }
}
