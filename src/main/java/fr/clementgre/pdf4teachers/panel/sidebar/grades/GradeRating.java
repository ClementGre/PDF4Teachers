package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;

import java.util.ArrayList;

public class GradeRating{

    public double originalValue;
    public double total;
    public String name;
    public int index;
    public String parentPath;
    public boolean originalAlwaysVisible;
    public int x;
    public int y;
    public int page;

    public GradeRating(double originalValue, double total, String name, int index, String parentPath, boolean originalAlwaysVisible, int x, int y, int page){
        this.originalValue = originalValue;
        this.total = total;
        this.name = name;
        this.index = index;
        this.parentPath = parentPath;
        this.originalAlwaysVisible = originalAlwaysVisible;
        this.x = x;
        this.y = y;
        this.page = page;
    }

    public GradeElement toGradeElement(double value, boolean alwaysVisible, int x, int y, int page){
        return new GradeElement(x, y, page, false, value, total, index, parentPath, name, alwaysVisible);
    }

    public GradeElement toGradeElement(double value, boolean  alwaysVisible){
        return new GradeElement(x, y, page, false, value, total, index, parentPath, name, alwaysVisible);
    }

    public boolean equals(GradeRating gradeRating){
        return total == gradeRating.total && name.equals(gradeRating.name) && index == gradeRating.index && parentPath.equals(gradeRating.parentPath);
    }
    
    public boolean isEligibleForAlwaysVisible(){
        return originalValue != -1 || originalAlwaysVisible;
    }

    public boolean containsIn(ArrayList<GradeRating> array){
        for(GradeRating element : array){
            if(equals(element)){
                return true;
            }
        }
        return false;
    }

    public GradeElement getSamePathIn(ArrayList<GradeElement> array){

        for(GradeElement element : array){
            if(name.equals(element.getName()) && parentPath.equals(element.getParentPath())){
                return element;
            }
        }
        return null;
    }
}
