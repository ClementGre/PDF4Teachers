package fr.clementgre.pdf4teachers.components;

import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SyncColorPicker extends ColorPicker {

    private static ArrayList<ColorPicker> colorPickers = new ArrayList<>();
    private static ArrayList<Color> customColors = new ArrayList<>();

    public SyncColorPicker(){
        super();

        colorPickers.add(this);

        getCustomColors().setAll(customColors);

        valueProperty().addListener((observable, oldValue, newValue) -> {
            customColors = new ArrayList<>(getCustomColors());
            for(ColorPicker colorPicker : colorPickers){
                if(colorPicker != this) colorPicker.getCustomColors().setAll(customColors);
            }
        });
        setOnHiding((value) -> {
            customColors = new ArrayList<>(getCustomColors());
            for (ColorPicker colorPicker : colorPickers) {
                if (colorPicker != this) colorPicker.getCustomColors().setAll(customColors);
            }
        });
    }

    public static void loadCustomsColors(List<String> colors){
        customColors = colors.stream().map(s -> {
            try{ return Color.valueOf(s); }catch(IllegalArgumentException ignored){}
            return Color.BLACK;
        }).collect(Collectors.toCollection(ArrayList::new));

        for(ColorPicker colorPicker : colorPickers){
            colorPicker.getCustomColors().setAll(customColors);
        }
    }
    public static List<String> getCustomColorsList() {
        return customColors.stream().map(Color::toString).collect(Collectors.toList());
    }
}
