/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.interfaces;

import javafx.event.Event;

import java.lang.ref.WeakReference;

public class NonLeakingEventHandler<L extends Event, T> implements javafx.event.EventHandler<L>{
    
    private final WeakReference<T> element;
    private final CallBackArg<EventHandler<L, T>> onChanged;
    
    public NonLeakingEventHandler(T element, CallBackArg<EventHandler<L, T>> onChanged) {
        this.element = new WeakReference<>(element);
        this.onChanged = onChanged;
    }
    
    @Override
    public void handle(L event){
        if(element.get() != null) {
            onChanged.call(new EventHandler<>(event, element.get()));
        }
    }
    
    public record EventHandler<L, T>(L event, T element){
        public T getElement(){
            return element;
        }
        public L getEvent(){
            return event;
        }
    }
    
}
