/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.svg;

public class SVGScalerHandler extends SVGSimpleTransformHandler{
    
    private float scaleX, scaleY, translateX, translateY;
    
    // Translate Y/X should be relative to original coordinates
    // Width and Height too.
    public SVGScalerHandler(float scaleX, float scaleY, float translateX, float translateY, int decimals){
        super(decimals);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translateX = translateX;
        this.translateY = translateY;
    }
    public SVGScalerHandler(float scaleX, float scaleY, float translateX, float translateY,
                            boolean invertX, boolean invertY, float currentWidth, float currentHeight, int decimals){
        super(decimals);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translateX = translateX;
        this.translateY = translateY;
        
        if(invertX){
            this.translateX -= currentWidth;
            this.scaleX = -this.scaleX;
        }
        if(invertY){
            this.translateY -= currentHeight;
            this.scaleY = -this.scaleY;
        }
    }
    
    @Override
    protected float manageX(float x, boolean rel){
        if(rel) return x * scaleX;
        return (x+translateX) * scaleX;
    }
    @Override
    protected float manageY(float y, boolean rel){
        if(rel) return y * scaleY;
        return (y+translateY) * scaleY;
    }
}
