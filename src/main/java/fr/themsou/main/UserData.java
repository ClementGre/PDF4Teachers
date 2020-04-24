package fr.themsou.main;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.panel.leftBar.notes.LBNoteTab;
import fr.themsou.panel.leftBar.notes.NoteSettingsWindow;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserData {

    private class DataType{
        public static final int SIMPLE_DATA = 0;
        public static final int TEXT_ELEMENT_FAVORITE = 1;
        public static final int TEXT_ELEMENT_LAST = 2;
    }

    public static File lastOpenDir = new File(System.getProperty("user.home"));
    public static File lastExportDir = new File(System.getProperty("user.home"));
    public static File lastExportDirNotes = new File(System.getProperty("user.home"));

    public UserData(){
        loadData();
    }
    public void loadData(){

        new File(Main.dataFolder).mkdirs();
        File file = new File(Main.dataFolder + "userData.hex");

        try{
            if(file.createNewFile()){ //file was created

            }else{ // file already exist
                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

                while(reader.available() != 0){
                    int dataType = reader.readInt();

                    switch(dataType){
                        case DataType.SIMPLE_DATA: // Last TextElement
                            try{
                                lastOpenDir = new File(reader.readUTF());
                                lastExportDir = new File(reader.readUTF());
                                lastExportDirNotes = new File(reader.readUTF());

                                // LAST FONTS (TEXT_TAB)

                                Main.lbTextTab.lastFont = reader.readUTF();
                                Main.lbTextTab.lastFontSize = reader.readInt();
                                Main.lbTextTab.lastColor = reader.readUTF();
                                Main.lbTextTab.lastBold = reader.readBoolean();
                                Main.lbTextTab.lastItalic = reader.readBoolean();

                                // TIERS FONTS (NOTE_TAB)

                                for(int i = 0; i < 5 ; i++){
                                    LBNoteTab.fontTiers.put(i, Map.entry(
                                            Font.loadFont(Element.getFontFile(reader.readUTF(), reader.readBoolean(), reader.readBoolean()), reader.readDouble()), // Font + Size
                                            Map.entry(Color.valueOf(reader.readUTF()), reader.readBoolean()))); // Color + ShowName
                                }
                                Main.lbNoteTab.updateElementsFont();

                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                        case DataType.TEXT_ELEMENT_FAVORITE: // Favorite TextElement
                            try{
                                Main.lbTextTab.favoritesText.getChildren().add(TextTreeItem.readDataAndGive(reader, TextTreeItem.FAVORITE_TYPE));
                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                        case DataType.TEXT_ELEMENT_LAST: // Last TextElement
                            try{
                                Main.lbTextTab.lastsText.getChildren().add(TextTreeItem.readDataAndGive(reader, TextTreeItem.LAST_TYPE));
                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                    }
                }
                reader.close();
                Main.lbTextTab.favoritesTextSortManager.simulateCall();
                Main.lbTextTab.lastsTextSortManager.simulateCall();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void saveData(){

        new File(Main.dataFolder).mkdirs();
        File file = new File(Main.dataFolder + "userData.hex");

        try{
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)));

            for(TreeItem<String> item : Main.lbTextTab.favoritesText.getChildren()){
                if(item instanceof TextTreeItem){
                    writer.writeInt(DataType.TEXT_ELEMENT_FAVORITE);
                    ((TextTreeItem) item).writeData(writer);
                }
            }

            for(TreeItem<String> item : Main.lbTextTab.lastsText.getChildren()){
                if(item instanceof TextTreeItem){
                    writer.writeInt(DataType.TEXT_ELEMENT_LAST);
                    ((TextTreeItem) item).writeData(writer);
                }
            }

            writer.writeInt(DataType.SIMPLE_DATA);

            writer.writeUTF(lastOpenDir.getAbsolutePath());
            writer.writeUTF(lastExportDir.getAbsolutePath());
            writer.writeUTF(lastExportDirNotes.getAbsolutePath());

            // LAST FONTS (TEXT_TAB)

            writer.writeUTF(Main.lbTextTab.lastFont);
            writer.writeInt(Main.lbTextTab.lastFontSize);
            writer.writeUTF(Main.lbTextTab.lastColor);
            writer.writeBoolean(Main.lbTextTab.lastBold);
            writer.writeBoolean(Main.lbTextTab.lastItalic);

            // TIERS FONTS (NOTE_TAB)

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

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
