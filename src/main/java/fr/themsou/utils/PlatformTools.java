package fr.themsou.utils;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class PlatformTools {

    
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
}
