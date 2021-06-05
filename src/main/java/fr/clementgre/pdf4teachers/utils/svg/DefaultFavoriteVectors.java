package fr.clementgre.pdf4teachers.utils.svg;

import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class DefaultFavoriteVectors{
    
    private static final Color CYAN_TRANSPARENT = Color.color(34/255d, 176/255d, 222/255d, .5);
    private static final Color YELLOW_TRANSPARENT = Color.color(1, 1, 0, .4);
    
    private static final VectorData HORIZONTAL_ARROW = new VectorData(20000, 7000, GraphicElement.RepeatMode.STRETCH, GraphicElement.ResizeMode.CORNERS,
            false, CYAN_TRANSPARENT, Color.DARKBLUE, 2, "M0 1L5 1 M5 1 L4 0 M5 1 L4 2", false, false, 0, 0);
    
    private static final VectorData VERTICAL_ARROW = new VectorData(7000, 20000, GraphicElement.RepeatMode.STRETCH, GraphicElement.ResizeMode.CORNERS,
            false, CYAN_TRANSPARENT, Color.DARKBLUE, 2, "M-1 0 L-1 5 M-1 5 L-0 4 M-1 5 L-2 4", false, false, 0, 0);
    
    private static final VectorData LINE = new VectorData(20000, 20000, GraphicElement.RepeatMode.STRETCH, GraphicElement.ResizeMode.OPPOSITE_CORNERS,
            false, CYAN_TRANSPARENT, Color.DARKRED, 2, "M0 1 L1 0", false, false, 0, 0);
    
    private static final VectorData CIRCLE = new VectorData(30000, 30000, GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS,
            false, CYAN_TRANSPARENT, Color.DARKRED, 3, "M1.5,1 C1.5,2 0,2 0,1 C0,0 1.5,0 1.5,1", false, false, 0, 0);
    
    private static final VectorData RECT = new VectorData(40000, 30000, GraphicElement.RepeatMode.STRETCH, GraphicElement.ResizeMode.CORNERS,
            false, CYAN_TRANSPARENT, Color.DARKRED, 3, "M0 0 L1 0 M1 0 L1 1 M1 1 L0 1 M0 1 L0 0", false, false, 0, 0);
    
    private static final VectorData RECT_FILL = new VectorData(40000, 30000, GraphicElement.RepeatMode.STRETCH, GraphicElement.ResizeMode.CORNERS,
            true, CYAN_TRANSPARENT, Color.DARKBLUE, 3, "M0 0 L1 0 M1 0 L1 1 M1 1 L0 1 M0 1 L0 0", false, false, 0, 0);
    
    private static final VectorData SQUARE = new VectorData(30000, 30000, GraphicElement.RepeatMode.KEEP_RATIO, GraphicElement.ResizeMode.CORNERS,
            false, CYAN_TRANSPARENT, Color.DARKRED, 3, "M0 0 L1 0 M1 0 L1 1 M1 1 L0 1 M0 1 L0 0", false, false, 0, 0);
    
    private static final VectorData WAVES = new VectorData(50000, 5000, GraphicElement.RepeatMode.MULTIPLY, GraphicElement.ResizeMode.SIDE_EDGES,
            false, CYAN_TRANSPARENT, Color.DARKRED, 1, "M0 1 L1 0 L2 1 M0 -7", false, false, 0, 0);
    
    private static final VectorData HIGHLIGHTER = new VectorData(50000, 5000, GraphicElement.RepeatMode.STRETCH, GraphicElement.ResizeMode.SIDE_EDGES,
            true, YELLOW_TRANSPARENT, Color.DARKRED, 0, "M0 0 L1 0 1 1 0 1 z", false, false, 0, 0);
    
    private static final VectorData HIGHLIGHTER_CYAN = new VectorData(50000, 5000, GraphicElement.RepeatMode.STRETCH, GraphicElement.ResizeMode.SIDE_EDGES,
            true, CYAN_TRANSPARENT, Color.DARKRED, 0, "M0 0 L1 0 1 1 0 1 z", false, false, 0, 0);
    
    private static final VectorData CHECK = new VectorData(6000, 4474, GraphicElement.RepeatMode.KEEP_RATIO, GraphicElement.ResizeMode.CORNERS,
            true, Color.color(0, 163/255d, 0), Color.GREEN, 0, "M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z", false, false, 0, 0);
    
    private static final VectorData CROSS = new VectorData(5500, 5500, GraphicElement.RepeatMode.KEEP_RATIO, GraphicElement.ResizeMode.CORNERS,
            true, Color.color(192/255d, 0, 0), Color.RED, 0, "M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z", false, false, 0, 0);
    
    private static final VectorData THUMBS_UP = new VectorData(6000, 6399, GraphicElement.RepeatMode.KEEP_RATIO, GraphicElement.ResizeMode.CORNERS,
            true, Color.color(0, 163/255d, 0), Color.GREEN, 0, "M466.27 286.69C475.04 271.84 480 256 480 236.85c0-44.015-37.218-85.58-85.82-85.58H357.7c4.92-12.81 8.85-28.13 8.85-46.54C366.55 31.936 328.86 0 271.28 0c-61.607 0-58.093 94.933-71.76 108.6-22.747 22.747-49.615 66.447-68.76 83.4H32c-17.673 0-32 14.327-32 32v240c0 17.673 14.327 32 32 32h64c14.893 0 27.408-10.174 30.978-23.95 44.509 1.001 75.06 39.94 177.802 39.94 7.22 0 15.22.01 22.22.01 77.117 0 111.986-39.423 112.94-95.33 13.319-18.425 20.299-43.122 17.34-66.99 9.854-18.452 13.664-40.343 8.99-62.99zm-61.75 53.83c12.56 21.13 1.26 49.41-13.94 57.57 7.7 48.78-17.608 65.9-53.12 65.9h-37.82c-71.639 0-118.029-37.82-171.64-37.82V240h10.92c28.36 0 67.98-70.89 94.54-97.46 28.36-28.36 18.91-75.63 37.82-94.54 47.27 0 47.27 32.98 47.27 56.73 0 39.17-28.36 56.72-28.36 94.54h103.99c21.11 0 37.73 18.91 37.82 37.82.09 18.9-12.82 37.81-22.27 37.81 13.489 14.555 16.371 45.236-5.21 65.62zM88 432c0 13.255-10.745 24-24 24s-24-10.745-24-24 10.745-24 24-24 24 10.745 24 24z", false, false, 0, 0);
    
    private static final VectorData THUMBS_DOWN = new VectorData(6000, 6399, GraphicElement.RepeatMode.KEEP_RATIO, GraphicElement.ResizeMode.CORNERS,
            true, Color.color(192/255d, 0, 0), Color.RED, 0, "M466.27 225.31c4.674-22.647.864-44.538-8.99-62.99 2.958-23.868-4.021-48.565-17.34-66.99C438.986 39.423 404.117 0 327 0c-7 0-15 .01-22.22.01C201.195.01 168.997 40 128 40h-10.845c-5.64-4.975-13.042-8-21.155-8H32C14.327 32 0 46.327 0 64v240c0 17.673 14.327 32 32 32h64c11.842 0 22.175-6.438 27.708-16h7.052c19.146 16.953 46.013 60.653 68.76 83.4 13.667 13.667 10.153 108.6 71.76 108.6 57.58 0 95.27-31.936 95.27-104.73 0-18.41-3.93-33.73-8.85-46.54h36.48c48.602 0 85.82-41.565 85.82-85.58 0-19.15-4.96-34.99-13.73-49.84zM64 296c-13.255 0-24-10.745-24-24s10.745-24 24-24 24 10.745 24 24-10.745 24-24 24zm330.18 16.73H290.19c0 37.82 28.36 55.37 28.36 94.54 0 23.75 0 56.73-47.27 56.73-18.91-18.91-9.46-66.18-37.82-94.54C206.9 342.89 167.28 272 138.92 272H128V85.83c53.611 0 100.001-37.82 171.64-37.82h37.82c35.512 0 60.82 17.12 53.12 65.9 15.2 8.16 26.5 36.44 13.94 57.57 21.581 20.384 18.699 51.065 5.21 65.62 9.45 0 22.36 18.91 22.27 37.81-.09 18.91-16.71 37.82-37.82 37.82z", false, false, 0, 0);
    
    private static final VectorData FAT_HORIZONTAL_ARROW = new VectorData(10000, 9746, GraphicElement.RepeatMode.KEEP_RATIO, GraphicElement.ResizeMode.CORNERS,
            true, CYAN_TRANSPARENT, Color.NAVY, 2, "M190.5 66.9l22.2-22.2c9.4-9.4 24.6-9.4 33.9 0L441 239c9.4 9.4 9.4 24.6 0 33.9L246.6 467.3c-9.4 9.4-24.6 9.4-33.9 0l-22.2-22.2c-9.5-9.5-9.3-25 .4-34.3L311.4 296H24c-13.3 0-24-10.7-24-24v-32c0-13.3 10.7-24 24-24h287.4L190.9 101.2c-9.8-9.3-10-24.8-.4-34.3z", false, false, 0, 0);
    
    private static final VectorData FAT_VERTICAL_ARROW = new VectorData(10000, 10259, GraphicElement.RepeatMode.KEEP_RATIO, GraphicElement.ResizeMode.CORNERS,
            true, CYAN_TRANSPARENT, Color.NAVY, 2, "M-66.9 190.5l22.2 22.2c9.4 9.4 9.4 24.6 -0 33.9L-239 441c-9.4 9.4 -24.6 9.4 -33.9 -0L-467.3 246.6c-9.4 -9.4 -9.4 -24.6 0 -33.9l22.2 -22.2c9.5 -9.5 25 -9.3 34.3 0.4L-296 311.4L-296 24c0 -13.3 10.7 -24 24 -24l32 0c13.3 0 24 10.7 24 24l-0 287.4L-101.2 190.9c9.3 -9.8 24.8 -10 34.3 -0.4z", false, false, 0, 0);
    
    public static ArrayList<VectorData> getDefaultFavoriteVectors(){
        ArrayList<VectorData> data = new ArrayList<>();
        
        for(Field field : DefaultFavoriteVectors.class.getDeclaredFields()){
            if(field.getType() == VectorData.class){
                try{
                    data.add((VectorData) field.get(DefaultFavoriteVectors.class));
                }catch(IllegalAccessException e){ e.printStackTrace(); }
            }
        }
        return data;
    }
    
}
