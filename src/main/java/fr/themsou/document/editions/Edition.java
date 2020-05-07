package fr.themsou.document.editions;

import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.leftBar.notes.NoteTreeItem;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

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

        MainWindow.lbNoteTab.treeView.clear();
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
                            NoteElement.readDataAndCreate(reader);
                        break;
                        case 3:

                        break;
                    }
                }
                reader.close();
            }
        }catch (IOException e){ e.printStackTrace(); }
        MainWindow.lbTextTab.updateOnFileElementsList();
        //if(Main.lbNoteTab.treeView.getRoot() == null) Main.lbNoteTab.treeView.generateRoot();
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
                        elements.add(NoteElement.readDataAndGive(reader, false));
                        break;
                    case 3:

                    break;
                }
            }
            reader.close();
            return elements.toArray(new Element[elements.size()]);
        }
    }
    public static void simpleSave(File editFile, Element[] elements) throws Exception {

        try{
            editFile.createNewFile();
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(editFile, false)));

            int counter = 0;
            for(Element element : elements) {


                element.writeSimpleData(writer);

                if(element instanceof NoteElement){
                    // not incrément counter if root is default
                    if(Builders.cleanArray(((NoteElement) element).getParentPath().split(Pattern.quote("\\"))).length == 0){ // Element is Root
                        if(((NoteElement) element).getValue() == -1 && ((NoteElement) element).getTotal() == 20 && ((NoteElement) element).getName().equals(TR.tr("Total"))){ // Element is default Root
                            continue;
                }}}
                counter++;
            }
            writer.flush();
            writer.close();

            // delete edit file if edition is empty
            if(counter == 0) editFile.delete();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static double[] countElements(File editFile) throws Exception{

        if(!editFile.exists()){ //file does not exist
            return new double[0];
        }else{ // file already exist
            DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(editFile)));

            double[] totalNote = new double[]{-1, 0}; // Root note value and total
            int text = 0;
            int allNotes = 0; // All note element count
            int notes = 0; // All entered note
            int draw = 0;
            while(reader.available() != 0){
                byte elementType = reader.readByte();

                switch (elementType){
                    case 1:
                        text++;
                        TextElement.consumeData(reader);
                    break;
                    case 2:
                        double[] data = NoteElement.consumeData(reader);

                        if(data.length == 2) totalNote = data; // get the root note value and the root note total

                        if(data[0] != -1) notes++;
                        allNotes++;
                    break;
                    case 3:
                        draw++;
                    break;
                }
            }
            reader.close();
            return new double[]{text+notes+draw, text, notes, draw, totalNote[0], totalNote[1], allNotes};
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

                    // Ne sauvegarde pas les notes pour préserver l'ordre des notes : les notes sont sauvegardés après
                    if(!(page.getElements().get(i) instanceof NoteElement)){
                        page.getElements().get(i).writeSimpleData(writer);
                        counter++;
                    }
                }
            }
            for(NoteTreeItem element : NoteTreeView.getNotesArray((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot())){
                element.getCore().writeSimpleData(writer);

                // not incrément counter if root is default
                if(element.isRoot()){ // Element is Root
                    if(element.getCore().getValue() == -1 && element.getCore().getTotal() == 20 && element.getCore().getName().equals(TR.tr("Total"))){ // Element is default Root
                        continue;
                    }
                }
                counter++;
            }
            writer.flush();
            writer.close();

            // delete edit file if edition is empty
            if(counter == 0) editFile.delete();

        }catch (IOException e) {
            e.printStackTrace();
        }

        isSave.set(true);
        MainWindow.lbFilesTab.files.refresh();

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
            alert.setTitle(TR.tr("Confirmation"));
            alert.setHeaderText(TR.tr("Êtes vous sûr de vouloir supprimer l'édition de ce document ?"));
            alert.setContentText(TR.tr("Cette action est irréversible."));

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                confirm = false;
            }
        }

        if(!confirm){
            if(MainWindow.mainScreen.getStatus() == MainScreen.Status.OPEN){
                if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                    MainWindow.mainScreen.document.edition.clearEdit(false);
                    return;
                }
            }
            Edition.getEditFile(file).delete();
            MainWindow.lbFilesTab.files.refresh();
        }
    }

    public void clearEdit(boolean confirm){
        if(confirm){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            Builders.secureAlert(alert);
            alert.setTitle(TR.tr("Confirmation"));
            alert.setHeaderText(TR.tr("Êtes vous sûr de vouloir supprimer l'édition de ce document ?"));
            alert.setContentText(TR.tr("Cette action est irréversible."));

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
            MainWindow.mainScreen.setSelected(null);

            MainWindow.lbTextTab.updateOnFileElementsList();
            MainWindow.lbNoteTab.treeView.clear();
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
