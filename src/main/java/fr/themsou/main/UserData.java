package fr.themsou.main;

import fr.themsou.panel.leftBar.texts.TextTreeItem;
import javafx.scene.control.TreeItem;

import java.io.*;

public class UserData {

    private class DataType{
        public static final int SIMPLE_DATA = 0;
        public static final int TEXT_ELEMENT_FAVORITE = 1;
        public static final int TEXT_ELEMENT_LAST = 2;
    }

    public static File lastOpenDir = new File(System.getProperty("user.home"));
    public static File lastExportDir = new File(System.getProperty("user.home") + File.separator + "xyz.length");

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
                                Main.lbTextTab.lastFont = reader.readUTF();
                                Main.lbTextTab.lastFontSize = reader.readInt();
                                Main.lbTextTab.lastColor = reader.readUTF();
                                Main.lbTextTab.lastBold = reader.readBoolean();
                                Main.lbTextTab.lastItalic = reader.readBoolean();

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

            writer.writeUTF(Main.lbTextTab.lastFont);
            writer.writeInt(Main.lbTextTab.lastFontSize);
            writer.writeUTF(Main.lbTextTab.lastColor);
            writer.writeBoolean(Main.lbTextTab.lastBold);
            writer.writeBoolean(Main.lbTextTab.lastItalic);

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
