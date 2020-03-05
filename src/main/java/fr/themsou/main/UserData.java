package fr.themsou.main;

import fr.themsou.document.editions.elements.NoDisplayTextElement;
import javafx.scene.control.TreeItem;

import java.io.*;

public class UserData {

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
                    byte dataType = reader.readByte();

                    switch(dataType){
                        case NoDisplayTextElement.FAVORITE_TYPE: // Favorite TextElement
                            try{
                                Main.lbTextTab.favoritesText.getChildren().add(NoDisplayTextElement.readDataAndGive(reader, NoDisplayTextElement.FAVORITE_TYPE));
                            }catch(IOException e){ e.printStackTrace(); }
                        break;
                        case NoDisplayTextElement.LAST_TYPE: // Last TextElement
                            try{
                                Main.lbTextTab.lastsText.getChildren().add(NoDisplayTextElement.readDataAndGive(reader, NoDisplayTextElement.LAST_TYPE));
                            }catch(IOException e){ e.printStackTrace(); }
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
                    writer.writeByte(NoDisplayTextElement.FAVORITE_TYPE);
                    ((NoDisplayTextElement) item).writeData(writer);
                }
            }
            for(TreeItem<String> item : Main.lbTextTab.lastsText.getChildren()){
                if(item instanceof  NoDisplayTextElement){
                    writer.writeByte(NoDisplayTextElement.LAST_TYPE);
                    ((NoDisplayTextElement) item).writeData(writer);
                }
            }

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
