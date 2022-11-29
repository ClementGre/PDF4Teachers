package fr.clementgre.pdf4teachers.utils.interfaces;

import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.application.Platform;

public class CallsBuffer<T> {
    
    protected final int waitTime;
    protected final int waitTimeNs;
    protected final CallBackArg<T> callBack;
    public CallsBuffer(int waitTime, CallBackArg<T> callback){
        this.waitTime = waitTime;
        this.waitTimeNs = waitTime*1000000; // Convert to nanoseconds
        this.callBack = callback;
    }
    
    private long lastCallDate;
    public void call(T arg){
        lastCallDate = System.nanoTime();
        long localLastCallDate = lastCallDate;
        
        PlatformUtils.runLaterAnyThread(waitTime, () -> {
            if(localLastCallDate == lastCallDate){
                Platform.runLater(() -> callBack.call(arg));
            }
        });
    }
    
}
