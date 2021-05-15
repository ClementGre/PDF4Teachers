package fr.clementgre.pdf4teachers.utils.image;

import fr.clementgre.pdf4teachers.Main;

import java.util.Arrays;

public class SVGUtils{
    
    // NOT WORK
    
    public static String scalePath(String path, double xFactor, double yFactor){
        
        char[] chars = path.toCharArray();
        StringBuilder newPath = new StringBuilder();
        
        String currentAction = "";
        boolean pathNumber = false;
        boolean xAxis = true;
        for(int i = 0; i < chars.length; i++){
            
            if(isPathNumber(chars[i])){
                currentAction += chars[i];
            }else{
                currentAction += chars[i];
            }
            if(i+1 >= chars.length){
                if(pathNumber){
                    newPath.append(Main.baseFormat.format(Double.parseDouble(currentAction) * (xAxis ? xFactor : yFactor) ));
                }else{
                    newPath.append(currentAction);
                }
                
                break;
            }else if(pathNumber){
                if(chars[i+1] == '-'){
                    newPath.append(Main.baseFormat.format(Double.parseDouble(currentAction) * (xAxis ? xFactor : yFactor) ));
                    currentAction = "-";
                    pathNumber = false;
                    xAxis = !xAxis;
                }
                
                if(!isPathNumber(chars[i+1])){
                    newPath.append(Main.baseFormat.format(Double.parseDouble(currentAction) * (xAxis ? xFactor : yFactor) ));
                    currentAction = "";
                    pathNumber = false;
                    xAxis = !xAxis;
                }
            }else{
                if(chars[i+1] == '-'){
                    newPath.append(currentAction);
                    currentAction = "-";
                    pathNumber = true;
                }
                
                if(isPathNumber(chars[i+1])){
                    newPath.append(currentAction);
                    currentAction = "";
                    pathNumber = true;
                }
            }
            
        }
    
        //System.out.println(newPath);
        return newPath.toString();
    }
    
    private static boolean isPathNumber(char pathSection){
        return String.valueOf(pathSection).matches("[0-9.-]");
    }
    
}
