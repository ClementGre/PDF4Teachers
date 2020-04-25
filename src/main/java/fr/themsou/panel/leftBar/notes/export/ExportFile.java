package fr.themsou.panel.leftBar.notes.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.panel.leftBar.notes.NoteRating;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.utils.Builders;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class ExportFile{

    public File file;

    public List<NoteElement> notes = new ArrayList<>();
    public List<TextElement> comments;

    public ExportFile(File file, int exportTier, boolean comments) throws Exception {
        this.file = file;

        if(comments) this.comments = new ArrayList<>();
        File editFile = Edition.getEditFile(file);

        Element[] elements = Edition.simpleLoad(editFile);
        for(Element element : elements){
            if(element instanceof NoteElement){
                notes.add(((NoteElement) element));

            }else if(comments && element instanceof TextElement){
                this.comments.add(((TextElement) element));
            }

        }

        notes.removeIf(note -> NoteTreeView.getElementTier(note.getParentPath()) >= exportTier);

        notes.sort(Comparator.comparing(note -> {

            String[] parentPath = Builders.cleanArray(note.getParentPath().split(Pattern.quote("\\")));
            String lastParentPath = note.getParentPath();

            StringBuilder indexes = new StringBuilder(note.getIndex() + "");

            while(parentPath.length != 0){
                for(NoteElement parent : notes){
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

    public boolean isSameRatingScale(ArrayList<NoteRating> ratingScale){
        for(NoteElement note : notes){
            if(!containsRatingScale(ratingScale, note.toNoteRating())){
                return false;
            }
        }
        return true;
    }
    public boolean containsRatingScale(ArrayList<NoteRating> array, NoteRating noteRating){
        for(NoteRating element : array){
            if(element.equals(noteRating)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<NoteRating> generateRatingScale(){

        ArrayList<NoteRating> notesRating = new ArrayList<>();
        for(NoteElement note : notes){
            notesRating.add(note.toNoteRating());
        }
        return notesRating;
    }

    public boolean isCompleted() {

        for(NoteElement note : notes){
            if(note.getValue() == -1) return false;
        }
        return true;

    }
}
