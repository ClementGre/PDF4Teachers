package fr.clementgre.pdf4teachers.utils.svg;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.jetbrains.annotations.NotNull;

import java.awt.Shape;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

public class SVGUtils{
    
    public static final List<String> ACCEPTED_EXTENSIONS = Collections.singletonList("svg");
    
    public static String transformPath(String path, float xFactor, float yFactor, float translateX, float translateY,
                                       boolean invertX, boolean invertY, float width, float height){
        
        PathParser parser = new PathParser();
        SVGScalerHandler handler = new SVGScalerHandler(xFactor, yFactor, translateX, translateY, invertX, invertY, width, height);
        parser.setPathHandler(handler);
        parser.parse(path);
        
        return handler.getTransformedPath();
    }
    
    public static String rotatePath(String path, float degAngle){
        PathParser parser = new PathParser();
        SVGRotateHandler handler = new SVGRotateHandler(degAngle);
        parser.setPathHandler(handler);
        parser.parse(path);
    
        return handler.getTransformedPath();
    }
    
    public static Shape convertToAwtShape(String path){
        PathParser parser = new PathParser();
        AWTPathProducer producer = new AWTPathProducer();
    
        producer.setWindingRule(0);
        parser.setPathHandler(producer);
        parser.parse(path);
    
        return producer.getShape();
    }
    
}
