package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.RadioMenuItem;

public class BooleanSetting extends Setting<Boolean>{

    private BooleanProperty value;

    public BooleanSetting(Boolean value, String icon, String path, String title, String description){
        super(icon, path, title, description);
        this.value = new SimpleBooleanProperty(value);

        this.value.addListener((observable, oldValue, newValue) -> {
            if(Main.settings != null) Main.settings.saveSettings();
        });
    }

    @Override
    public void setupMenuItem() {
        menuItem = MenuBar.createRadioMenuItem(TR.trO(title), icon, TR.trO(description), true);
        if(menuItem instanceof RadioMenuItem){
            ((RadioMenuItem) menuItem).setSelected(value.get());
            this.value.bindBidirectional(((RadioMenuItem) menuItem).selectedProperty());
        }else if(menuItem instanceof NodeRadioMenuItem){
            ((NodeRadioMenuItem) menuItem).setSelected(value.get());
            this.value.bindBidirectional(((NodeRadioMenuItem) menuItem).selectedProperty());
        }
    }

    public BooleanProperty valueProperty() {
        return value;
    }
    @Override
    public Boolean getValue() {
        return value.get();
    }
    @Override
    public void setValue(Boolean value) {
        this.value.setValue(value);
    }
}
