/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.svg;

import fr.clementgre.pdf4teachers.Main;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathHandler;

import java.text.DecimalFormat;

public abstract class SVGSimpleTransformHandler implements PathHandler{
    
    protected StringBuilder newPath = new StringBuilder();
    
    private DecimalFormat format;
    public SVGSimpleTransformHandler(int decimals){
         if(decimals != -1) {
             format = new DecimalFormat("0." + ("#".repeat(decimals)), Main.baseDecimalFormatSymbols);
         }
    }
    
    public String getTransformedPath(){
        return newPath.toString();
    }
    
    protected abstract float manageX(float x, boolean rel);
    protected abstract float manageY(float y,boolean rel);
    
    protected String preManageX(float x, boolean rel){
        if(format != null){
            return format.format(manageX(x, rel));
        }
        return String.valueOf(manageX(x, rel));
    }
    
    protected String preManageY(float y, boolean rel){
        if(format != null){
            return format.format(manageY(y, rel));
        }
        return String.valueOf(manageY(y, rel));
    }
    
    @Override
    public void startPath() throws ParseException{
    }
    
    @Override
    public void endPath() throws ParseException{
    }
    
    @Override
    public void movetoRel(float x, float y) throws ParseException{
        newPath.append("m").append(preManageX(x, true)).append(" ").append(preManageY(y, true));
    }
    
    @Override
    public void movetoAbs(float x, float y) throws ParseException{
        newPath.append("M").append(preManageX(x, false)).append(" ").append(preManageY(y, false));
    }
    
    @Override
    public void closePath() throws ParseException{
        newPath.append("z");
    }
    
    @Override
    public void linetoRel(float x, float y) throws ParseException{
        newPath.append("l").append(preManageX(x, true)).append(" ").append(preManageY(y, true));
    }
    
    @Override
    public void linetoAbs(float x, float y) throws ParseException{
        newPath.append("L").append(preManageX(x, false)).append(" ").append(preManageY(y, false));
    }
    
    @Override
    public void linetoHorizontalRel(float x) throws ParseException{
        newPath.append("h").append(preManageX(x, true));
    }
    
    @Override
    public void linetoHorizontalAbs(float x) throws ParseException{
        newPath.append("H").append(preManageX(x, false));
    }
    
    @Override
    public void linetoVerticalRel(float y) throws ParseException{
        newPath.append("v").append(preManageY(y, true));
    }
    
    @Override
    public void linetoVerticalAbs(float y) throws ParseException{
        newPath.append("V").append(preManageY(y, false));
    }
    
    @Override
    public void curvetoCubicRel(float x1, float y1, float x2, float y2, float x, float y) throws ParseException{
        newPath.append("c").append(preManageX(x1, true)).append(" ").append(preManageY(y1, true)).append(" ")
                .append(preManageX(x2, true)).append(" ").append(preManageY(y2, true)).append(" ")
                .append(preManageX(x, true)).append(" ").append(preManageY(y, true));
    }
    
    @Override
    public void curvetoCubicAbs(float x1, float y1, float x2, float y2, float x, float y) throws ParseException{
        newPath.append("C").append(preManageX(x1, false)).append(" ").append(preManageY(y1, false)).append(" ")
                .append(preManageX(x2, false)).append(" ").append(preManageY(y2, false)).append(" ")
                .append(preManageX(x, false)).append(" ").append(preManageY(y, false));
    }
    
    @Override
    public void curvetoCubicSmoothRel(float x2, float y2, float x, float y) throws ParseException{
        newPath.append("s").append(preManageX(x2, true)).append(" ").append(preManageY(y2, true)).append(" ")
                .append(preManageX(x, true)).append(" ").append(preManageY(y, true));
    }
    
    @Override
    public void curvetoCubicSmoothAbs(float x2, float y2, float x, float y) throws ParseException{
        newPath.append("S").append(preManageX(x2, false)).append(" ").append(preManageY(y2, false)).append(" ")
                .append(preManageX(x, false)).append(" ").append(preManageY(y, false));
    }
    
    @Override
    public void curvetoQuadraticRel(float x1, float y1, float x, float y) throws ParseException{
        newPath.append("q").append(preManageX(x1, true)).append(" ").append(preManageY(y1, true)).append(" ")
                .append(preManageX(x, true)).append(" ").append(preManageY(y, true));
    }
    
    @Override
    public void curvetoQuadraticAbs(float x1, float y1, float x, float y) throws ParseException{
        newPath.append("Q").append(preManageX(x1, false)).append(" ").append(preManageY(y1, false)).append(" ")
                .append(preManageX(x, false)).append(" ").append(preManageY(y, false));
    }
    
    @Override
    public void curvetoQuadraticSmoothRel(float x, float y) throws ParseException{
        newPath.append("t").append(preManageX(x, true)).append(" ").append(preManageY(y, true));
    }
    
    @Override
    public void curvetoQuadraticSmoothAbs(float x, float y) throws ParseException{
        newPath.append("T").append(preManageX(x, false)).append(" ").append(preManageY(y, false));
    }
    
    @Override
    public void arcRel(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException{
        newPath.append("a").append(preManageX(rx, true)).append(" ").append(preManageY(ry, true)).append(" ")
                .append(xAxisRotation).append(" ")
                .append(largeArcFlag ? "1" : "0").append(" ").append(sweepFlag ? "1" : "0").append(" ")
                .append(preManageX(x, true)).append(" ").append(preManageY(y, true));
    }
    
    @Override
    public void arcAbs(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException{
        newPath.append("A").append(preManageX(rx, false)).append(" ").append(preManageY(ry, false)).append(" ")
                .append(xAxisRotation).append(" ")
                .append(largeArcFlag ? "1" : "0").append(" ").append(sweepFlag ? "1" : "0").append(" ")
                .append(preManageX(x, false)).append(" ").append(preManageY(y, false));
    }
}
