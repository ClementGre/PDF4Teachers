/*
 * Copyright (c) 2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.keyboard;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.function.Predicate;

public class CustomKeyCombination extends KeyCombination {
    
    private final Predicate<KeyEvent> predicate;
    
    public CustomKeyCombination(Predicate<KeyEvent> predicate, KeyCombination.Modifier... modifiers){
        super(modifiers);
        this.predicate = predicate;
    }
    
    @Override
    public boolean match(KeyEvent event){
        return super.match(event) && predicate.test(event);
    }
    
    @Override
    public int hashCode(){
        return super.hashCode() + 31 * predicate.hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj instanceof CustomKeyCombination ckc){
            return super.equals(ckc) && predicate.equals(ckc.predicate);
        }
        return false;
    }
}
