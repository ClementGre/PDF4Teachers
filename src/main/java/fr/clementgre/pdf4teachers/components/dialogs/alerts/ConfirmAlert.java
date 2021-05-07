package fr.clementgre.pdf4teachers.components.dialogs.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;

public class ConfirmAlert extends CustomAlert{
    
    public ConfirmAlert(boolean irreversible, String header){
        super(AlertType.CONFIRMATION, TR.tr("dialog.confirmation.title"), header);
        if(irreversible) setContentText(TR.tr("dialog.confirmation.irreversible"));
        
        addConfirmButton(ButtonPosition.DEFAULT);
        addCancelButton(ButtonPosition.CLOSE);
        
    }
    
    public boolean execute(){
        return getShowAndWaitIsDefaultButton();
    }
    
}
