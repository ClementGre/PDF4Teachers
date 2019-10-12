package fr.themsou.document.editions;

import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class Edition {

    private File file;
    private File editFile;

    public Document document;

    public Edition(File file, Document document){
        this.document = document;
        this.file = file;
        this.editFile = getEditFile(file);
        load();
    }

    public void load(){

        new File(System.getProperty("user.home") + "/.PDFTeacher/editions/").mkdirs();

        try{
            if(editFile.createNewFile()){ //file was created

            }else{ // file already exist
                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(editFile)));

                while(reader.available() != 0){
                    byte elementType = reader.readByte();

                    switch (elementType){
                        case 1:
                            TextElement.readDataAndCreate(reader);
                        break;
                        case 2:
                            //elements.add(TextElement.readDataAndCreate(reader));
                        break;
                        case 3:
                            ///elements.add(TextElement.readDataAndCreate(reader));
                        break;
                    }
                }
                reader.close();
            }
        }catch (IOException e){ e.printStackTrace(); }


    }

    public void save(){

        try{
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(editFile, false)));

            for(PageRenderer page : document.pages){
                for(int i = 0; i < page.getElements().size(); i++){
                    page.getElements().get(i).writeData(writer);
                }
            }

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }


    }
    public static File getEditFile(File file){

        return new File(System.getProperty("user.home") + "/.PDFTeacher/editions/" +  file.getParentFile().getAbsolutePath().replace("/", "!E") + "!E" + file.getName() + ".edit");

    }

    public static void clearEdit(File file, boolean confirm){

        if(confirm == true){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Êtes vous sûr de vouloir supprimer l'édition de ce document ?");
            alert.setContentText("Cette action est irréversible.");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                confirm = false;
            }
        }

        if(confirm == false){
            if(Main.mainScreen.getStatus() == -1){
                if(Main.mainScreen.document.getFile() == file){
                    Main.mainScreen.document.edition.clearEdit(true);
                    return;
                }
            }
            Edition.getEditFile(file).delete();
        }
    }

    public void clearEdit(boolean confirm){
        if(confirm == true){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Êtes vous sûr de vouloir supprimer l'édition de ce document ?");
            alert.setContentText("Cette action est irréversible.");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                confirm = false;
            }
        }

        if(confirm == false){
            if(Main.mainScreen.document.getFile() == file){
                Main.mainScreen.document.edition.clearEdit(true);
                return;
            }
        }
    }

}
