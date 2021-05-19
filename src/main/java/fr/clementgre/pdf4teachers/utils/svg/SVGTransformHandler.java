package fr.clementgre.pdf4teachers.utils.svg;

import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathHandler;

public abstract class SVGTransformHandler implements PathHandler{
    
    protected StringBuilder newPath = new StringBuilder();
    
    public String getTransformedPath(){
        return newPath.toString();
    }
    
    // H, h, V, v are treated as L or l
    protected abstract float manageX(float x, float y, boolean rel);
    protected abstract float manageY(float y, float x, boolean rel);
    
    private float lastX = 0;
    private float lastY = 0;
    
    private float preManageX(float x, float y, boolean rel){
        if(!rel) lastX = x;
        else lastX += x;
        return manageX(x, y, rel);
    }
    private float preManageY(float y, float x, boolean rel){
        if(!rel) lastY = y;
        else lastY += y;
        return manageY(y, x, rel);
    }
    
    @Override
    public void startPath() throws ParseException{
    }
    @Override
    public void endPath() throws ParseException{
    }
    
    @Override
    public void movetoRel(float x, float y) throws ParseException{
        newPath.append("m").append(preManageX(x, y, true)).append(" ").append(preManageY(y, x, true));
    }
    
    @Override
    public void movetoAbs(float x, float y) throws ParseException{
        newPath.append("M").append(preManageX(x, y, false)).append(" ").append(preManageY(y, x, false));
    }
    
    @Override
    public void closePath() throws ParseException{
        newPath.append("z");
    }
    
    @Override
    public void linetoRel(float x, float y) throws ParseException{
        newPath.append("l").append(preManageX(x, y, true)).append(" ").append(preManageY(y, x, true));
    }
    
    @Override
    public void linetoAbs(float x, float y) throws ParseException{
        newPath.append("L").append(preManageX(x, y, false)).append(" ").append(preManageY(y, x, false));
    }
    
    // H, h, V, v are treated as L or l
    @Override
    public void linetoHorizontalRel(float x) throws ParseException{
        newPath.append("l").append(preManageX(x, 0, true)).append(" ").append(preManageY(0, x, true));
    }
    // H, h, V, v are treated as L or l
    @Override
    public void linetoHorizontalAbs(float x) throws ParseException{
        newPath.append("L").append(preManageX(x, lastY, false)).append(" ").append(preManageY(lastY, x, false));
    }
    // H, h, V, v are treated as L or l
    @Override
    public void linetoVerticalRel(float y) throws ParseException{
        newPath.append("l").append(preManageX(0, y, true)).append(" ").append(preManageY(y, 0, true));
    }
    // H, h, V, v are treated as L or l
    @Override
    public void linetoVerticalAbs(float y) throws ParseException{
        newPath.append("L").append(preManageX(lastX, y, false)).append(" ").append(preManageY(y, lastX, false));
    }
    
    @Override
    public void curvetoCubicRel(float x1, float y1, float x2, float y2, float x, float y) throws ParseException{
        newPath.append("c").append(preManageX(x1, y1, true)).append(" ").append(preManageY(y1, x1, true)).append(" ")
                .append(preManageX(x2, y2, true)).append(" ").append(preManageY(y2, x2, true)).append(" ")
                .append(preManageX(x, y, true)).append(" ").append(preManageY(y, x, true));
    }
    
    @Override
    public void curvetoCubicAbs(float x1, float y1, float x2, float y2, float x, float y) throws ParseException{
        newPath.append("C").append(preManageX(x1, y1, false)).append(" ").append(preManageY(y1, x1, false)).append(" ")
                .append(preManageX(x2, y2, false)).append(" ").append(preManageY(y2, x2, false)).append(" ")
                .append(preManageX(x, y, false)).append(" ").append(preManageY(y, x, false));
    }
    
    @Override
    public void curvetoCubicSmoothRel(float x2, float y2, float x, float y) throws ParseException{
        newPath.append("s").append(preManageX(x2, y2, true)).append(" ").append(preManageY(y2, x2, true)).append(" ")
                .append(preManageX(x, y, true)).append(" ").append(preManageY(y, x, true));
    }
    
    @Override
    public void curvetoCubicSmoothAbs(float x2, float y2, float x, float y) throws ParseException{
        newPath.append("S").append(preManageX(x2, y2, false)).append(" ").append(preManageY(y2, x2, false)).append(" ")
                .append(preManageX(x, y, false)).append(" ").append(preManageY(y, x, false));
    }
    
    @Override
    public void curvetoQuadraticRel(float x1, float y1, float x, float y) throws ParseException{
        newPath.append("q").append(preManageX(x1, y1, true)).append(" ").append(preManageY(y1, x1, true)).append(" ")
                .append(preManageX(x, y, true)).append(" ").append(preManageY(y, x, true));
    }
    
    @Override
    public void curvetoQuadraticAbs(float x1, float y1, float x, float y) throws ParseException{
        newPath.append("Q").append(preManageX(x1, y1, false)).append(" ").append(preManageY(y1, x1, false)).append(" ")
                .append(preManageX(x, y, false)).append(" ").append(preManageY(y, x, false));
    }
    
    @Override
    public void curvetoQuadraticSmoothRel(float x, float y) throws ParseException{
        newPath.append("t").append(preManageX(x, y, true)).append(" ").append(preManageY(y, x, true));
    }
    
    @Override
    public void curvetoQuadraticSmoothAbs(float x, float y) throws ParseException{
        newPath.append("T").append(preManageX(x, y, false)).append(" ").append(preManageY(y, x, false));
    }
    
    @Override
    public void arcRel(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException{
        newPath.append("a").append(preManageX(rx, ry, true)).append(" ").append(preManageY(ry, rx, true)).append(" ")
                .append(xAxisRotation).append(" ")
                .append(largeArcFlag ? "1" : "0").append(" ").append(sweepFlag ? "1" : "0").append(" ")
                .append(preManageX(x, y, true)).append(" ").append(preManageY(y, x, true));
    }
    
    @Override
    public void arcAbs(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException{
        newPath.append("A").append(preManageX(rx, ry, false)).append(" ").append(preManageY(ry, rx, false)).append(" ")
                .append(xAxisRotation).append(" ")
                .append(largeArcFlag ? "1" : "0").append(" ").append(sweepFlag ? "1" : "0").append(" ")
                .append(preManageX(x, y, false)).append(" ").append(preManageY(y, x, false));
    }
    
}
