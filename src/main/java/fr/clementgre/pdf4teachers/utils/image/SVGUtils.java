package fr.clementgre.pdf4teachers.utils.image;

import org.apache.batik.parser.PathParser;

public class SVGUtils{
    
    public static String transformPath(String path, float xFactor, float yFactor, float translateX, float translateY,
                                       boolean invertX, boolean invertY, float width, float height){
        
        PathParser parser = new PathParser();
        SVGScalerHandler handler = new SVGScalerHandler(xFactor, yFactor, translateX, translateY, invertX, invertY, width, height);
        parser.setPathHandler(handler);
        parser.parse(path);
        
        return handler.getScaledPath();
    }
    
}
