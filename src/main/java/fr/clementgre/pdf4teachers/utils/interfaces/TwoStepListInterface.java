/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public interface TwoStepListInterface<T, D>{
    
    List<T> prepare(boolean recursive);
    
    Entry<D, Integer> sortData(T data, boolean recursive) throws Exception;
    
    String getSortedDataName(D data, boolean recursive);
    
    TwoStepListAction.ProcessResult completeData(D data, boolean recursive);
    
    void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive);
    
}
