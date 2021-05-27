package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.utils.PaneUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;

public class DoubleInputAlert extends CustomAlert{
    
    private final Label beforeText = new Label();
    private final Spinner<Double> input;
    
    public DoubleInputAlert(double min, double max, double val, double step, String title, String header, String details){
        super(AlertType.CONFIRMATION, title, header, null);
    
        input = new Spinner<>(min, max, val, step);
        input.setEditable(true);
        
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
        
        /*StyleManager.putCustomStyle(getDialogPane(), "someDialogs.css");
        if(StyleManager.DEFAULT_STYLE == Style.LIGHT) StyleManager.putCustomStyle(getDialogPane(), "someDialogs-light.css");
        else StyleManager.putCustomStyle(getDialogPane(), "someDialogs-dark.css");*/
        
        getDialogPane().setContent(box);
    }
    
    public void setValue(double value){
        input.getValueFactory().setValue(value);
    }
    public Double getValue(){
        return input.getValue();
    }
    
}
