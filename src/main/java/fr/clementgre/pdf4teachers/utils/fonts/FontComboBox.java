package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class FontComboBox extends ComboBox<String>{
    
    public FontComboBox(){
        super();
        if(FontUtils.isFontsLoaded()) updateFonts();
        setCellFactory((ListView<String> stringListView) -> new FontCell());
    }
    
    public void updateFonts(){
        setItems(FontUtils.getAllFonts());
        if(getSelectionModel().getSelectedItem() == null) getSelectionModel().select("Open Sans");
    }
    
    public static class FontCell extends ListCell<String>{
        @Override
        public void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            
            if(empty){
                setText(null);
                setGraphic(null);
            }else{
                setText(item);
                if(FontUtils.isDefaultFont(item)){
                    FontUtils.getFont(item, false, false, 14 * MainWindow.TEMP_SCALE);
                }
                setStyle("-fx-font: " + (14 * MainWindow.TEMP_SCALE) + " \"" + item + "\";");
            }
        }
    }
    
}
