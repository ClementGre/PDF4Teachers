/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.locking;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.application.Platform;
import tk.pratanumandal.unique4j.Unique4j;
import tk.pratanumandal.unique4j.Unique4jList;
import tk.pratanumandal.unique4j.exception.Unique4jException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LockManager {
    
    public static boolean locked = false;
    private static LockMessageType messageType = LockMessageType.CHECK;
    
    private static Unique4j unique;
    public static final boolean FAKE_OPEN_FILE = false;
    
    public static boolean registerInstance(List<String> args){
        
        List<String> toOpenPaths;
        if(FAKE_OPEN_FILE){
            toOpenPaths = List.of("/home/clement/Téléchargements/Kev.pdf");
        }else{
            toOpenPaths = getToOpenFiles(args).stream().map(File::getAbsolutePath).toList();
        }
        
        System.out.println("Executing with toOpenPath = " + toOpenPaths.toString());
        
        unique = new Unique4jList(Main.APP_ID, false) {
            
            @Override
            protected List<String> sendMessageList(){
                System.out.println("Another instance is already running:");
                
                LockMessage message = new LockMessage(messageType, toOpenPaths);
                System.out.println("Sending message to other instance: " + message.toStringInfos());
                return message.toList();
            }
            
            @Override
            protected void receiveMessageList(List<String> list){
                LockMessage message = LockMessage.fromList(list);
                System.out.println("Receiving message from another instance: " + message.toStringInfos());
                
                if(message.type() == LockMessageType.REQUIRE_UNLOCK){
                    try{
                        locked = unique.freeLock();
                        System.out.println("Lock released: " + locked);
                    }catch(Unique4jException e){e.printStackTrace();}
                    
                }else if(message.type() == LockMessageType.OPEN_FILES){
                    Platform.runLater(() -> tryToOpenFiles(getToOpenFiles(message.args())));
                }
            }
            
        };
        
        try{
            
            if(toOpenPaths.size() == 0){
                messageType = LockMessageType.REQUIRE_UNLOCK;
            }else{
                messageType = LockMessageType.OPEN_FILES;
            }
            locked = unique.acquireLock();
            
            if(!locked && messageType == LockMessageType.REQUIRE_UNLOCK){
                // In this case, the last instance should have freeLock()
                int i = 0;
                while(!locked){
                    if(i > 5) break;
                    i++;
                    PlatformUtils.sleepThread(300);
                    locked = unique.acquireLock();
                }
                System.out.println("Instance locked: " + locked);
                return true;
                
            }else{
                System.out.println("Instance locked: " + locked);
                return locked; // If non-locked: the files has been opened on the other instance.
            }
            
        }catch(Unique4jException e){
            e.printStackTrace();
            return false;
        }
    }
    public static void onCloseApp(){
        try{
            locked = unique.freeLock();
        }catch(Unique4jException e){e.printStackTrace();}
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
