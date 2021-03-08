package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.RadioMenuItem;

public class IntSetting extends Setting<Integer>{

    private IntegerProperty value;
    private boolean hideCheck = false;

    public IntSetting(Integer value, String icon, String path, String title, String description){
        super(icon, path, title, description);
        this.value = new SimpleIntegerProperty(value);

        this.value.addListener((observable, oldValue, newValue) -> {
            if(Main.settings != null) Main.settings.saveSettings();
        });
    }
    public IntSetting(Integer value, String icon, String path, String title, String description, boolean hideCheck){
        super(icon, path, title, description);
        this.value = new SimpleIntegerProperty(value);
        this.hideCheck = hideCheck;

        this.value.addListener((observable, oldValue, newValue) -> {
            if(Main.settings != null){
                Main.settings.saveSettings();
                if(menuItem instanceof RadioMenuItem){
                    ((RadioMenuItem) menuItem).setSelected(this.value.get() != -1);
                }else if(menuItem instanceof NodeRadioMenuItem){
                    ((NodeRadioMenuItem) menuItem).setSelected(this.value.get() != -1);
                }
            }
        });
    }

    @Override
    public void setupMenuItem(){
        if(hideCheck){
            menuItem = MenuBar.createMenuItem(TR.tr(title), icon, null, TR.tr(description), true);
        }else{
            menuItem = MenuBar.createRadioMenuItem(TR.tr(title), icon, TR.tr(description), false);
            if(menuItem instanceof RadioMenuItem){
                ((RadioMenuItem) menuItem).setSelected(this.value.get() != -1);
            }else if(menuItem instanceof NodeRadioMenuItem){
                ((NodeRadioMenuItem) menuItem).setSelected(this.value.get() != -1);
            }
        }

    }

    public IntegerProperty valueProperty() {
        return value;
    }
    @Override
    public Integer getValue() {
        return value.get();
    }
    @Override
    public void setValue(Integer value) {
        this.value.setValue(value);
    }

    public void setRadioSelected(boolean selected){
        if(menuItem instanceof RadioMenuItem){
            ((RadioMenuItem) menuItem).setSelected(selected);
        }else if(menuItem instanceof NodeRadioMenuItem){
            ((NodeRadioMenuItem) menuItem).setSelected(selected);
        }
    }
}
