package fr.themsou.panel.leftBar.notes;

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

    public boolean equals(NoteRating noteRating){
        return total == noteRating.total && name.equals(noteRating.name) && index == noteRating.index && parentPath.equals(noteRating.parentPath);
    }
}
