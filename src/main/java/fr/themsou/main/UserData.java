package fr.themsou.main;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.panel.leftBar.notes.LBNoteTab;
import fr.themsou.panel.leftBar.texts.TextListItem;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.windows.MainWindow;
import fr.themsou.yaml.Config;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.util.*;

public class UserData {

    private class DataType{
        public static final int SIMPLE_DATA = 0;
        public static final int TEXT_ELEMENT_FAVORITE = 1;
        public static final int TEXT_ELEMENT_LAST = 2;
        public static final int TEXT_ELEMENT_LIST = 3;
    }

    public static File lastOpenDir = new File(System.getProperty("user.home"));

    // Notes ExportParams

    public String lastExportFileName = "";
    public String lastExportFileNamePrefix = "";
    public String lastExportFileNameSuffix = "";
    public String lastExportFileNameReplace = "";
    public String lastExportFileNameBy = "";
    public String lastExportStudentNameReplace = "";
    public String lastExportStudentNameBy = "";
    public boolean settingsOnlySameRatingScale = true;
    public boolean settingsOnlyCompleted = false;
    public boolean settingsOnlySameDir = false;
    public boolean settingsAttributeTotalLine = false;
    public boolean settingsAttributeMoyLine = true;
    public boolean settingsWithTxtElements = false;
    public int settingsTiersExportSlider = 2;

    public UserData(){

        if(Main.settings.getSettingsVersion().isEmpty()){
            return;
        }else if(Main.settings.getSettingsVersion().equals("1.2.0") ||
                Main.settings.getSettingsVersion().equals("Snapshot 1.2.0")){
            loadDataFromYAML();
        }else{
            loadDataFromHEX();
        }
    }

    private void loadDataFromYAML(){

        try {
            new File(Main.dataFolder).mkdirs();
            File file = new File(Main.dataFolder + "userdata.yml");
            if(file.createNewFile()) return; // File does not exist or can't create it

            Config config = new Config(file);
            config.load();

            // Text Elements Lists & lastFont

            HashMap<String, Object> texts = config.getSection("texts");

            for(Object data : Config.getList(texts, "favorites")){
                if(data instanceof Map) MainWindow.lbTextTab.favoritesText.getChildren().add(TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeItem.FAVORITE_TYPE));
            }

            for(Object data : Config.getList(texts, "lasts")){
                if(data instanceof Map) MainWindow.lbTextTab.lastsText.getChildren().add(TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeItem.LAST_TYPE));
            }

            for(Map.Entry<String, Object> list : Config.getSection(texts, "lists").entrySet()){
                if(list.getValue() instanceof List){
                    ArrayList<TextListItem> listTexts = new ArrayList<>();
                    for(Object data : ((List<Object>) list.getValue())){
                        listTexts.add(TextListItem.readYAMLDataAndGive(Config.castSection(data)));
                    }
                    MainWindow.lbTextTab.favoriteLists.put(list.getKey(), listTexts);
                }
            }

            HashMap<String, Object> lastTextFont = Config.getSection(texts, "lastFont");
            if(lastTextFont.size() != 5) return;
            MainWindow.lbTextTab.lastFont = Config.getString(lastTextFont, "font");
            MainWindow.lbTextTab.lastFontSize = (int) Config.getDouble(lastTextFont, "size");
            MainWindow.lbTextTab.lastColor = Config.getString(lastTextFont, "color");
            MainWindow.lbTextTab.lastBold = Config.getBoolean(lastTextFont, "bold");
            MainWindow.lbTextTab.lastItalic = Config.getBoolean(lastTextFont, "italic");

            // Single Data

            lastOpenDir = new File(config.getString("lastOpenDir"));

            // TIERS FONTS (NOTE_TAB) + lock

            HashMap<String, Object> notes = config.getSection("notes");

            int i = 0;
            for(Object font : Config.getSection(notes, "tiersFont").values()){
                if(font instanceof Map){
                    HashMap<String, Object> data = (HashMap<String, Object>) font;
                    LBNoteTab.fontTiers.put(i, Map.entry(
                            Font.loadFont(
                                    Element.getFontFile(Config.getString(data, "font"), Config.getBoolean(data, "italic"), Config.getBoolean(data, "bold")),
                                    Config.getDouble(data, "size")), // Font + Size
                            Map.entry(
                                    Color.valueOf(Config.getString(data, "color")),
                                    Config.getBoolean(data, "showName")
                            ))); // Color + ShowName
                }
                i++;
            }


            MainWindow.lbNoteTab.lockRatingScale.setSelected(Config.getBoolean(notes, "lockRatingScale"));

            // ExportParams

            HashMap<String, Object> exportParams = config.getSection("export");

