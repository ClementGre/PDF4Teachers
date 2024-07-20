/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.Alert;

public class WrongAlert extends CustomAlert {
    
    private final boolean continueAsk;
    
    public WrongAlert(String header, String details, boolean continueAsk){
        this(AlertType.WARNING, TR.tr("dialog.error.title"), header, details, continueAsk);
    }
    public WrongAlert(String title, String header, String details, boolean continueAsk){
        this(AlertType.WARNING, title, header, details, continueAsk);
    }
    public WrongAlert(Alert.AlertType icon, String title, String header, String details, boolean continueAsk){
        super(icon, title, header, details);
        this.continueAsk = continueAsk;
        
        if(continueAsk){
            addButton(TR.tr("dialog.actionError.cancelAll"), ButtonPosition.CLOSE);
            addContinueButton(ButtonPosition.DEFAULT);
        }else{
            addOKButton(ButtonPosition.CLOSE);
        }
    }
    
    public boolean execute(){
        if(continueAsk){
            return getShowAndWaitIsCancelButton();
        }
        show();
        return false;
    }
}
