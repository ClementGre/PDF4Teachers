package fr.clementgre.pdf4teachers.utils.interfaces;

import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.application.Platform;

// Keeps the argument of the first call.
public class CallsBufferMemory<T> extends CallsBuffer<T>{
    public CallsBufferMemory(int waitTime, CallBackArg<T> callback){
        super(waitTime, callback);
    }
    
    private long lastCallDate;
    private T olderArg;
    @Override
    public void call(T arg){
        if(Math.abs(System.nanoTime() - lastCallDate) > waitTimeNs){ // First call
            olderArg = arg;
        }
        lastCallDate = System.nanoTime();
        long localLastCallDate = lastCallDate;
        
        PlatformUtils.runLaterAnyThread(waitTime, () -> {
            if(localLastCallDate == lastCallDate){
                Platform.runLater(() -> callBack.call(olderArg));
            }
        });
    }
    
}
