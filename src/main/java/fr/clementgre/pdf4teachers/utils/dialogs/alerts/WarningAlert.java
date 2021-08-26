/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

public class WarningAlert extends CustomAlert {
    
    public WarningAlert(String title, String header, String details){
        super(AlertType.WARNING, title, header, details);
        addCancelButton(ButtonPosition.CLOSE);
        addContinueButton(ButtonPosition.DEFAULT);
    }
    
    public boolean execute(){
        return !getShowAndWaitIsCancelButton(); // OK or OS close
    }
    
}
