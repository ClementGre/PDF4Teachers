/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.ReturnCallBack;
import javafx.application.Platform;
import javafx.scene.Cursor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class PlatformUtils {
    
    public static final Cursor CURSOR_MOVE = isMac() ? Cursor.OPEN_HAND : Cursor.MOVE;
    
    public static void sleepThread(long millis){
        try{
            Thread.sleep(millis);
        }catch(InterruptedException e){
            Log.eNotified(e);
        }
    }
    public static void sleepThreadSeconds(double seconds){
        try{
            Thread.sleep((long) (seconds * 1000));
        }catch(InterruptedException e){
            Log.eNotified(e);
        }
    }
    public static void sleepThreadMinutes(double minutes){
        try{
            Thread.sleep((long) (minutes * 60000));
        }catch(InterruptedException e){
            Log.eNotified(e);
        }
    }
    
    public static void runLaterOnUIThread(int millis, Runnable runnable){
        new Thread(() -> {
            try{
                Thread.sleep(millis);
            }catch(InterruptedException e){
                Log.eNotified(e);
            }
            Platform.runLater(runnable);
        }, "runLaterOnUIThread").start();
    }
    
    public static void repeatOnUIThread(int millis, Runnable runnable){
        new Thread(() -> {
            while(true){
                try{
                    Thread.sleep(millis);
                }catch(InterruptedException e){
                    Log.eNotified(e);
                }
                Platform.runLater(runnable);
            }
        }, "repeatOnUIThread").start();
    }
    
    // Run code on JavaFX Application Thread from another Thread
    // and wait until action completes before continuing the other Thread
    public static <T> T runAndWait(ReturnCallBack<T> action){
        if(action == null)
            throw new NullPointerException("action");
        
        // run synchronously on JavaFX thread
        if(Platform.isFxApplicationThread()){
            return action.call();
        }
        
        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        
        AtomicReference<T> toReturn = new AtomicReference<>();
        Platform.runLater(() -> {
            try{
                toReturn.set(action.call());
            }finally{
                doneLatch.countDown();
            }
        });
        
        try{
            doneLatch.await();
        }catch(InterruptedException ignored){
        }
        
        return toReturn.get();
        
    }
    public static void startHeapDebug(long printIntervalMs){
        new Thread(() -> {
            while(true){
                System.gc();
                printHeapStatus();
                try{
                    Thread.sleep(printIntervalMs);
                }catch(InterruptedException e){Log.eNotified(e);}
            }
        }, "Heap debugger").start();
    }
    public static void printHeapStatus(){
        if(MainWindow.twoDigFormat != null)
            Log.d("Heap: " + MainWindow.twoDigFormat.format((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000d)
                    + "MB / " + MainWindow.twoDigFormat.format(Runtime.getRuntime().maxMemory() / 1000000d) + "MB");
        else
            Log.d("Heap: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000
                    + "MB / " + Runtime.getRuntime().maxMemory() / 1000000 + "MB");
    }
    public static void printActionTime(CallBack action, String name){
        long time = countActionTime(action);
        Log.d("Executing action \"" + name + "\" in " + time + "ms (" + (time / 1000) + "s)");
    }
    public static void printActionTimeIfDebug(CallBack action, String name){
        if(!Log.doDebug()){
            action.call();
            return;
        }
        printActionTime(action, name);
    }
    public static long countActionTime(CallBack action){
        long startTime = System.currentTimeMillis();
        action.call();
        return System.currentTimeMillis() - startTime;
    }
    
    public static void openFile(String uri){
        if(isMac()){
            try{
                if(Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(uri));
            }catch(IOException e){
                Log.eNotified(e, "unable to open URI file/directory: " + uri);
            }
        }else{
            Main.hostServices.showDocument(uri);// Doesn't work for OSX
        }
    }
    
    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    public static boolean isMac(){
        return System.getProperty("os.name").toLowerCase().contains("mac os x");
    }
    public static boolean isLinux(){
        return !isWindows() && !isMac();
    }
    public static boolean isMacAArch64(){
        return isMac() && System.getProperty("os.arch").toLowerCase().contains("aarch64");
    }
    public static boolean isJDKMacAArch64(){
        return isMac() && "true".equals(System.getProperty("fr.clementgre.pdf4teachers.isaarch64"));
    }
    
    public static String getDataFolder(){
        String dataFolder;
        
        if(isWindows()){
            dataFolder = System.getenv("APPDATA") + "\\PDF4Teachers\\";
        }else if(isMac()){
            dataFolder = System.getProperty("user.home") + "/Library/Application Support/PDF4Teachers/";
            // Move data folder if needed
            if(!new File(dataFolder).exists() && new File(System.getProperty("user.home") + "/.PDF4Teachers/").exists()) FilesUtils.moveDataFolder();
        }else{
            if(System.getenv("XDG_DATA_HOME") != null && new File(System.getenv("XDG_DATA_HOME")).exists()) dataFolder = System.getenv("XDG_DATA_HOME") + "/PDF4Teachers/";
            else dataFolder = System.getProperty("user.home") + "/.local/share/PDF4Teachers/";
            // Move data folder if needed
            if(!new File(dataFolder).exists() && new File(System.getProperty("user.home") + "/.PDF4Teachers/").exists()) FilesUtils.moveDataFolder();
        }
        
        new File(dataFolder).mkdirs();
        return dataFolder;
    }
}
