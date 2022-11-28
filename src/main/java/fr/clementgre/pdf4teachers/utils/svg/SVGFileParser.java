/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.svg;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SVGFileParser{
    
    private final File file;
    public SVGFileParser(File file){
        this.file = file;
    }
    
    private Document document;
    private XPath xpath;
    
    public void load() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(file);
    
        XPathFactory xpf = XPathFactory.newInstance();
        xpath = xpf.newXPath();
    }
    
    public String getPath() throws XPathExpressionException{
        XPathExpression expression = xpath.compile("//path/@d");
        NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
    
        return IntStream.range(0, svgPaths.getLength())
                .mapToObj(i -> svgPaths.item(i).getNodeValue())
                .collect(Collectors.joining());
    }
    
    public Color getFillColor() throws XPathExpressionException{
        XPathExpression expression = xpath.compile("//path/@fill");
        return getColor(expression);
    }
    
    public Color getStrokeColor() throws XPathExpressionException{
        XPathExpression expression = xpath.compile("//path/@stroke");
        return getColor(expression);
    }
    
    @Nullable
    private Color getColor(XPathExpression expression) throws XPathExpressionException{
        NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        
        Color finalColor = null;
        for(int i = 0; i < svgPaths.getLength() ; i++){
            try{
                finalColor = Color.valueOf(svgPaths.item(i).getNodeValue());
            }catch(NullPointerException | IllegalArgumentException ignored){}
        }
        return finalColor;
    }
    
    public int getStrokeWidth() throws XPathExpressionException{
        XPathExpression expression = xpath.compile("//path/@stroke-width");
        NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        
        int finalStrokeWidth = 0;
        for(int i = 0; i < svgPaths.getLength() ; i++){
            try{
                finalStrokeWidth = Integer.parseInt(svgPaths.item(i).getNodeValue());
            }catch(NullPointerException | IllegalArgumentException ignored){}
        }
        
        return finalStrokeWidth;
    }
    
}
