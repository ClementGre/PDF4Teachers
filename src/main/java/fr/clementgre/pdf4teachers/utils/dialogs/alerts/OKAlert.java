/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

public class OKAlert extends CustomAlert{
    
    public OKAlert(String title){
        this(AlertType.INFORMATION, title, "", null);
    }
    public OKAlert(String title, String header){
        this(AlertType.INFORMATION, title, header, null);
    }
    public OKAlert(String title, String header, String details){
        this(AlertType.INFORMATION, title, header, details);
    }
    public OKAlert(AlertType icon, String title, String header, String details){
        super(icon, title, header, details);
        
        addOKButton(ButtonPosition.DEFAULT);
    }
    
}
