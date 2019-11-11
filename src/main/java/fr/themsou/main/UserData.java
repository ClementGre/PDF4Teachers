package fr.themsou.main;

import fr.themsou.document.editions.elements.NoDisplayTextElement;
import javafx.scene.control.TreeItem;

import java.io.*;

public class UserData {

    public TreeItem<String> favoritesText = new TreeItem<>("Éléments Favoris");
    public TreeItem<String> lastsText = new TreeItem<>("Éléments Précédents");

    public UserData(){

        loadData();
    }
    public void loadData(){

        new File(System.getProperty("user.home") + "" + File.separator + ".PDFTeacher/").mkdirs();
        File file = new File(System.getProperty("user.home") + "/.PDFTeacher/userData.hex");

        try{
            if(file.createNewFile()){ //file was created

            }else{ // file already exist
                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

                while(reader.available() != 0){
                    byte dataType = reader.readByte();

                    switch(dataType){
                        case 1: // Favorite TextElement
                            try{
                                favoritesText.getChildren().add(NoDisplayTextElement.readDataAndGive(reader, true));
                            }catch(IOException e){ e.printStackTrace(); }
                            break;
                        case 2: // Last TextElement
                            try{
                                lastsText.getChildren().add(NoDisplayTextElement.readDataAndGive(reader, false));
                            }catch(IOException e){ e.printStackTrace(); }
                        break;
                    }
                }
                reader.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void saveData(){

        new File(System.getProperty("user.home") + "/.PDFTeacher/").mkdirs();
        File file = new File(System.getProperty("user.home") + "/.PDFTeacher/userData.hex");

        try{
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)));

            for(TreeItem<String> item : favoritesText.getChildren()){
                writer.writeByte(1);
                ((NoDisplayTextElement) item).writeData(writer);
            }
            for(TreeItem<String> item : lastsText.getChildren()){
                writer.writeByte(2);
                ((NoDisplayTextElement) item).writeData(writer);
            }

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
