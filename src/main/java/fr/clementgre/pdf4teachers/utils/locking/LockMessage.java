/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.locking;

import java.util.ArrayList;
import java.util.List;

public record LockMessage(LockMessageType type, List<String> args) {
    
    public List<String> toList(){
        ArrayList<String> list = new ArrayList<>();
        list.add(type().name());
        list.addAll(args());
        return list;
    }
    
    public String toStringInfos(){
        return "Type=" + type().name() + " ; Args=" + args().toString();
    }
    
    public static LockMessage fromList(List<String> list){
        LockMessageType type = LockMessageType.valueOf(list.getFirst());
        List<String> args = list.subList(1, list.size());
        return new LockMessage(type, args);
    }
    
}
