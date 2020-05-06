package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.elements.NoteElement;

import java.util.ArrayList;

public class NoteRating{

    public double total;
    public String name;
    public int index;
    public String parentPath;

    public NoteRating(double total, String name, int index, String parentPath) {
        this.total = total;
        this.name = name;
        this.index = index;
        this.parentPath = parentPath;
    }

    public NoteElement toNoteElement(){
        return toNoteElement(-1, 0, 0, 0);
    }
    public NoteElement toNoteElement(double value, int x, int y, int page){
        return new NoteElement(x, y, name, value, total, index, parentPath, page, null);
    }

    public boolean equals(NoteRating noteRating){
        return total == noteRating.total && name.equals(noteRating.name) && index == noteRating.index && parentPath.equals(noteRating.parentPath);
    }

    public boolean containsIn(ArrayList<NoteRating> array){
        for(NoteRating element : array){
            if(equals(element)){
                return true;
            }
        }
        return false;
    }

    public NoteElement getSamePathIn(ArrayList<NoteElement> array){

        for(NoteElement element : array){
            if(name.equals(element.getName()) && parentPath.equals(element.getParentPath())){
                return element;
            }
        }
        return null;
    }
}
