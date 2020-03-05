package fr.themsou.document.editions;

import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Edition {

    private File file;
    private File editFile;
    private static BooleanProperty isSave = new SimpleBooleanProperty(true);

    public Document document;

    public Edition(File file, Document document){
        this.document = document;
        this.file = file;
        this.editFile = getEditFile(file);
        load();
    }

    public void load(){

        new File(Main.dataFolder + "editions").mkdirs();

        try{
            if(editFile.exists()){ //file does not exist

                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(editFile)));

                while(reader.available() != 0){
                    byte elementType = reader.readByte();

                    switch (elementType){
                        case 1:
                            TextElement.readDataAndCreate(reader);
                        break;
                        case 2:

                        break;
                        case 3:

                        break;
                    }
                }
                reader.close();
                Main.lbTextTab.updateOnFileElementsList();
            }
        }catch (IOException e){ e.printStackTrace(); }
    }
    public static Element[] simpleLoad(File editFile) throws Exception{

        if(!editFile.exists()){ //file does not exist
            return new Element[0];
        }else{ // file already exist
            DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(editFile)));
            ArrayList<Element> elements = new ArrayList<>();

            while(reader.available() != 0){
                byte elementType = reader.readByte();

                switch (elementType){
                    case 1:
                        elements.add(TextElement.readDataAndGive(reader, false));
                        break;
                    case 2:

                        break;
                    case 3:

                        break;
                }
            }
            reader.close();
            return elements.toArray(new Element[elements.size()]);
        }
    }
    public static Integer[] countElements(File editFile) throws Exception{

        if(!editFile.exists()){ //file does not exist
            return new Integer[0];
        }else{ // file already exist
            DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(editFile)));

            int text = 0;
            int notes = 0;
            int draw = 0;
            while(reader.available() != 0){
                byte elementType = reader.readByte();

                switch (elementType){
                    case 1:
                        text++;
                        TextElement.consumeData(reader);
                    break;
                    case 2:
                        notes++;
                    break;
                    case 3:
                        draw++;
                    break;
                }
            }
            reader.close();
            return new Integer[]{text+notes+draw, text, notes, draw};
        }
    }

    public void save(){

        if(Edition.isSave()) return;

        try{
            editFile.createNewFile();
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(editFile, false)));

            int counter = 0;
            for(PageRenderer page : document.pages){
                for(int i = 0; i < page.getElements().size(); i++){
                    page.getElements().get(i).writeSimpleData(writer);
                    counter++;
                }
            }
            writer.flush();
            writer.close();

            if(counter == 0) editFile.delete();

        }catch (IOException e) {
            e.printStackTrace();
        }

        isSave.set(true);

    }
    public static File getEditFile(File file){
        return new File(Main.dataFolder + "editions" + File.separator + file.getParentFile().getAbsolutePath().replace(File.separator, "!E!").replace(":", "!P!") + "!E!" + file.getName() + ".edit");
    }
    public static File getFileEdit(File file){
        return new File(file.getName().replaceAll("!E!", "\\" + File.separator).replaceAll("!P!", ":").replace(".edit", ""));
    }
    public static void mergeEditFileWithDocFile(File from, File dest){
        mergeEditFileWithEditFile(getEditFile(from), getEditFile(dest));
    }
    public static void mergeEditFileWithEditFile(File fromEdit, File destEdit){
        if(destEdit.exists()) destEdit.delete();
        fromEdit.renameTo(destEdit);
        fromEdit.delete();
    }

    public static ArrayList<File> getEditFilesWithSameName(File originFile){

        ArrayList<File> files = new ArrayList<>();

        for(File editFile : new File(Main.dataFolder + "editions" + File.separator).listFiles()){

            File file = getFileEdit(editFile);
            String path = file.getParentFile().getAbsolutePath();

            if(file.getName().equals(originFile.getName()) && !file.equals(originFile)){
                files.add(file);
            }
        }
        return files;

    }

    public static void clearEdit(File file, boolean confirm){

        if(confirm){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            Builders.secureAlert(alert);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Êtes vous sûr de vouloir supprimer l'édition de ce document ?");
            alert.setContentText("Cette action est irréversible.");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                confirm = false;
            }
        }

        if(!confirm){
            if(Main.mainScreen.getStatus() == -1){
                if(Main.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                    Main.mainScreen.document.edition.clearEdit(false);
                    return;
                }
            }
            Edition.getEditFile(file).delete();
        }
    }

    public void clearEdit(boolean confirm){
        if(confirm){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            Builders.secureAlert(alert);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Êtes vous sûr de vouloir supprimer l'édition de ce document ?");
            alert.setContentText("Cette action est irréversible.");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                confirm = false;
            }
        }

        if(!confirm){
            for(PageRenderer page : document.pages){
                page.clearElements();
            }
            editFile.delete();
            Main.mainScreen.setSelected(null);
        }
    }

    public static boolean isSave() {
        return isSave.get();
    }
    public static void setUnsave() {
        isSave.set(false);
    }
    public static BooleanProperty isSaveProperty() {
        return isSave;
    }
}
