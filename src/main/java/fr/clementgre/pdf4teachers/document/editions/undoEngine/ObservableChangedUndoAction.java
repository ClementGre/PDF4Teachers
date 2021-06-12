package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import javafx.beans.property.Property;

import java.lang.ref.WeakReference;

public class ObservableChangedUndoAction<T> extends UndoAction{
    
    private final WeakReference<Property<T>> observable;
    private T value;
    
    public ObservableChangedUndoAction(Property<T> observable, T value, UType undoType){
        super(undoType);
        this.observable = new WeakReference<>(observable);
        this.value = value;
    }
    
    @Override
    public boolean undoAndInvert(){
        if(observable.get() != null){
            
            T oldValue = observable.get().getValue();
            observable.get().setValue(value);
            value = oldValue;
            
            
            return true;
        }
        return false;
    }
}
