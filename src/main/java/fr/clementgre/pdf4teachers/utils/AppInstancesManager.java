/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import javafx.application.Platform;
import tk.pratanumandal.unique4j.Unique4j;
import tk.pratanumandal.unique4j.Unique4jList;
import tk.pratanumandal.unique4j.exception.Unique4jException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppInstancesManager {
    
    private static Unique4j unique;
    
    public static boolean registerInstance(List<String> args){
        
        List<String> toOpenPaths /*= getToOpenFiles(args).stream().map(File::getAbsolutePath).toList();
        toOpenPaths*/ = Arrays.asList("/home/clement/Téléchargements/Kev.pdf");
        System.out.println("Executing with toOpenPath = " + toOpenPaths.toString());
        
        unique = new Unique4jList(Main.APP_ID, false) {
            
            @Override
            protected List<String> sendMessageList(){
                System.out.println("An instance of this app is already running !");
                if(toOpenPaths.size() != 0) System.out.println("Sending arguments to first instance...");
                
                return toOpenPaths;
            }
            
            @Override
            protected void receiveMessageList(List<String> toOpenPaths){
                System.out.println("Receiving arguments from another instance: " + toOpenPaths.toString());
                
                if(toOpenPaths.size() == 0){
                    // No file to open -> the next instance will take the lock
                    /*try{
                        System.out.println("freeLock because the message was empty : the next instance will take the lock");
                        unique.freeLock();
                    }catch(Unique4jException e){e.printStackTrace();}*/
                    
                }else{
                    Platform.runLater(() -> tryToOpenFiles(getToOpenFiles(toOpenPaths)));
                }
            }
            
        };
        
        try{
            boolean locked = unique.acquireLock();
            
            if(!locked && toOpenPaths.size() == 0){
                // In this case, the last instance should have freeLock()
                /*while(!locked){
                    PlatformUtils.sleepThread(300);
                    System.out.println("cycle...");
                    locked = unique.acquireLock();
                }
                System.out.println("Lock acquired because of no files to open");*/
                return true;
            }
            
            return locked;
            
        }catch(Unique4jException e){
            e.printStackTrace();
            return false;
        }
    }
    
    
    public static void tryToOpenFiles(List<File> toOpenFiles){
        while(true){
            if(Main.window == null){
                PlatformUtils.sleepThread(500);
            }else if(Main.window.isShowing()){
                Main.window.openFiles(toOpenFiles, true);
                Main.window.setIconified(true);
                Main.window.requestFocus();
                Main.window.setIconified(false);
                return;
            }else{
                Main.window.setOnShown((e) -> Main.window.openFiles(toOpenFiles, true));
                return;
            }
        }
        
    }
    
    public static List<File> getToOpenFiles(List<String> rawArgs){
        List<File> toOpenFiles = new ArrayList<>();
        
        for(String param : rawArgs){
            File file = new File(param);
            if(file.exists() && FilesUtils.getExtension(file).equals("pdf")){
                toOpenFiles.add(file);
            }
        }
        
        return toOpenFiles;
    }
    
}
