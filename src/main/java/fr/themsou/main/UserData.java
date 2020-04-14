package fr.themsou.main;

import fr.themsou.document.editions.elements.NoDisplayTextElement;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

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
                                Main.lbTextTab.fontCombo.getSelectionModel().select(reader.readUTF());
                                Main.lbTextTab.sizeCombo.getSelectionModel().select((Integer) reader.readInt());
                                Main.lbTextTab.colorPicker.setValue(Color.valueOf(reader.readUTF()));
                                Main.lbTextTab.boldBtn.setSelected(reader.readBoolean());
                                Main.lbTextTab.itBtn.setSelected(reader.readBoolean());

                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                        case DataType.TEXT_ELEMENT_FAVORITE: // Favorite TextElement
                            try{
                                Main.lbTextTab.favoritesText.getChildren().add(NoDisplayTextElement.readDataAndGive(reader, NoDisplayTextElement.FAVORITE_TYPE));
                            }catch(Exception e){ e.printStackTrace(); }
                        break;
                        case DataType.TEXT_ELEMENT_LAST: // Last TextElement
                            try{
                                Main.lbTextTab.lastsText.getChildren().add(NoDisplayTextElement.readDataAndGive(reader, NoDisplayTextElement.LAST_TYPE));
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
                if(item instanceof  NoDisplayTextElement){
                    writer.writeInt(DataType.TEXT_ELEMENT_FAVORITE);
                    ((NoDisplayTextElement) item).writeData(writer);
                }
            }

            for(TreeItem<String> item : Main.lbTextTab.lastsText.getChildren()){
                if(item instanceof  NoDisplayTextElement){
                    writer.writeInt(DataType.TEXT_ELEMENT_LAST);
                    ((NoDisplayTextElement) item).writeData(writer);
                }
            }

            writer.writeInt(DataType.SIMPLE_DATA);

            writer.writeUTF(lastOpenDir.getAbsolutePath());
            writer.writeUTF(lastExportDir.getAbsolutePath());

            writer.writeUTF(Main.lbTextTab.fontCombo.getSelectionModel().getSelectedItem());
            writer.writeInt(Main.lbTextTab.sizeCombo.getSelectionModel().getSelectedItem());
            writer.writeUTF(Main.lbTextTab.colorPicker.getValue().toString());
            writer.writeBoolean(Main.lbTextTab.boldBtn.isSelected());
            writer.writeBoolean(Main.lbTextTab.itBtn.isSelected());

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
