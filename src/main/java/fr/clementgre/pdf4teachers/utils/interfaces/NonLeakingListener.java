package fr.clementgre.pdf4teachers.utils.interfaces;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.lang.ref.WeakReference;

public class NonLeakingListener<L, T> implements ChangeListener<L>{
    private final WeakReference<T> element;
    private final CallBackArg<ChangeEvent<L, T>> onChanged;
    
    public NonLeakingListener(T element, CallBackArg<ChangeEvent<L, T>> onChanged) {
        this.element = new WeakReference<>(element);
        this.onChanged = onChanged;
    }
    
    @Override
    public void changed(ObservableValue<? extends L> observable, L oldValue, L newValue){
        if(element.get() != null) {
            onChanged.call(new ChangeEvent<>(observable, oldValue, newValue, element.get()));
        }
    }
    
    public record ChangeEvent<L, T>(ObservableValue<? extends L> observable, L oldValue, L newValue, T element){
        public T getElement(){
            return element;
        }
        public ObservableValue<? extends L> getObservable(){
            return observable;
        }
        public L getOldValue(){
            return oldValue;
        }
        public L getNewValue(){
            return newValue;
        }
    }
}
