/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import org.apache.pdfbox.util.Matrix;

public record PageSpecs(float width, float height, float realWidth, float realHeight, float startX, float startY, Matrix rotation) {
    
    public float bottomMargin(){
        return realHeight() - height() - startY();
    }
    
    public float getYTopOrigin(){
        return bottomMargin() + realHeight();
    }
    
    
    // Converts a point to the PDPage coordinate space
    // considering the coordinate origin of the page (inverted Y axis and potentially margins).
    public float layoutXToPDCoo(float x){
        return startX() + (x / layoutPageWidth() * realWidth());
    }
    public float layoutYToPDCoo(float y){
        return bottomMargin() + realHeight() - (y / layoutPageHeight() * realHeight());
    }
    public float realXToPDCoo(float x){
        return startX() + (x / Element.GRID_WIDTH * realWidth());
    }
    public float realYToPDCoo(float y){
        return bottomMargin() + realHeight() - (y / Element.GRID_HEIGHT * realHeight());
    }
    
    
    // Converts a width or height to the PDPage coordinate space
    public float layoutWToPDCoo(float x){
        return x / layoutPageWidth() * realWidth();
    }
    public float layoutHToPDCoo(float y){
        return y / layoutPageHeight() * realHeight();
    }
    public float realWToPDCoo(float x){
        return x / Element.GRID_WIDTH * realWidth();
    }
    public float realHToPDCoo(float y){
        return y / Element.GRID_HEIGHT * realHeight();
    }
    
    
    // Basic Layout/Real (Grid) converters
    public float layoutXToReal(float x){
        return x / layoutPageWidth() * Element.GRID_WIDTH;
    }
    public float layoutYToReal(float y){
        return y / layoutPageHeight() * Element.GRID_HEIGHT;
    }
    public float realXToLayout(float x){
        return x / Element.GRID_WIDTH * layoutPageWidth();
    }
    public float realYToLayout(float y){
        return y / Element.GRID_HEIGHT * layoutPageHeight();
    }
    
    // Page layout dimensions
    private float layoutPageWidth(){
        return 596f;
    }
    private float layoutPageHeight(){
        return layoutPageWidth() / realWidth() * realHeight();
    }
    
}
