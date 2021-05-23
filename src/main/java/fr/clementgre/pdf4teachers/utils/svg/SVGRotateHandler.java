package fr.clementgre.pdf4teachers.utils.svg;

public class SVGRotateHandler extends SVGTransformHandler{
    
    private StringBuilder newPath = new StringBuilder();
    
    private float rotate;
    private float cosA;
    private float sinA;
    
    // Rotation in Degrees
    public SVGRotateHandler(float rotate, boolean formatNumbers){
        super(formatNumbers);
        
        this.rotate = (float) Math.toRadians(rotate);
        this.cosA = (float) Math.cos(this.rotate);
        this.sinA = (float) Math.sin(this.rotate);
    }
    
    public String getScaledPath(){
        return newPath.toString();
    }
    
    
    protected float manageX(float x, float y, boolean rel){
        return x * cosA + y * -sinA;
    }
    protected float manageY(float y, float x, boolean rel){
        return x * sinA + y * cosA;
    }
}
