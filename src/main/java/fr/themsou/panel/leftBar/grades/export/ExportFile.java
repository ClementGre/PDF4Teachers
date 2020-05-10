package fr.themsou.panel.leftBar.grades.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.panel.leftBar.grades.GradeRating;
import fr.themsou.panel.leftBar.grades.GradeTreeView;
import fr.themsou.utils.Builders;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

        grades.sort(Comparator.comparing(grade -> {

            String[] parentPath = Builders.cleanArray(grade.getParentPath().split(Pattern.quote("\\")));
            String lastParentPath = grade.getParentPath();

            StringBuilder indexes = new StringBuilder(grade.getIndex() + "");

            while(parentPath.length != 0){
                for(GradeElement parent : grades){
                    if((parent.getParentPath() + "\\" + parent.getName()).equals(lastParentPath)){
                        indexes.insert(0, parent.getIndex());
                        lastParentPath = "\\" + String.join("\\", parentPath);
                        parentPath = Builders.cleanArray(parent.getParentPath().split(Pattern.quote("\\")));
                    }
                }
            }
            return indexes.toString();
        }));
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
