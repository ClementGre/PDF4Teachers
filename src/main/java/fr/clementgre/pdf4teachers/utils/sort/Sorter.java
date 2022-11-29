/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.sort;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;

import java.io.File;
import java.util.List;

public class Sorter {
    
    // FILES SORTING
    
    public static List<File> sortFilesByName(List<File> files, boolean order){
        
        files.sort((file1, file2) -> {
            int result = file1.getName().compareToIgnoreCase(file2.getName());
            
            if(order) {
                return result;
            }
            return result * -1;
        });
        return files;
    }
    
    public static List<File> sortFilesByDir(List<File> files, boolean order){
        
        files.sort((file1, file2) -> {
            int result = file1.getParent().compareToIgnoreCase(file2.getParent());
            if(result == 0) {
                result = file1.getName().compareToIgnoreCase(file2.getName());
            }
            
            if(order) {
                return result;
            }
            return result * -1;
        });
        return files;
    }
    
    public static List<File> sortFilesByEdit(List<File> files, boolean order){
        
        files.sort((file1, file2) -> {
            
            int file1Elements = 0;
            if(Edition.getEditFile(file1).exists()){
                try{
                    file1Elements = Edition.countElements(Edition.getEditFile(file1));
                }catch(Exception e){
                    Log.eNotified(e);
                }
            }
            int file2Elements = 0;
            if(Edition.getEditFile(file2).exists()){
                try{
                    file2Elements = Edition.countElements(Edition.getEditFile(file2));
                }catch(Exception e){
                    Log.eNotified(e);
                }
            }
    
            int result = ((file1Elements - 9999) + file1.getName() + file1.getParentFile().getAbsolutePath()).compareToIgnoreCase((file2Elements - 9999) + file2.getName() + file2.getParentFile().getAbsolutePath());
            if(order) {
                return result;
            }
            return result * -1;
        });
        return files;
    }
    
    // ELEMENT SORTING
    
    public static List<TextTreeItem> sortElementsByDate(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            
            if(!order) {
                return ((element1.getCreationDate() - 999999999999L) + element1.getText()).compareToIgnoreCase((element2.getCreationDate() - 999999999999L) + element2.getText()) * -1;
            }
            return ((element1.getCreationDate() - 999999999999L) + element1.getText()).compareToIgnoreCase((element2.getCreationDate() - 999999999999L) + element2.getText());
        });
        return elements;
    }
    
    public static List<TextTreeItem> sortElementsByName(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            
            if(!order) {
                return element1.getText().compareToIgnoreCase(element2.getText()) * -1;
            }
            return element1.getText().compareToIgnoreCase(element2.getText());
        });
        return elements;
    }
    
    public static List<TextTreeItem> sortElementsByUtils(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            
            if(!order) {
                return ((element1.getUses() - 99999) + element1.getText()).compareToIgnoreCase((element2.getUses() - 99999) + element2.getText()) * -1;
            }
            return ((element1.getUses() - 99999) + element1.getText()).compareToIgnoreCase((element2.getUses() - 99999) + element2.getText());
        });
        return elements;
    }
    
    public static List<TextTreeItem> sortElementsByPolice(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            
            if(!order) {
                return ((element1.getFont().getFamily() + element1.getFont().getStyle() + element1.getText()).compareToIgnoreCase(element2.getFont().getFamily() + element2.getFont().getStyle() + element2.getText())) * -1;
            }
            return ((element1.getFont().getFamily() + element1.getFont().getStyle() + element1.getText()).compareToIgnoreCase(element2.getFont().getFamily() + element2.getFont().getStyle() + element2.getText()));
        });
        return elements;
    }
    
    public static List<TextTreeItem> sortElementsBySize(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            
            if(!order) {
                return ((element1.getFont().getSize() - 999.0) + element1.getText()).compareToIgnoreCase((element2.getFont().getSize() - 999.0) + element2.getText()) * -1;
            }
            return ((element1.getFont().getSize() - 999.0) + element1.getText()).compareToIgnoreCase((element2.getFont().getSize() - 999.0) + element2.getText());
        });
        return elements;
    }
    
    public static List<TextTreeItem> sortElementsByColor(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            if(!order) {
                return ((element1.getColor() + element1.getText()).compareToIgnoreCase(element2.getColor() + element2.getText())) * -1;
            }
            return ((element1.getColor() + element1.getText()).compareToIgnoreCase(element2.getColor() + element2.getText()));
        });
        return elements;
    }
    
    public static List<TextTreeItem> sortElementsByCorePosition(List<TextTreeItem> elements, boolean order){
        elements.sort((element1, element2) -> {
            
            if(!order) {
                return (element1.getCore().getPageNumber() - 999 + "" + (element1.getCore().getRealY() - 233900)).compareToIgnoreCase(element2.getCore().getPageNumber() - 999 + "" + (element2.getCore().getRealY() - 233900));
            }
            return (element1.getCore().getPageNumber() - 999 + "" + (element1.getCore().getRealY() - 233900)).compareToIgnoreCase(element2.getCore().getPageNumber() - 999 + "" + (element2.getCore().getRealY() - 233900)) * -1;
            
        });
        return elements;
    }
}
