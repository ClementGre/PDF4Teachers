package fr.clementgre.pdf4teachers.components.menus;

import fr.clementgre.pdf4teachers.interfaces.windows.settings.SettingsWindow;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.input.MouseEvent;

public class EmptyMenu extends Menu{
    
    private static final String NAME_STYLE = "-fx-padding: 0 7;";
    
    private final Label name;
    
    public EmptyMenu(String text, MenuBar menuBar){
        super();
    
    
        name = new Label(text);
        name.setAlignment(Pos.CENTER);
        name.setOnMouseClicked(e -> new SettingsWindow());
        name.prefHeightProperty().bind(menuBar.heightProperty());
        name.setStyle(NAME_STYLE);
        
        setGraphic(name);
        
    }
    
    public void setEmptyMenuStyle(String style){
        name.setStyle(NAME_STYLE + style);
    }
    
    public void setOnClick(EventHandler<? super MouseEvent> e){
        name.setOnMouseClicked(e);
    }
}
