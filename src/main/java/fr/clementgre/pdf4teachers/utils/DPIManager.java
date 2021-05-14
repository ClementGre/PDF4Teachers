package fr.clementgre.pdf4teachers.utils;

public class DPIManager{
    
    // 1 in = 25.4 mm
    // The width of 1 cm
    private double oneCmWidth = 1;
    
    private int dpi;
    
    public DPIManager(int dpi){
        this.dpi = dpi;
    }
    
    public void initOneCmWidth(double oneCmWidth){
        this.oneCmWidth = oneCmWidth;
    }
    public void initOneCmWidth(double width, double cmWidth){
        this.oneCmWidth = width / cmWidth;
    }
    public void initOneCmWidthFromA4Width(double width){
        this.oneCmWidth = width / 21; // A4 width = 21cm
    }
    
    public int getPixelsLength(double width){
        double mmWidth = 10d * width / oneCmWidth;
    
        return (int) (dpi * mmWidth / 25.4);
    }
}
