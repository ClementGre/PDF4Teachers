/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs;

public enum AlertIconType{
    CONFIRM, ERROR, INFORMATION, WARNING;

    public String getFileName(){
        return name().toLowerCase();
    }

}
