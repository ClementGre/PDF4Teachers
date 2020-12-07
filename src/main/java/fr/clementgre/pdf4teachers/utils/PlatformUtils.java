package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.interfaces.ReturnCallBack;
import javafx.application.Platform;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class PlatformUtils {

    
    public static <T> T runAndWait(ReturnCallBack<T> action) {
        if (action == null)
            throw new NullPointerException("action");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);

        AtomicReference<T> toReturn = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                toReturn.set(action.call());
            } finally {
                doneLatch.countDown();
            }
        });

        try{
            doneLatch.await();
        }catch(InterruptedException ignored){}

        return toReturn.get();

    }

    public static void openFile(String uri){

        if (Main.isOSX()){
            try {
                Runtime.getRuntime().exec("/usr/bin/open " + uri).waitFor();
            } catch (InterruptedException | IOException e) {
                System.out.println("unable to open URI");
                e.printStackTrace();
            }
        }else{
            Main.hostServices.showDocument(uri);    //Doesn't work for OSX
        }

    }
}
