package fr.clementgre.pdf4teachers.utils.image;

import fr.clementgre.pdf4teachers.Main;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;

public class ImageUtils {

    public static ColorAdjust defaultFullDarkColorAdjust = new ColorAdjust();
    public static ColorAdjust defaultDarkColorAdjust = new ColorAdjust();
    public static ColorAdjust defaultGrayColorAdjust = new ColorAdjust();
    public static ColorAdjust defaultWhiteColorAdjust = new ColorAdjust();

    public static void setupListeners(){
        Main.settings.darkThemeProperty().addListener((observable, oldValue, newValue) -> updateColorsAdjust());
        updateColorsAdjust();
    }
    private static void updateColorsAdjust(){
        if(Main.settings.isDarkTheme()){
            defaultFullDarkColorAdjust.setBrightness(1);
            defaultDarkColorAdjust.setBrightness(0.8);
            defaultGrayColorAdjust.setBrightness(0.8);
            defaultWhiteColorAdjust.setBrightness(0);
        }else{
            defaultFullDarkColorAdjust.setBrightness(-1);
            defaultDarkColorAdjust.setBrightness(0.25);
            defaultGrayColorAdjust.setBrightness(0);
            defaultWhiteColorAdjust.setBrightness(-1);
        }
    }

    public static ImageView buildImage(Image image, int width, int height) {
        ImageView imageView = new ImageView(image);

        if(width == 0 && height == 0) return imageView;

        if(width == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }

    public static ImageView buildImage(String imgPath, int width, int height) {
        return buildImage(imgPath, width, height, null);
    }

    public static ImageView buildImage(String imgPath, int width, int height, Effect effect) {
        ImageView imageView = new ImageView(new Image(imgPath));

        if(effect != null) imageView.setEffect(effect);

        if(width == 0 && height == 0) return imageView;

        if(width == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }

    public static ImageView buildImage(FileInputStream file, int width, int height) {
        ImageView imageView = new ImageView(new Image(file));

        if(width == 0 && height == 0) return imageView;

        if(width == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }
}
