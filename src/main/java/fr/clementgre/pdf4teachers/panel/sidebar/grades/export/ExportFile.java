package fr.clementgre.pdf4teachers.panel.sidebar.grades.export;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeRating;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class ExportFile{

    public File file;

    public List<GradeElement> grades = new ArrayList<>();
    public List<TextElement> comments;

    public ExportFile(File file, int exportTier, boolean comments) throws Exception {
        this.file = file;

        if(comments) this.comments = new ArrayList<>();
        File editFile = Edition.getEditFile(file);

        Element[] elements = Edition.simpleLoad(editFile);
        for(Element element : elements){
            if(element instanceof GradeElement){
                grades.add(((GradeElement) element));

            }else if(comments && element instanceof TextElement){
                this.comments.add(((TextElement) element));
            }

        }

        grades.removeIf(grade -> GradeTreeView.getElementTier(grade.getParentPath()) >= exportTier);

        grades = GradeElement.sortGrades(grades);
    }

    private int getGradeSortIndex(GradeElement grade){
        String[] parentPath = StringUtils.cleanArray(grade.getParentPath().split(Pattern.quote("\\")));

        if(grade.isRoot()) return 0;

        int index = (int) (-grade.getIndex() * Math.pow(10, parentPath.length));

        for(GradeElement parent : grades){
            if((parent.getParentPath() + "\\" + parent.getName()).equals(grade.getParentPath())){ // parent is direct parent of grade
                index += getGradeSortIndex(parent);
            }
        }

        return index;
    }

    public boolean isSameGradeScale(ArrayList<GradeRating> gradeScale){
        int i = 0;
        for(GradeElement grade : grades){
            if(!grade.toGradeRating().containsIn(gradeScale)){
                return false;
            }
            i++;
        }
        return i == gradeScale.size();
    }

    public ArrayList<GradeRating> generateGradeScale(){

        ArrayList<GradeRating> gradesRating = new ArrayList<>();
        for(GradeElement grade : grades){
            gradesRating.add(grade.toGradeRating());
        }
        return gradesRating;
    }

    public boolean isCompleted() {

        for(GradeElement grade : grades){
            if(grade.getValue() == -1) return false;
        }
        return true;

    }
}
