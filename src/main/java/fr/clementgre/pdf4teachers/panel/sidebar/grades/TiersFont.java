package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.utils.FontUtils;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TiersFont {

    private Font font;
    private Color color;
    private boolean showName;
    private boolean hide;

    public TiersFont(Font font, Color color, boolean showName, boolean hide) {
        this.font = font;
        this.color = color;
        this.showName = showName;
        this.hide = hide;
    }

    public static TiersFont getInstance(HashMap<String, Object> data){
        InputStream fontFile = FontUtils.getFontFile(Config.getString(data, "font"), Config.getBoolean(data, "italic"), Config.getBoolean(data, "bold"));
        return new TiersFont(
                Font.loadFont(fontFile,  Config.getDouble(data, "size")),
                Color.valueOf(Config.getString(data, "color")),
                Config.getBoolean(data, "showName"),
                Config.getBoolean(data, "hide"));
    }
    public LinkedHashMap<String, Object> getData(){
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("font", font.getFamily());
        data.put("italic", FontUtils.getFontPosture(font) == FontPosture.ITALIC);
        data.put("bold", FontUtils.getFontWeight(font) == FontWeight.BOLD);
        data.put("size", font.getSize());
        data.put("color", color.toString());
        data.put("showName", showName);
        data.put("hide", hide);
        return data;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }
}
