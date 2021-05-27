package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.ReturnCallBack;
import javafx.application.Platform;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class PlatformUtils{
    
    public static void runLaterOnUIThread(int millis, Runnable runnable){
        new Thread(() -> {
            try{
                Thread.sleep(millis);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            Platform.runLater(runnable);
        }, "runLaterOnUIThread").start();
    }
    
    // Running code on JavaFX Thread from another Thread
    // and waiting this acting is completed before continuing the other Thread
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
                }catch(InterruptedException e){ e.printStackTrace(); }
            }
        }, "Heap debugger").start();
    }
    public static void printHeapStatus(){
        if(MainWindow.twoDigFormat != null)
            System.out.println("Heap: " + MainWindow.twoDigFormat.format((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000d)
                + "MB / " + MainWindow.twoDigFormat.format(Runtime.getRuntime().maxMemory()/1000000d) + "MB");
        else
            System.out.println("Heap: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000
                    + "MB / " + Runtime.getRuntime().maxMemory()/1000000 + "MB");
    }
    public static void printActionTime(CallBack action, String name){
        long time = countActionTime(action);
        System.out.println("Executing action \"" + name + "\" in " + time + "ms (" + (time / 1000) + "s)");
    }
    public static void printActionTimeIfDebug(CallBack action, String name){
        if(!Main.DEBUG){
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
    
    public static void openDirectory(String uri){
        
        if(Main.isOSX()){
            try{
                Runtime.getRuntime().exec("/usr/bin/open " + uri).waitFor();
            }catch(InterruptedException | IOException e){
                System.out.println("unable to open URI directory");
                e.printStackTrace();
            }
        }else{
            Main.hostServices.showDocument(uri); // Doesn't work for OSX
        }
        
    }
    
    public static void openFile(String uri){
        
        if(Main.isOSX()){
            try{
                Runtime.getRuntime().exec("/usr/bin/open -a TextEdit " + uri).waitFor();
            }catch(InterruptedException | IOException e){
                System.out.println("unable to open URI file");
                e.printStackTrace();
            }
        }else{
            Main.hostServices.showDocument(uri);// Doesn't work for OSX
        }
        
    }
}
