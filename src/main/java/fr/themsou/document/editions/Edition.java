package fr.themsou.document.editions;

import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.leftBar.grades.GradeTreeItem;
import fr.themsou.panel.leftBar.grades.GradeTreeView;
import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import fr.themsou.yaml.Config;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import java.io.*;
import java.util.*;
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
        File lastEditFile = new File(StringUtils.removeAfterLastRejex(editFile.getAbsolutePath(), ".yml") + ".edit");
        if(lastEditFile.exists()){
            loadHEX(lastEditFile);
            lastEditFile.delete();
            return;
        }
        new File(Main.dataFolder + "editions").mkdirs();
        MainWindow.lbGradeTab.treeView.hardClear();

        try{
            if(!editFile.exists()) return; // File does not exist
            Config config = new Config(editFile);
            config.load();

            for(Map.Entry<String, Object> pageData : config.getSection("texts").entrySet()){
                Integer page = StringUtils.getInt(pageData.getKey().replaceFirst("page", ""));
                if(page == null || !(pageData.getValue() instanceof List)) break;

                for(Object data : ((ArrayList<Object>) pageData.getValue())){
                    if(data instanceof Map) TextElement.readYAMLDataAndCreate((HashMap<String, Object>) data, page);
                }
            }

            for(Object data : config.getList("grades")){
                if(data instanceof Map) GradeElement.readYAMLDataAndCreate((HashMap<String, Object>) data);
            }

        }catch (IOException e){ e.printStackTrace(); }
        MainWindow.lbTextTab.updateOnFileElementsList();
    }
    public void save(){
        if(Edition.isSave()) return;

        try{
            editFile.createNewFile();
            Config config = new Config(editFile);

            LinkedHashMap<String, ArrayList<Object>> texts = new LinkedHashMap<>();
            ArrayList<Object> grades = new ArrayList<>();

            // TEXTS ELEMENTS
            int counter = 0; int i = 0;
            for(PageRenderer page : document.pages){
                ArrayList<Object> pageData = new ArrayList<>();

                for(Element element : page.getElements()){
                    if(element instanceof TextElement){
                        pageData.add(element.getYAMLData());
                        counter++;
                    }
                }
                if(pageData.size() >= 1) texts.put("page"+i, pageData);
                i++;
            }

            // GRADES ELEMENTS
            for(GradeTreeItem element : GradeTreeView.getGradesArray((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot())){
                grades.add(element.getCore().getYAMLData());
                if(!element.getCore().isDefaultRoot()) counter++;
            }

            // delete edit file if edition is empty
            if(counter == 0) editFile.delete();
            else{
                config.base.put("texts", texts);
                config.base.put("grades", grades);
                config.save();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

        isSave.set(true);
        MainWindow.lbFilesTab.files.refresh();

    }
    public void loadHEX(File file){
        MainWindow.lbGradeTab.treeView.clear();
        try{
            if(file.exists()){ //file does not exist

                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                while(reader.available() != 0){
                    byte elementType = reader.readByte();

                    switch (elementType){
                        case 1:
                            TextElement.readDataAndCreate(reader);
                            break;
                        case 2:
                            GradeElement.readDataAndCreate(reader);
                            break;
                    }
                }
                reader.close();
            }
        }catch (IOException e){ e.printStackTrace(); }
        MainWindow.lbTextTab.updateOnFileElementsList();
    }

    // STATIC

    public static Element[] simpleLoad(File editFile) throws Exception{

        if(!editFile.exists()){ //file does not exist
            return new Element[0];
        }else{ // file exist
            Config config = new Config(editFile);
            config.load();

            ArrayList<Element> elements = new ArrayList<>();

            for(Map.Entry<String, Object> pageData : config.getSection("texts").entrySet()){
                Integer page = StringUtils.getInt(pageData.getKey().replaceFirst("page", ""));
                if(page == null || !(pageData.getValue() instanceof List)) break;

                for(Object data : ((ArrayList<Object>) pageData.getValue())){
                    if(data instanceof Map)
                        elements.add(TextElement.readYAMLDataAndGive((HashMap<String, Object>) data, false, page));
                }
            }

            for(Object data : config.getList("grades")){
                if(data instanceof Map) elements.add(GradeElement.readYAMLDataAndGive((HashMap<String, Object>) data, false));
            }

            return elements.toArray(new Element[elements.size()]);
        }
    }
    public static void simpleSave(File editFile, Element[] elements){

        try{
            editFile.createNewFile();
            Config config = new Config(editFile);

            LinkedHashMap<String, ArrayList<Object>> texts = new LinkedHashMap<>();
            ArrayList<Object> grades = new ArrayList<>();

            int counter = 0;
            for(Element element : elements){

                if(element instanceof TextElement){
                    if(texts.containsKey("page"+element.getPageNumber())){
                        texts.get("page"+element.getPageNumber()).add(element.getYAMLData());
                    }else{
                        texts.put("page"+element.getPageNumber(),  new ArrayList<>(Collections.singletonList(element.getYAMLData())));
                    } counter++;

                }else if(element instanceof GradeElement){
                    grades.add(((GradeElement) element).getYAMLData());
                    if(!((GradeElement) element).isDefaultRoot()) counter++;
                }
            }
            // delete edit file if edition is empty
            if(counter == 0) editFile.delete();
            else{
                config.base.put("texts", texts);
                config.base.put("grades", grades);
                config.save();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static double[] countElements(File editFile) throws Exception{

        if(!editFile.exists()){ //file does not exist
            return new double[0];
        }else{ // file already exist

            double[] totalGrade = new double[]{-1, 0}; // Root grade value and total
            int text = 0;
            int allGrades = 0; // All grade element count
            int grades = 0; // All entered grade
            int draw = 0;

            Config config = new Config(editFile);
            config.load();

            for(Map.Entry<String, Object> pageData : config.getSection("texts").entrySet()){
                Integer page = StringUtils.getInt(pageData.getKey().replaceFirst("page", ""));
                if(page == null || !(pageData.getValue() instanceof List)) break;
                text += ((List<Object>) pageData.getValue()).size();
            }

            for(Object data : config.getList("grades")){
                if(data instanceof Map){
                    double[] stats = GradeElement.getYAMLDataStats((HashMap<String, Object>) data);

                    if(stats.length == 2) totalGrade = stats; // get the root grade value and the root grade total
                    if(stats[0] != -1) grades++;
                    allGrades++;
                }
            }

            return new double[]{text+grades+draw, text, grades, draw, totalGrade[0], totalGrade[1], allGrades};
        }
    }

    // get YAML file from PDF file
    public static File getEditFile(File file){
        return new File(Main.dataFolder + "editions" + File.separator + file.getParentFile().getAbsolutePath().replace(File.separator, "!E!").replace(":", "!P!") + "!E!" + file.getName() + ".yml");
    }
    // get PDF file from YAML file
    public static File getFileEdit(File file){
        return new File(file.getName().replaceAll("!E!", "\\" + File.separator).replaceAll("!P!", ":").replace(".yml", ""));
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
            MainWindow.mainScreen.setSelected(null);
            for(PageRenderer page : document.pages){
                page.clearElements();
            }
            MainWindow.lbTextTab.updateOnFileElementsList();
            MainWindow.lbGradeTab.treeView.clear();

            Edition.setUnsave();
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
