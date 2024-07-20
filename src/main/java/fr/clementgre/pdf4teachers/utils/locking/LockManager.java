/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.locking;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.application.Platform;
import tk.pratanumandal.unique4j.Unique4j;
import tk.pratanumandal.unique4j.Unique4jList;
import tk.pratanumandal.unique4j.exception.Unique4jException;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class LockManager {
    
    public static boolean locked;
    private static LockMessageType messageType = LockMessageType.CHECK;
    
    private static Unique4j unique;
    public static final boolean FAKE_OPEN_FILE = false;
    
    public static boolean registerInstance(List<String> args){
        
        List<String> toOpenPaths;
        if(FAKE_OPEN_FILE){
            toOpenPaths = List.of("C:\\Users\\Clement\\Documents\\PDF\\Kev.pdf");
        }else{
            toOpenPaths = getToOpenFiles(args).stream().map(File::getAbsolutePath).toList();
        }
        
        Log.d("Executing with toOpenPath = " + toOpenPaths);
        
        unique = new Unique4jList(Main.APP_ID, false) {
            
            @Override
            protected List<String> sendMessageList(){
                Log.d("Another instance is already running:");
                
                LockMessage message = new LockMessage(messageType, toOpenPaths);
                Log.d("Sending message to other instance: " + message.toStringInfos());
                return message.toList();
            }
            
            @Override
            protected void receiveMessageList(List<String> list){
                LockMessage message = LockMessage.fromList(list);
                Log.d("Receiving message from another instance: " + message.toStringInfos());
                
                if(message.type() == LockMessageType.REQUIRE_UNLOCK){
                    try{
                        locked = !unique.freeLock();
                        Log.d("Lock released: " + !locked);
                    }catch(Unique4jException e){Log.eNotified(e);}
                    
                }else if(message.type() == LockMessageType.OPEN_FILES){
                    Platform.runLater(() -> tryToOpenFiles(getToOpenFiles(message.args())));
                }
            }
            
        };
        
        try{
            
            if(toOpenPaths.isEmpty()){
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
                Log.d("Instance locked: " + locked);
                return true;
                
            }
            Log.d("Instance locked: " + locked);
            return locked; // If non-locked: the files has been opened on the locked instance.
            
        }catch(Unique4jException e){
            Log.eNotified(e);
            return false;
        }
    }
    public static void onCloseApp(){
        try{
            if(locked) locked = unique.freeLock();
        }catch(Unique4jException e){Log.eNotified(e);}
    }
    
    public static void tryToOpenFiles(List<File> toOpenFiles){
        if(Main.window != null && Main.window.isShowing()){
            
            MainWindow.mainScreen.openFiles(toOpenFiles, !MainWindow.mainScreen.hasDocument(false));
            
            /*Main.window.setIconified(true);*/
            Main.window.requestFocus();
            /*Main.window.setIconified(false);*/
        }else if(Main.window != null){
            Main.window.setOnShown((e) -> MainWindow.mainScreen.openFiles(toOpenFiles, true));
        }
        
        
    }
    
    public static List<File> getToOpenFiles(List<String> rawArgs){
        return rawArgs.stream()
                .map(File::new)
                .filter(file -> file.exists() && FilesUtils.getExtension(file.toPath()).equals("pdf"))
                .collect(Collectors.toList());
    }
}
