package fr.clementgre.pdf4teachers.utils.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public interface TwoStepListInterface<T, D> {

    List<T> prepare();
    Entry<D, Integer> sortData(T data);
    TwoStepListAction.ProcessResult completeData(D data);
    void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons);

}
