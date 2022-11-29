/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;

public class ConfirmAlert extends CustomAlert{
    
    public ConfirmAlert(boolean irreversible, String header){
        super(AlertType.CONFIRMATION, TR.tr("dialog.confirmation.title"), header);
        if(irreversible) {
            setContentText(TR.tr("dialog.confirmation.irreversible"));
        }
        
        addConfirmButton(ButtonPosition.DEFAULT);
        addCancelButton(ButtonPosition.CLOSE);
        
    }
    
    public boolean execute(){
        return getShowAndWaitIsDefaultButton();
    }
    
}
