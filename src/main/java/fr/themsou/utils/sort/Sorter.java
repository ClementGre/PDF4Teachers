package fr.themsou.utils.sort;

import fr.themsou.document.editions.Edition;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import java.io.File;
import java.util.Comparator;
import java.util.List;

public class Sorter {

    // FILES SORTING

    public static List<File> sortFilesByName(List<File> files, boolean order){


        files.sort(new Comparator<File>() {
            @Override public int compare(File file1, File file2){
                if(!order) return (file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase(file2.getName() + file1.getParentFile().getAbsolutePath())*-1;
                return (file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase(file2.getName() + file1.getParentFile().getAbsolutePath());
            }
        });
        return files;
    }

    public static List<File> sortFilesByDir(List<File> files, boolean order){

        files.sort((file1, file2) -> {
            if(!order) return file1.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath())*-1;
            return file1.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath());
        });
        return files;
    }

    public static List<File> sortFilesByEdit(List<File> files, boolean order){

        files.sort((file1, file2) -> {

            int file1Elements = 0;
            if(Edition.getEditFile(file1).exists()){
                try{
                    file1Elements = (int) Edition.countElements(Edition.getEditFile(file1))[0];
                }catch(Exception e){ e.printStackTrace();}
            }
            int file2Elements = 0;
            if(Edition.getEditFile(file2).exists()){
                try{
                    file2Elements = (int) Edition.countElements(Edition.getEditFile(file2))[0];
                }catch(Exception e){ e.printStackTrace();}
            }

            if(!order) return ((file1Elements-9999) + file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase((file2Elements-9999) + file2.getName() + file2.getParentFile().getAbsolutePath())*-1;
            return ((file1Elements-9999) + file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase((file2Elements-9999) + file2.getName() + file2.getParentFile().getAbsolutePath());
        });
        return files;
    }

    // ELEMENT SORTING

    public static List<TextTreeItem> sortElementsByDate(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {

            if(!order) return ((element1.getCreationDate()-999999999999L) + element1.getText()).compareToIgnoreCase((element2.getCreationDate()-999999999999L) + element2.getText())*-1;
            return ((element1.getCreationDate()-999999999999L) + element1.getText()).compareToIgnoreCase((element2.getCreationDate()-999999999999L) + element2.getText());
        });
        return elements;
    }

    public static List<TextTreeItem> sortElementsByName(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {

            if(!order) return element1.getText().compareToIgnoreCase(element2.getText())*-1;
            return element1.getText().compareToIgnoreCase(element2.getText());
        });
        return elements;
    }

    public static List<TextTreeItem> sortElementsByUtils(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {

            if(!order) return ((element1.getUses()-99999) + element1.getText()).compareToIgnoreCase((element2.getUses()-99999) + element2.getText())*-1;
            return ((element1.getUses()-99999) + element1.getText()).compareToIgnoreCase((element2.getUses()-99999) + element2.getText());
        });
        return elements;
    }

    public static List<TextTreeItem> sortElementsByPolice(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {

            if(!order) return ((element1.getFont().getFamily() + element1.getFont().getStyle() + element1.getText()).compareToIgnoreCase(element2.getFont().getFamily() + element2.getFont().getStyle() + element2.getText()))*-1;
            return ((element1.getFont().getFamily() + element1.getFont().getStyle() + element1.getText()).compareToIgnoreCase(element2.getFont().getFamily() + element2.getFont().getStyle() + element2.getText()));
        });
        return elements;
    }

    public static List<TextTreeItem> sortElementsBySize(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {

            if(!order) return ((element1.getFont().getSize()-999.0) + element1.getText()).compareToIgnoreCase((element2.getFont().getSize()-999.0) + element2.getText())*-1;
            return ((element1.getFont().getSize()-999.0) + element1.getText()).compareToIgnoreCase((element2.getFont().getSize()-999.0) + element2.getText());
        });
        return elements;
    }

    public static List<TextTreeItem> sortElementsByColor(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            if(!order) return ((element1.getColor() + element1.getText()).compareToIgnoreCase(element2.getColor() + element2.getText()))*-1;
            return ((element1.getColor() + element1.getText()).compareToIgnoreCase(element2.getColor() + element2.getText()));
        });
        return elements;
    }

    public static List<TextTreeItem> sortElementsByCorePosition(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {

            if(!order) return (element1.getCore().getPageNumber()-999 + "" + (element1.getCore().getRealY()-9999)).compareToIgnoreCase(element2.getCore().getPageNumber()-999 + "" + (element2.getCore().getRealY()-9999));
            return (element1.getCore().getPageNumber()-999 + "" + (element1.getCore().getRealY()-9999)).compareToIgnoreCase(element2.getCore().getPageNumber()-999 + "" + (element2.getCore().getRealY()-9999))*-1;

        });
        return elements;
    }
}
