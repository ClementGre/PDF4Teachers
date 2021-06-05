package fr.clementgre.pdf4teachers.utils.svg;

import org.apache.batik.parser.ParseException;

public class SVGArrowsCreatorHandler extends SVGTransformHandler{
    
    
    private float arrowLength;
    private float angle;
    public SVGArrowsCreatorHandler(boolean formatNumbers, float arrowLength, float degAngle){
        super(formatNumbers);
        this.arrowLength = arrowLength;
        this.angle = (float) Math.toRadians(180-degAngle);
    }
    
    @Override
    protected float manageX(float x, float y, boolean rel){
        return x;
    }
    
    @Override
    protected float manageY(float y, float x, boolean rel){
        return y;
    }
    
    @Override
    public void linetoRel(float x, float y) throws ParseException{
        float originX = lastX;
        float originY = lastY;
        
        super.linetoRel(x, y);
        
        makeArrow(originX, originY, lastX, lastY);
    }
    
    @Override
    public void linetoAbs(float x, float y) throws ParseException{
        float originX = lastX;
        float originY = lastY;
        
        super.linetoAbs(x, y);
        
        makeArrow(originX, originY, x, y);
    }
    
    private void makeArrow(float originX, float originY, float x, float y){
        
        float moveX = x - originX;
        float moveY = y - originY;
        
        // Calcul de l'angle de la ligne sur le plan
        double arrowAngle = -Math.atan(moveY / moveX);
        if(moveX < 0) arrowAngle += Math.PI;
        
        // Coordonnés du bout de la première branche
        double arX1 = x + arrowLength * Math.cos(angle + arrowAngle);
        double arY1 = y - arrowLength * Math.sin(angle + arrowAngle);
    
        // Coordonnés du bout de la seconde branche
        double arX2 = x + arrowLength * Math.cos(-angle + arrowAngle);
        double arY2 = y - arrowLength * Math.sin(-angle + arrowAngle);
        
        newPath.append("M").append(x).append(" ").append(y)
                .append("L").append(arX1).append(" ").append(arY1)
                .append("M").append(x).append(" ").append(y)
                .append("L").append(arX2).append(" ").append(arY2)
                .append("M").append(x).append(" ").append(y);
    }
    
}
