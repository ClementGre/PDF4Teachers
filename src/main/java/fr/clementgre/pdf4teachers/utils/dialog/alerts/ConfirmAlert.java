package fr.clementgre.pdf4teachers.utils.dialog.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.ButtonType;

public class ConfirmAlert extends CustomAlert{
    
    public ConfirmAlert(boolean irreversible, String header){
        super(AlertType.CONFIRMATION, TR.tr("dialog.confirmation.title"), header/* + header + header + header*/);
        if(irreversible) setContentText(TR.tr("dialog.confirmation.irreversible"));
        
        addConfirmButton(ButtonPosition.DEFAULT);
        addCancelButton(ButtonPosition.CLOSE);
        
    }
    
    public boolean execute(){
        ButtonType result = getShowAndWait();
        if(result == null) return false;
        return result.getButtonData().isDefaultButton();
    }
    
}
