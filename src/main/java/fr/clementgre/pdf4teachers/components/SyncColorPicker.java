/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.utils.interfaces.NonLeakingListener;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SyncColorPicker extends ColorPicker {
    
    private static final ArrayList<WeakReference<ColorPicker>> colorPickers = new ArrayList<>();
    private static ArrayList<Color> customColors = new ArrayList<>();
    
    public SyncColorPicker(){
        this(Color.WHITE);
    }
    
    public SyncColorPicker(Color color){
        super(color);
        colorPickers.add(new WeakReference<>(this));
        
        getCustomColors().setAll(customColors);
        
        valueProperty().addListener(new NonLeakingListener<>(this, SyncColorPicker::updateFavoriteColors));
        setOnHiding((value) -> updateFavoriteColors(
                new NonLeakingListener.ChangeEvent<>(null, null, null, this))
        );
    }
    
    public static void updateFavoriteColors(NonLeakingListener.ChangeEvent<Color, SyncColorPicker> changeEvent){
        customColors = new ArrayList<>(changeEvent.element().getCustomColors());
        updateColorPickersFavorites();
    }
    
    public static void updateColorPickersFavorites(){
        for(WeakReference<ColorPicker> colorPicker : colorPickers){
            if(colorPicker.get() != null){
                Objects.requireNonNull(colorPicker.get()).getCustomColors().setAll(customColors);
            }
        }
    }
    
    public static void loadCustomsColors(List<String> colors){
        customColors = colors.stream().map(s -> {
            try{
                return Color.valueOf(s);
            }catch(IllegalArgumentException ignored){
            }
            return Color.BLACK;
        }).collect(Collectors.toCollection(ArrayList::new));
        
        updateColorPickersFavorites();
    }
    
    public static List<String> getCustomColorsList(){
        return customColors.stream().map(Color::toString).collect(Collectors.toList());
    }
}