            HashMap<String, Object> exportFields = Config.getSection(exportParams, "fields");
            lastExportFileName = Config.getString(exportFields, "fileName");
            lastExportFileName = Config.getString(exportFields, "fileName");
            lastExportFileNameReplace = Config.getString(exportFields, "fileNameReplace");
            lastExportFileNameBy = Config.getString(exportFields, "fileNameBy");
            lastExportFileNameSuffix = Config.getString(exportFields, "fileNameSuffix");
            lastExportFileNamePrefix = Config.getString(exportFields, "fileNamePrefix");
            lastExportStudentNameReplace = Config.getString(exportFields, "studentNameReplace");
            lastExportStudentNameBy = Config.getString(exportFields, "studentNameBy");

            HashMap<String, Object> exportSettings = Config.getSection(exportParams, "settings");
            settingsOnlySameRatingScale = Config.getBoolean(exportSettings, "onlySameRatingScale");
            settingsOnlyCompleted = Config.getBoolean(exportSettings, "onlyCompleted");
            settingsOnlySameDir = Config.getBoolean(exportSettings, "onlySameDir");
            settingsAttributeTotalLine = Config.getBoolean(exportSettings, "attributeTotalLine");
            settingsAttributeMoyLine = Config.getBoolean(exportSettings, "attributeMoyLine");
            settingsWithTxtElements = Config.getBoolean(exportSettings, "withTxtElements");
            settingsTiersExportSlider = (int) Config.getLong(exportSettings, "tiersExportSlider");

        }catch(IOException e) {
            e.printStackTrace();
        }

        MainWindow.lbTextTab.favoritesTextSortManager.simulateCall();
        MainWindow.lbTextTab.lastsTextSortManager.simulateCall();
        MainWindow.lbTextTab.listsManager.setupMenu();

    }
    private void loadDataFromHEX(){

        new File(Main.dataFolder).mkdirs();
        File file = new File(Main.dataFolder + "userdata.hex");

        try{
            if(!file.createNewFile()){ // file exist
                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

                while(reader.available() != 0){
                    int dataType = reader.readInt();

                    switch(dataType){
                        case DataType.SIMPLE_DATA: // Last TextElement
                            try{
                                lastOpenDir = new File(reader.readUTF());
                                 reader.readUTF(); // lastExportDir
                                 reader.readUTF(); // lastExportDirNotes

                                // LAST FONTS (TEXT_TAB)

                                MainWindow.lbTextTab.lastFont = reader.readUTF();
                                MainWindow.lbTextTab.lastFontSize =reader.readInt();
                                MainWindow.lbTextTab.lastColor = reader.readUTF();
                                MainWindow.lbTextTab.lastBold = reader.readBoolean();
                                MainWindow.lbTextTab.lastItalic = reader.readBoolean();

                                // TIERS FONTS (NOTE_TAB) + Lock + ExportParams

                                for(int i = 0; i < 5 ; i++){
                                    LBNoteTab.fontTiers.put(i, Map.entry(
                                            Font.loadFont(Element.getFontFile(reader.readUTF(), reader.readBoolean(), reader.readBoolean()), reader.readDouble()), // Font + Size
                                            Map.entry(Color.valueOf(reader.readUTF()), reader.readBoolean()))); // Color + ShowName
                                }
                                MainWindow.lbNoteTab.updateElementsFont();

                                MainWindow.lbNoteTab.lockRatingScale.setSelected(reader.readBoolean());

                                lastExportFileName = reader.readUTF();
                                lastExportFileNameReplace = reader.readUTF();
                                lastExportFileNameBy = reader.readUTF();
                                lastExportFileNamePrefix = reader.readUTF();
                                lastExportFileNameSuffix = reader.readUTF();
                                lastExportStudentNameReplace = reader.readUTF();
                                lastExportStudentNameBy = reader.readUTF();
                                settingsOnlySameRatingScale = reader.readBoolean();
                                settingsOnlyCompleted = reader.readBoolean();
                                settingsOnlySameDir = reader.readBoolean();
                                settingsAttributeTotalLine = reader.readBoolean();
                                settingsAttributeMoyLine = reader.readBoolean();
                                settingsWithTxtElements = reader.readBoolean();
                                settingsTiersExportSlider = reader.readInt();

                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                        case DataType.TEXT_ELEMENT_FAVORITE: // Favorite TextElement
                            try{
                                MainWindow.lbTextTab.favoritesText.getChildren().add(TextTreeItem.readDataAndGive(reader, TextTreeItem.FAVORITE_TYPE));
                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                        case DataType.TEXT_ELEMENT_LAST: // Last TextElement
                            try{
                                MainWindow.lbTextTab.lastsText.getChildren().add(TextTreeItem.readDataAndGive(reader, TextTreeItem.LAST_TYPE));
                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                        case DataType.TEXT_ELEMENT_LIST: // List TextElement
                            try{
                                String listName = reader.readUTF();
                                ArrayList<TextListItem> list = MainWindow.lbTextTab.favoriteLists.containsKey(listName) ? MainWindow.lbTextTab.favoriteLists.get(listName) : new ArrayList<>();
                                list.add(TextListItem.readDataAndGive(reader));
                                MainWindow.lbTextTab.favoriteLists.put(listName, list);

                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                    }
                }
                reader.close();
                MainWindow.lbTextTab.favoritesTextSortManager.simulateCall();
                MainWindow.lbTextTab.lastsTextSortManager.simulateCall();
                MainWindow.lbTextTab.listsManager.setupMenu();

                file.delete();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void saveData(){

        new File(Main.dataFolder).mkdirs();
        Config config = new Config(new File(Main.dataFolder + "userdata.yml"));

        try{

            // Text Elements Lists & LastFont

            HashMap<Object, Object> texts = new HashMap<>();
            List<Object> favoriteTexts = new ArrayList<>();
            for(TreeItem<String> item : MainWindow.lbTextTab.favoritesText.getChildren()){
                if(item instanceof TextTreeItem){
                    favoriteTexts.add(((TextTreeItem) item).getYAMLData());
                }
            }
            texts.put("favorites", favoriteTexts);

            List<Object> lastTexts = new ArrayList<>();
            for(TreeItem<String> item : MainWindow.lbTextTab.lastsText.getChildren()){
                if(item instanceof TextTreeItem){
                    lastTexts.add(((TextTreeItem) item).getYAMLData());
                }
            }
            texts.put("lasts", lastTexts);

            HashMap<Object, Object> lists = new HashMap<>();
            for(Map.Entry<String, ArrayList<TextListItem>> list : MainWindow.lbTextTab.favoriteLists.entrySet()){
                List<Object> listTexts = new ArrayList<>();
                for(TextListItem item : list.getValue()){
                    listTexts.add(item.getYAMLData());
                }
                lists.put(list.getKey(), listTexts);
            }
            texts.put("lists", lists);


            HashMap<Object, Object> lastTextFont = new HashMap<>();
            lastTextFont.put("font", MainWindow.lbTextTab.lastFont);
            lastTextFont.put("size", MainWindow.lbTextTab.lastFontSize);
            lastTextFont.put("color", MainWindow.lbTextTab.lastColor);
            lastTextFont.put("bold", MainWindow.lbTextTab.lastBold);
            lastTextFont.put("italic", MainWindow.lbTextTab.lastItalic);
            texts.put("lastFont", lastTextFont);

            config.base.put("texts", texts);

            // Single Data

            config.base.put("lastOpenDir", lastOpenDir.getAbsolutePath());

            // TIERS FONTS (NOTE_TAB) + lock

            int i = 0;
            HashMap<Object, Object> notes = new HashMap<>();
            HashMap<Object, Object> noteTiersFont = new HashMap<>();
            for(Map.Entry<Font, Map.Entry<Color, Boolean>> font : LBNoteTab.fontTiers.values()){
                Font realFont = font.getKey();
                HashMap<Object, Object> data = new HashMap<>();

                data.put("font", realFont.getFamily());
                data.put("italic", Element.getFontPosture(realFont) == FontPosture.ITALIC);
                data.put("bold", Element.getFontWeight(realFont) == FontWeight.BOLD);
                data.put("size", realFont.getSize());

                data.put("color", font.getValue().getKey().toString());
                data.put("showName", font.getValue().getValue());

                noteTiersFont.put(i+"", data);
                i++;
            }
            notes.put("tiersFont", noteTiersFont);
            notes.put("lockRatingScale", MainWindow.lbNoteTab.lockRatingScale.isSelected());

            config.base.put("notes", notes);

            // ExportParams

            HashMap<Object, Object> exportParams = new HashMap<>();

            HashMap<Object, Object> exportFields = new HashMap<>();
            exportFields.put("fileName", lastExportFileName);
            exportFields.put("fileNameReplace", lastExportFileNameReplace);
            exportFields.put("fileNameBy", lastExportFileNameBy);
            exportFields.put("fileNameSuffix", lastExportFileNameSuffix);
            exportFields.put("fileNamePrefix", lastExportFileNamePrefix);
            exportFields.put("studentNameReplace", lastExportStudentNameReplace);
            exportFields.put("studentNameBy", lastExportStudentNameBy);
            exportParams.put("fields", exportFields);

            HashMap<Object, Object> exportSettings = new HashMap<>();
            exportSettings.put("onlySameRatingScale", settingsOnlySameRatingScale);
            exportSettings.put("onlyCompleted", settingsOnlyCompleted);
            exportSettings.put("onlySameDir", settingsOnlySameDir);
            exportSettings.put("attributeTotalLine", settingsAttributeTotalLine);
            exportSettings.put("attributeMoyLine", settingsAttributeMoyLine);
            exportSettings.put("withTxtElements", settingsWithTxtElements);
            exportSettings.put("tiersExportSlider", settingsTiersExportSlider);
            exportParams.put("settings", exportSettings);

            config.base.put("export", exportParams);

            config.save();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
