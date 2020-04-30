package fr.themsou.main;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.panel.leftBar.notes.LBNoteTab;
import fr.themsou.panel.leftBar.texts.TextListItem;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.windows.MainWindow;
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
        public static final int VERSION = 4;
    }

    public static File lastOpenDir = new File(System.getProperty("user.home"));
    public static File lastExportDir = new File(System.getProperty("user.home"));
    public static File lastExportDirNotes = new File(System.getProperty("user.home"));

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
        loadData();
    }
    public void loadData(){

        new File(Main.dataFolder).mkdirs();
        File file = new File(Main.dataFolder + "userdata.hex");

        if(Main.settings.getSettingsVersion().isEmpty()) return;

        try{
            if(!file.createNewFile()){ // file already exist
                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

                while(reader.available() != 0){
                    int dataType = reader.readInt();

                    switch(dataType){
                        case DataType.VERSION: // Last TextElement

                        case DataType.SIMPLE_DATA: // Last TextElement
                            try{
                                lastOpenDir = new File(reader.readUTF());
                                lastExportDir = new File(reader.readUTF());
                                lastExportDirNotes = new File(reader.readUTF());

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
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void saveData(){

        new File(Main.dataFolder).mkdirs();
        File file = new File(Main.dataFolder + "userdata.hex");

        try{
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)));

            for(TreeItem<String> item : MainWindow.lbTextTab.favoritesText.getChildren()){
                if(item instanceof TextTreeItem){
                    writer.writeInt(DataType.TEXT_ELEMENT_FAVORITE);
                    ((TextTreeItem) item).writeData(writer);
                }
            }

            for(TreeItem<String> item : MainWindow.lbTextTab.lastsText.getChildren()){
                if(item instanceof TextTreeItem){
                    writer.writeInt(DataType.TEXT_ELEMENT_LAST);
                    ((TextTreeItem) item).writeData(writer);
                }
            }
            for(Map.Entry<String, ArrayList<TextListItem>> list : MainWindow.lbTextTab.favoriteLists.entrySet()){
                String listName = list.getKey();
                for(TextListItem item : list.getValue()){
                    writer.writeInt(DataType.TEXT_ELEMENT_LIST);
                    writer.writeUTF(listName);
                    item.writeData(writer);
                }
            }

            writer.writeInt(DataType.SIMPLE_DATA);

            writer.writeUTF(lastOpenDir.getAbsolutePath());
            writer.writeUTF(lastExportDir.getAbsolutePath());
            writer.writeUTF(lastExportDirNotes.getAbsolutePath());

            // LAST FONTS (TEXT_TAB)

            writer.writeUTF(MainWindow.lbTextTab.lastFont);
            writer.writeInt(MainWindow.lbTextTab.lastFontSize);
            writer.writeUTF(MainWindow.lbTextTab.lastColor);
            writer.writeBoolean(MainWindow.lbTextTab.lastBold);
            writer.writeBoolean(MainWindow.lbTextTab.lastItalic);

            // TIERS FONTS (NOTE_TAB) + lock + ExportParams

            for(int i = 0; i < 5 ; i++){
                Map.Entry<Font, Map.Entry<Color, Boolean>> font = LBNoteTab.fontTiers.get(i);
                Font realFont = font.getKey();

                writer.writeUTF(realFont.getFamily());
                writer.writeBoolean(Element.getFontPosture(realFont) == FontPosture.ITALIC);
                writer.writeBoolean(Element.getFontWeight(realFont) == FontWeight.BOLD);
                writer.writeDouble(realFont.getSize());

                writer.writeUTF(font.getValue().getKey().toString());
                writer.writeBoolean(font.getValue().getValue());
            }

            writer.writeBoolean(MainWindow.lbNoteTab.lockRatingScale.isSelected());

            writer.writeUTF(lastExportFileName);
            writer.writeUTF(lastExportFileNameReplace);
            writer.writeUTF(lastExportFileNameBy);
            writer.writeUTF(lastExportFileNameSuffix);
            writer.writeUTF(lastExportFileNamePrefix);
            writer.writeUTF(lastExportStudentNameReplace);
            writer.writeUTF(lastExportStudentNameBy);
            writer.writeBoolean(settingsOnlySameRatingScale);
            writer.writeBoolean(settingsOnlyCompleted);
            writer.writeBoolean(settingsOnlySameDir);
            writer.writeBoolean(settingsAttributeTotalLine);
            writer.writeBoolean(settingsAttributeMoyLine);
            writer.writeBoolean(settingsWithTxtElements);
            writer.writeInt(settingsTiersExportSlider);

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
