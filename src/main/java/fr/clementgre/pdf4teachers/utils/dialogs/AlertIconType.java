package fr.clementgre.pdf4teachers.utils.dialogs;

public enum AlertIconType{
    CONFIRM, ERROR, INFORMATION, WARNING;

    public String getFileName(){
        return name().toLowerCase();
    }

}
