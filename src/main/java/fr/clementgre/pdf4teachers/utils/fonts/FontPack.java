package fr.clementgre.pdf4teachers.utils.fonts;

public class FontPack{
    
    private String name;
    private String path;
    
    private String italicPath;
    private String boldPath;
    private String boldItalicPath;
    
    private boolean hasItalic;
    private boolean hasBold;
    private boolean hasBoldItalic;
    
    public FontPack(String name, String path){
        this.name = name;
        this.path = path;
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getPath(){
        return path;
    }
    
    public void setPath(String path){
        this.path = path;
    }
    
    public String getItalicPath(){
        return italicPath;
    }
    
    public void setItalicPath(String italicPath){
        this.italicPath = italicPath;
    }
    
    public String getBoldPath(){
        return boldPath;
    }
    
    public void setBoldPath(String boldPath){
        this.boldPath = boldPath;
    }
    
    public boolean isHasItalic(){
        return hasItalic;
    }
    
    public void setHasItalic(boolean hasItalic){
        this.hasItalic = hasItalic;
    }
    
    public boolean isHasBold(){
        return hasBold;
    }
    
    public void setHasBold(boolean hasBold){
        this.hasBold = hasBold;
    }
    
    public String getBoldItalicPath(){
        return boldItalicPath;
    }
    
    public void setBoldItalicPath(String boldItalicPath){
        this.boldItalicPath = boldItalicPath;
    }
    
    public boolean isHasBoldItalic(){
        return hasBoldItalic;
    }
    
    public void setHasBoldItalic(boolean hasBoldItalic){
        this.hasBoldItalic = hasBoldItalic;
    }
}
