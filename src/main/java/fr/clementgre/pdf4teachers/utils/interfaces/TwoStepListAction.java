/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.interfaces;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.LoadingAlert;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TwoStepListAction<T, D>{
    
    private int completedSize;
    private boolean recursive;
    private final List<T> data;
    private final List<D> sortedData = new ArrayList<>();
    private final HashMap<Integer, Integer> excludedReasons = new HashMap<>();
    
    public static final int CODE_STOP = -1;
    public static final int CODE_OK = 0;
    public static final int CODE_SKIP_1 = 1;
    public static final int CODE_SKIP_2 = 2;
    public static final int CODE_SKIP_3 = 3;
    public static final int CODE_SKIP_4 = 4;
    
    
    public enum ProcessResult{
        OK,
        SKIPPED,
        STOP,
        STOP_WITHOUT_ALERT
    }
    
    public TwoStepListAction(boolean async, boolean recursive, TwoStepListInterface<T, D> caller){
        this.recursive = recursive;
        this.data = caller.prepare(isRecursive());
        
        if(filterData(caller)){
            if(async){
                processDataAsync(caller, () -> {
                    caller.finish(data.size(), sortedData.size(), completedSize, excludedReasons, isRecursive());
                });
            }else{
                if(processData(caller)){
                    caller.finish(data.size(), sortedData.size(), completedSize, excludedReasons, isRecursive());
                }
            }
            
        }
        
    }
    
    public boolean filterData(TwoStepListInterface<T, D> caller){
        for(T value : data){
            try{
                Entry<D, Integer> result = caller.filterData(value, isRecursive());
                if(result != null){
                    if(result.getValue() == 0){
                        sortedData.add(result.getKey());
                    }
                    if(result.getValue() == -1){
                        return false;
                    }
                    if(excludedReasons.containsKey(result.getValue())){
                        excludedReasons.put(result.getValue(), excludedReasons.get(result.getValue()) + 1);
                    }else{
                        excludedReasons.put(result.getValue(), 1);
                    }
                }
            }catch(Exception e){
                Log.e(e);
                boolean result = new ErrorAlert(null, e.getMessage(), data.size() > 1).execute();
                if(data.size() <= 1) return false;
                if(result) return false;
            }
        }
        return true;
    }
    
    public boolean processData(TwoStepListInterface<T, D> caller){
        for(D value : sortedData){
            TwoStepListAction.ProcessResult result = caller.completeData(value, isRecursive());
            if(result == ProcessResult.OK) completedSize++;
            else if(result == ProcessResult.STOP) break;
            else if(result == ProcessResult.STOP_WITHOUT_ALERT) return false;
        }
        return true;
    }
    
    private boolean shouldStop;
    public void processDataAsync(TwoStepListInterface<T, D> caller, CallBack callBack){
        LoadingAlert loadingAlert = new LoadingAlert(true, TR.tr("dialogs.asyncAction.header.title"), TR.tr("dialogs.asyncAction.header"));
        loadingAlert.setTotal(sortedData.size());
        
        loadingAlert.showAsync(() -> { // Cancel button click event
            shouldStop = true;
        });
        
        new Thread(() -> {
            boolean isCanceled = false;
            for(D value : sortedData){
                if(shouldStop){
                    isCanceled = true;
                    break;
                }
                
                ProcessResult result = caller.completeData(value, isRecursive());
                if(result == ProcessResult.OK) completedSize++;
                else if(result == ProcessResult.STOP) break;
                else if(result == ProcessResult.STOP_WITHOUT_ALERT){
                    isCanceled = true;
                    break;
                }
                
                Platform.runLater(() -> {
                    loadingAlert.setCurrentTaskText(caller.getSortedDataName(value, isRecursive()));
                    loadingAlert.setProgress(completedSize);
                });
                
            }
            boolean finalIsCanceled = isCanceled;
            Platform.runLater(() -> {
                loadingAlert.close();
                if(!finalIsCanceled){
                    callBack.call();
                }
            });
        }, "TwoStepListAction Async data processing").start();
        
        
    }
    
    public boolean isRecursive(){
        return recursive;
    }
    
    public void setRecursive(boolean recursive){
        this.recursive = recursive;
    }
}
