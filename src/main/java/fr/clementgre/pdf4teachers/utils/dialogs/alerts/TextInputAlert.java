package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import jfxtras.styles.jmetro.Style;

public class TextInputAlert extends CustomAlert{
    
    private Label beforeText = new Label();
    private TextField input = new TextField();
    
    public TextInputAlert(String title, String header, String details){
        super(AlertType.CONFIRMATION, title, header, null);
    
        HBox box = new HBox();
        box.setPadding(new Insets(15));
        if(details != null){
            beforeText.setText(details);
            box.setSpacing(10);
            PaneUtils.setHBoxPosition(beforeText, 0, 25, 0);
            box.getChildren().addAll(beforeText, input);
        }else{
            box.getChildren().addAll(input);
        }
    
        StyleManager.putCustomStyle(getDialogPane(), "someDialogs.css");
        if(StyleManager.DEFAULT_STYLE == Style.LIGHT) StyleManager.putCustomStyle(getDialogPane(), "someDialogs-light.css");
        else StyleManager.putCustomStyle(getDialogPane(), "someDialogs-dark.css");
        
        getDialogPane().setContent(box);
    }
    
    public void setText(String text){
        input.setText(text);
    }
    public String getText(){
        return input.getText();
    }
    
}
