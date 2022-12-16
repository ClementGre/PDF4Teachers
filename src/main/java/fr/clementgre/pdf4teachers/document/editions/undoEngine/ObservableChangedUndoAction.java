/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.beans.property.Property;
import javafx.geometry.Bounds;
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
            Bounds vectorElementBeforeBounds = null;
            if(!(value instanceof String)){
                MainWindow.mainScreen.setSelected(null);
            }else if(element.get() instanceof VectorElement element && observable.get() == element.pathProperty()){ // VectorElement path
                vectorElementBeforeBounds = element.getNoScaledSvgPath().getLayoutBounds();
                //  When element scale to page is performed, UndoType is UType.NO_COUNT => We need to quit the edit mode
                if(element.isEditMode() && getUndoType() == UType.NO_COUNT){
                    element.quitEditMode();
                }
            }
            
            T oldValue = observable.get().getValue();
            observable.get().setValue(value);
            value = oldValue;
    
            if(!(value instanceof String)){
                MainWindow.mainScreen.setSelected(selected);
            }else if(element.get() instanceof VectorElement element && observable.get() == element.pathProperty()){ // VectorElement path
                // Dimensions must not be corrected if element is in edit mode
                // When element scale to page is performed, UndoType is UType.NO_COUNT. And in this case, dimensions must not be corrected.
                if(vectorElementBeforeBounds != null && !element.isEditMode() && getUndoType() == UType.UNDO) element.correctDimensions(vectorElementBeforeBounds);
            }
    
            Edition.setUnsave("ObservableChangedUndoAction");
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
