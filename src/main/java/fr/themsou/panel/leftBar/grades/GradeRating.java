package fr.themsou.panel.leftBar.grades;

import fr.themsou.document.editions.elements.GradeElement;

import java.util.ArrayList;

public class GradeRating {

    public double total;
    public String name;
    public int index;
    public String parentPath;

    public GradeRating(double total, String name, int index, String parentPath) {
        this.total = total;
        this.name = name;
        this.index = index;
        this.parentPath = parentPath;
    }

    public GradeElement toGradeElement(){
        return toGradeElement(-1, 0, 0, 0);
    }
    public GradeElement toGradeElement(double value, int x, int y, int page){
        return new GradeElement(x, y, page, name, value, total, index, parentPath, false);
    }

    public boolean equals(GradeRating gradeRating){
        return total == gradeRating.total && name.equals(gradeRating.name) && index == gradeRating.index && parentPath.equals(gradeRating.parentPath);
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
