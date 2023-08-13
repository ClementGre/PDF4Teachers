package fr.clementgre.pdf4teachers.interfaces;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.Objects;
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
