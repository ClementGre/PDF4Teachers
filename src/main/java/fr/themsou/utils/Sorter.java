package fr.themsou.utils;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.util.Comparator;
import java.util.List;

public class Sorter {

    public static List<File> sortByName(List<File> files, boolean order){


        files.sort(new Comparator<File>() {
            @Override public int compare(File file1, File file2){
                if(!order) return (file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase(file2.getName() + file1.getParentFile().getAbsolutePath())*-1;
                return (file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase(file2.getName() + file1.getParentFile().getAbsolutePath());
            }
        });
        return files;
    }
    public static List<File> sortByDir(List<File> files, boolean order){

        files.sort(new Comparator<File>() {
            @Override public int compare(File file1, File file2){
                if(!order) return file1.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath())*-1;
                return file1.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath());
            }
        });
        return files;
    }
    public static List<File> sortByEdit(List<File> files, boolean order){

        files.sort(new Comparator<File>() {
            @Override public int compare(File file1, File file2){

                int file1Elements = 0;
                if(Edition.getEditFile(file1).exists()){
                    try{
                        file1Elements = Edition.countElements(Edition.getEditFile(file1))[0];
                    }catch(Exception e){ e.printStackTrace();}
                }
                int file2Elements = 0;
                if(Edition.getEditFile(file2).exists()){
                    try{
                        file2Elements = Edition.countElements(Edition.getEditFile(file2))[0];
                    }catch(Exception e){ e.printStackTrace();}
                }

                if(!order) return ((file1Elements-9999) + file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase((file2Elements-9999) + file2.getName() + file2.getParentFile().getAbsolutePath())*-1;
                return ((file1Elements-9999) + file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase((file2Elements-9999) + file2.getName() + file2.getParentFile().getAbsolutePath());
            }
        });
        return files;
    }
}
