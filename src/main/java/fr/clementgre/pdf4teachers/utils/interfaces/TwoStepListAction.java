package fr.clementgre.pdf4teachers.utils.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TwoStepListAction<T, D> {

    private int completedSize = 0;
    private boolean canceled = false;
    private List<T> data;
    private List<D> sortedData = new ArrayList<>();
    private HashMap<Integer, Integer> excludedReasons = new HashMap<>();

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

    public TwoStepListAction(TwoStepListInterface<T, D> caller){
        this.data = caller.prepare();

        if(sortData(caller)){
            if(processData(caller)){
                caller.finish(data.size(), sortedData.size(), completedSize, excludedReasons);
            }
        }

    }

    public boolean sortData(TwoStepListInterface<T, D> caller){
        for(T value : data){
            Entry<D, Integer> result = caller.sortData(value);
            if(result != null){
                if(result.getValue() == 0){
                    sortedData.add(result.getKey());
                }if(result.getValue() == -1){
                    return false;
                }else{
                    if(excludedReasons.containsKey(result.getValue())){
                        excludedReasons.put(result.getValue(), excludedReasons.get(result.getValue())+1);
                    }else{
                        excludedReasons.put(result.getValue(), 1);
                    }

                }

            }
        }
        return true;
    }

    public boolean processData(TwoStepListInterface<T, D> caller){
        for(D value : sortedData){
            TwoStepListAction.ProcessResult result = caller.completeData(value);
            if(result == ProcessResult.OK) completedSize++;
            else if(result == ProcessResult.STOP) break;
            else if(result == ProcessResult.STOP_WITHOUT_ALERT) return false;
        }
        return true;
    }



}
