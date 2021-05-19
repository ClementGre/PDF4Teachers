package fr.clementgre.pdf4teachers.utils.svg;

import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathHandler;

public abstract class SVGSimpleTransformHandler implements PathHandler{
    
    protected StringBuilder newPath = new StringBuilder();
    
    public String getTransformedPath(){
        return newPath.toString();
    }
    
    protected abstract float manageX(float x, boolean rel);
    
    protected abstract float manageY(float y, boolean rel);
    
    @Override
    public void startPath() throws ParseException{
    }
    
    @Override
    public void endPath() throws ParseException{
    }
    
    @Override
    public void movetoRel(float x, float y) throws ParseException{
        newPath.append("m").append(manageX(x, true)).append(" ").append(manageY(y, true));
    }
    
    @Override
    public void movetoAbs(float x, float y) throws ParseException{
        newPath.append("M").append(manageX(x, false)).append(" ").append(manageY(y, false));
    }
    
    @Override
    public void closePath() throws ParseException{
        newPath.append("z");
    }
    
    @Override
    public void linetoRel(float x, float y) throws ParseException{
        newPath.append("l").append(manageX(x, true)).append(" ").append(manageY(y, true));
    }
    
    @Override
    public void linetoAbs(float x, float y) throws ParseException{
        newPath.append("L").append(manageX(x, false)).append(" ").append(manageY(y, false));
    }
    
    @Override
    public void linetoHorizontalRel(float x) throws ParseException{
        newPath.append("h").append(manageX(x, true));
    }
    
    @Override
    public void linetoHorizontalAbs(float x) throws ParseException{
        newPath.append("H").append(manageX(x, false));
    }
    
    @Override
    public void linetoVerticalRel(float y) throws ParseException{
        newPath.append("v").append(manageY(y, true));
    }
    
    @Override
    public void linetoVerticalAbs(float y) throws ParseException{
        newPath.append("V").append(manageY(y, false));
    }
    
    @Override
    public void curvetoCubicRel(float x1, float y1, float x2, float y2, float x, float y) throws ParseException{
        newPath.append("c").append(manageX(x1, true)).append(" ").append(manageY(y1, true)).append(" ")
                .append(manageX(x2, true)).append(" ").append(manageY(y2, true)).append(" ")
                .append(manageX(x, true)).append(" ").append(manageY(y, true));
    }
    
    @Override
    public void curvetoCubicAbs(float x1, float y1, float x2, float y2, float x, float y) throws ParseException{
        newPath.append("C").append(manageX(x1, false)).append(" ").append(manageY(y1, false)).append(" ")
                .append(manageX(x2, false)).append(" ").append(manageY(y2, false)).append(" ")
                .append(manageX(x, false)).append(" ").append(manageY(y, false));
    }
    
    @Override
    public void curvetoCubicSmoothRel(float x2, float y2, float x, float y) throws ParseException{
        newPath.append("s").append(manageX(x2, true)).append(" ").append(manageY(y2, true)).append(" ")
                .append(manageX(x, true)).append(" ").append(manageY(y, true));
    }
    
    @Override
    public void curvetoCubicSmoothAbs(float x2, float y2, float x, float y) throws ParseException{
        newPath.append("S").append(manageX(x2, false)).append(" ").append(manageY(y2, false)).append(" ")
                .append(manageX(x, false)).append(" ").append(manageY(y, false));
    }
    
    @Override
    public void curvetoQuadraticRel(float x1, float y1, float x, float y) throws ParseException{
        newPath.append("q").append(manageX(x1, true)).append(" ").append(manageY(y1, true)).append(" ")
                .append(manageX(x, true)).append(" ").append(manageY(y, true));
    }
    
    @Override
    public void curvetoQuadraticAbs(float x1, float y1, float x, float y) throws ParseException{
        newPath.append("Q").append(manageX(x1, false)).append(" ").append(manageY(y1, false)).append(" ")
                .append(manageX(x, false)).append(" ").append(manageY(y, false));
    }
    
    @Override
    public void curvetoQuadraticSmoothRel(float x, float y) throws ParseException{
        newPath.append("t").append(manageX(x, true)).append(" ").append(manageY(y, true));
    }
    
    @Override
    public void curvetoQuadraticSmoothAbs(float x, float y) throws ParseException{
        newPath.append("T").append(manageX(x, false)).append(" ").append(manageY(y, false));
    }
    
    @Override
    public void arcRel(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException{
        newPath.append("a").append(manageX(rx, true)).append(" ").append(manageY(ry, true)).append(" ")
                .append(xAxisRotation).append(" ")
                .append(largeArcFlag ? "1" : "0").append(" ").append(sweepFlag ? "1" : "0").append(" ")
                .append(manageX(x, true)).append(" ").append(manageY(y, true));
    }
    
    @Override
    public void arcAbs(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException{
        newPath.append("A").append(manageX(rx, false)).append(" ").append(manageY(ry, false)).append(" ")
                .append(xAxisRotation).append(" ")
                .append(largeArcFlag ? "1" : "0").append(" ").append(sweepFlag ? "1" : "0").append(" ")
                .append(manageX(x, false)).append(" ").append(manageY(y, false));
    }
}
