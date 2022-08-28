/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilterUtils {
    
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    
    // Must be used with at least one argument
    @SafeVarargs
    public static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        
        ArrayList<Set<Object>> sets = new ArrayList<>();
        for(int i = 0; i < keyExtractors.length; i++){
            sets.add(ConcurrentHashMap.newKeySet());
        }
        
        return t -> {
            boolean atLeastOneDifferent = false;
            for(int i = 0; i < keyExtractors.length; i++){
                if(sets.get(i).add(keyExtractors[i].apply(t))) atLeastOneDifferent = true;
            }
            return atLeastOneDifferent;
        };
    }
    
}
