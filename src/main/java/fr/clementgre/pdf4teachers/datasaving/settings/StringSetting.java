package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StringSetting extends Setting<String>{

    private StringProperty value;

    public StringSetting(String value, String icon, String path, String title, String description){
        super(icon, path, title, description);
        this.value = new SimpleStringProperty(value);

        this.value.addListener((observable, oldValue, newValue) -> {
            if(Main.settings != null) Main.settings.saveSettings();
        });
    }

    @Override
    public void setupMenuItem() {
        menuItem = MenuBar.createMenuItem(TR.trO(title), icon, null, TR.trO(description), true);
    }

    public StringProperty valueProperty() {
        return value;
    }
    @Override
    public String getValue() {
        return value.get();
    }
    @Override
    public void setValue(String value) {
        this.value.set(value);
    }
}
