/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.svg;

public class SVGRotateHandler extends SVGTransformHandler{
    
    private final StringBuilder newPath = new StringBuilder();
    
    private final float rotate;
    private final float cosA;
    private final float sinA;
    
    // Rotation in Degrees
    public SVGRotateHandler(float rotate, int decimals){
        super(decimals);
        
        this.rotate = (float) Math.toRadians(rotate);
        this.cosA = (float) Math.cos(this.rotate);
        this.sinA = (float) Math.sin(this.rotate);
    }
    
    public String getScaledPath(){
        return newPath.toString();
    }
    
    
    @Override
    protected float manageX(float x, float y, boolean rel){
        return x * cosA + y * -sinA;
    }
    @Override
    protected float manageY(float y, float x, boolean rel){
        return x * sinA + y * cosA;
    }
}
