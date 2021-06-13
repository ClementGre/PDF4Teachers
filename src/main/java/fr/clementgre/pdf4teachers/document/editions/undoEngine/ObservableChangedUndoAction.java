package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.beans.property.Property;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.lang.ref.WeakReference;

public class ObservableChangedUndoAction<T> extends UndoAction{
    
    private final WeakReference<Element> element;
    private final WeakReference<Property<T>> observable;
    private T value;
    
    public ObservableChangedUndoAction(Element element, Property<T> observable, T value, UType undoType){
        super(undoType);
        this.element = new WeakReference<>(element);
        this.observable = new WeakReference<>(observable);
        this.value = value;
    }
    
    @Override
    public boolean undoAndInvert(){
        if(observable.get() != null){
    
            // De-select and re-select element to update values in side panes
            // Strings does not provoke this update in order to keep the carret position.
            Element selected = MainWindow.mainScreen.getSelected();
            if(!(value instanceof String)){
                MainWindow.mainScreen.setSelected(null);
            }
            
            T oldValue = observable.get().getValue();
            observable.get().setValue(value);
            value = oldValue;
    
            if(!(value instanceof String)) MainWindow.mainScreen.setSelected(selected);
            
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        if(element.get() == null) return TR.tr("actions.edit") + " " + getElementName().toLowerCase();
        
        if(value instanceof String && element.get() instanceof GraphicElement){
            return TR.tr("actions.edit") + " " + getElementName().toLowerCase();
        }if(value instanceof String){
            return TR.tr("actions.rename") + " " + getElementName().toLowerCase();
        }if(value instanceof Color){
            return TR.tr("actions.editColor") + " " + getElementName().toLowerCase();
        }if(value instanceof Font){
            return TR.tr("actions.editFont") + " " + getElementName().toLowerCase();
        }
        return TR.tr("actions.edit") + " " + getElementName().toLowerCase();
    }
    
    public String getElementName(){
        if(element.get() != null){
            return element.get().getElementName(false);
        }
        return "";
    }
    
    public Property<T> getObservableValue(){
        return observable.get();
    }
}
