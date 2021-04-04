package fr.clementgre.pdf4teachers.document.editions;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.elements.*;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

import java.awt.Image;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class Edition{
    
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
        MainWindow.gradeTab.treeView.hardClear();
        
        try{
            if(!editFile.exists()) return; // File does not exist
            Config config = new Config(editFile);
            config.load();
            
            loadItemsInPage(config.getSection("texts").entrySet(), elementData -> {
                TextElement.readYAMLDataAndCreate(elementData.getValue(), elementData.getKey());
            });
            loadItemsInPage(config.getSection("images").entrySet(), elementData -> {
                ImageElement.readYAMLDataAndCreate(elementData.getValue(), elementData.getKey());
            });
            loadItemsInPage(config.getSection("vectors").entrySet(), elementData -> {
                VectorElement.readYAMLDataAndCreate(elementData.getValue(), elementData.getKey());
            });
            
            for(Object data : config.getList("grades")){
                if(data instanceof Map) GradeElement.readYAMLDataAndCreate((HashMap<String, Object>) data);
            }
            
        }catch(IOException e){
            e.printStackTrace();
        }
        
        MainWindow.gradeTab.treeView.updateAllSum();
        MainWindow.textTab.treeView.onFileSection.updateElementsList();
    }
    
    public void save(){
        if(Edition.isSave()) return;
        
        try{
            editFile.createNewFile();
            Config config = new Config(editFile);
            
            LinkedHashMap<String, ArrayList<Object>> texts = new LinkedHashMap<>();
            LinkedHashMap<String, ArrayList<Object>> images = new LinkedHashMap<>();
            LinkedHashMap<String, ArrayList<Object>> vectors = new LinkedHashMap<>();
            ArrayList<Object> grades = new ArrayList<>();
            
            // NON GRADES ELEMENTS
            int counter = 0;
            for(PageRenderer page : document.pages){
                ArrayList<Object> pageTextsData = getPageDataFromElements(page.getElements(), TextElement.class);
                if(pageTextsData != null){
                    texts.put("page" + page.getPage(), pageTextsData);
                    counter += pageTextsData.size();
                }
                ArrayList<Object> pageImagesData = getPageDataFromElements(page.getElements(), ImageElement.class);
                if(pageImagesData != null){
                    images.put("page" + page.getPage(), pageImagesData);
                    counter += pageImagesData.size();
                }
                ArrayList<Object> pageVectorsData = getPageDataFromElements(page.getElements(), VectorElement.class);
                if(pageVectorsData != null){
                    vectors.put("page" + page.getPage(), pageVectorsData);
                    counter += pageVectorsData.size();
                }
            }
            
            // GRADES ELEMENTS
            for(GradeTreeItem element : GradeTreeView.getGradesArray(GradeTreeView.getTotal())){
                grades.add(element.getCore().getYAMLData());
                if(!element.getCore().isDefaultGrade()) counter++;
            }
            
            // delete edit file if edition is empty
            if(counter == 0) editFile.delete();
            else{
                config.base.put("texts", texts);
                config.base.put("grades", grades);
                config.base.put("images", images);
                config.base.put("vectors", vectors);
                config.save();
            }
            
        }catch(IOException e){
            e.printStackTrace();
        }
        
        isSave.set(true);
        MainWindow.footerBar.showAlert(Color.web("#008e00"), Color.WHITE, "Saved !");
        MainWindow.filesTab.files.refresh();
        
    }
    
    private ArrayList<Object> getPageDataFromElements(ArrayList<Element> elements, Class<? extends Element> acceptedElements){
        ArrayList<Object> pageData = new ArrayList<>();
        for(Element element : elements){
            if(acceptedElements.isInstance(element)){
                pageData.add(element.getYAMLData());
            }
        }
        if(pageData.size() >= 1) return pageData;
        else return null;
    }
    
    // STATIC
    
    public static Element[] simpleLoad(File editFile) throws Exception{
        
        if(!editFile.exists()){ //file does not exist
            return new Element[0];
        }else{ // file exist
            Config config = new Config(editFile);
            config.load();
            
            ArrayList<Element> elements = new ArrayList<>();
    
            loadItemsInPage(config.getSection("texts").entrySet(), elementData -> {
                elements.add(TextElement.readYAMLDataAndGive(elementData.getValue(), false, elementData.getKey()));
            });
            loadItemsInPage(config.getSection("images").entrySet(), elementData -> {
                elements.add(ImageElement.readYAMLDataAndGive(elementData.getValue(), false, elementData.getKey()));
            });
            loadItemsInPage(config.getSection("vectors").entrySet(), elementData -> {
                elements.add(VectorElement.readYAMLDataAndGive(elementData.getValue(), false, elementData.getKey()));
            });
            
            for(Object data : config.getList("grades")){
                if(data instanceof Map)
                    elements.add(GradeElement.readYAMLDataAndGive((HashMap<String, Object>) data, false));
            }
            
            return elements.toArray(new Element[elements.size()]);
        }
    }
    
    private static void loadItemsInPage(Set<Map.Entry<String, Object>> data, CallBackArg<Map.Entry<Integer, HashMap<String, Object>>> addCallBack){ // Key : Page | Value : Element Data
        for(Map.Entry<String, Object> pageData : data){
            Integer page = StringUtils.getInt(pageData.getKey().replaceFirst("page", ""));
            if(page == null || !(pageData.getValue() instanceof List)) break;

            for(Object elementData : ((List<Object>) pageData.getValue())){
                if(elementData instanceof HashMap) addCallBack.call(Map.entry(page, (HashMap<String, Object>) elementData));
            }
        }
    }
    
    public static void simpleSave(File editFile, Element[] elements){
        
        try{
            editFile.createNewFile();
            Config config = new Config(editFile);
            
            LinkedHashMap<String, ArrayList<Object>> texts = new LinkedHashMap<>();
            LinkedHashMap<String, ArrayList<Object>> images = new LinkedHashMap<>();
            LinkedHashMap<String, ArrayList<Object>> vectors = new LinkedHashMap<>();
            ArrayList<Object> grades = new ArrayList<>();
            
            int counter = 0;
            for(Element element : elements){
                
                if(!(element instanceof GradeElement)){
                    if(element instanceof TextElement){
                        if(texts.containsKey("page" + element.getPageNumber())){
                            texts.get("page" + element.getPageNumber()).add(element.getYAMLData());
                        }else{
                            texts.put("page" + element.getPageNumber(), new ArrayList<>(Collections.singletonList(element.getYAMLData())));
                        }
                    }else if(element instanceof ImageElement){
                        if(images.containsKey("page" + element.getPageNumber())){
                            images.get("page" + element.getPageNumber()).add(element.getYAMLData());
                        }else{
                            images.put("page" + element.getPageNumber(), new ArrayList<>(Collections.singletonList(element.getYAMLData())));
                        }
                    }else if(element instanceof VectorElement){
                        if(vectors.containsKey("page" + element.getPageNumber())){
                            vectors.get("page" + element.getPageNumber()).add(element.getYAMLData());
                        }else{
                            vectors.put("page" + element.getPageNumber(), new ArrayList<>(Collections.singletonList(element.getYAMLData())));
                        }
                    }
                    counter++;
                }else{
                    grades.add(element.getYAMLData());
                    if(!((GradeElement) element).isDefaultGrade()) counter++;
                }
            }
            // delete edit file if edition is empty
            if(counter == 0) editFile.delete();
            else{
                config.base.put("texts", texts);
                config.base.put("grades", grades);
                config.base.put("images", images);
                config.base.put("vectors", vectors);
                config.save();
            }
            
        }catch(IOException e){
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
            int images = 0;
            int vectors = 0;
            
            Config config = new Config(editFile);
            config.load();
            
            text = countSection(config.getSection("texts"));
            images = countSection(config.getSection("images"));
            vectors = countSection(config.getSection("vectors"));
            
            for(Object data : config.getList("grades")){
                if(data instanceof HashMap){
                    double[] stats = GradeElement.getYAMLDataStats(convertInstanceOfObject(data, HashMap.class));
                    if(stats.length == 2) totalGrade = stats; // get the root grade value and the root grade total
                    if(stats[0] != -1) grades++;
                    allGrades++;
                }
            }
            
            return new double[]{text + grades + images + vectors, text, grades, images+vectors, totalGrade[0], totalGrade[1], allGrades};
        }
    }

    public static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        } catch(ClassCastException e) {
            return null;
        }
    }
    
    private static int countSection(HashMap<String, Object> sectionData){
        int count = 0;
        for(Map.Entry<String, Object> pageData : sectionData.entrySet()){
            Integer page = StringUtils.getInt(pageData.getKey().replaceFirst("page", ""));
            if(page == null || !(pageData.getValue() instanceof List)) break;
            count += ((List<Object>) pageData.getValue()).size();
        }
        return count;
    }

    
    // get YAML file from PDF file
    public static File getEditFile(File pdfFile){
        String namePath = pdfFile.getParentFile().getAbsolutePath().replace(File.separator, "!E!").replace(":", "!P!");
        String nameName = pdfFile.getName() + ".yml";
        return new File(Main.dataFolder + "editions" + File.separator + namePath + "!E!" + nameName);
    }
    
    // get PDF file from YAML file
    public static File getFileFromEdit(File editFile){
        String path = editFile.getName();
        path = path.replaceAll(Pattern.quote("!E!"), "\\" + File.separator).replaceAll(Pattern.quote("!P!"), ":");
        path = StringUtils.removeAfterLastRegex(path, ".yml");
        
        if(!editFile.getName().contains("!P!") && Main.isWindows()){
            return new File(File.separator + path);
        }
        if(!editFile.getName().startsWith("!E!") && !Main.isWindows()){
            return new File(File.separator + path);
        }
        return new File(path);
    }
    
    public static void mergeEditFileWithEditFile(File fromEdit, File destEdit){
        try{
            Files.move(fromEdit.toPath(), destEdit.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){
            e.printStackTrace();
            DialogBuilder.showErrorAlert(DialogBuilder.unableToCopyFileHeader(fromEdit.getAbsolutePath(), destEdit.getAbsolutePath(), true), e.getMessage(), false);
        }
        fromEdit.delete();
    }
    
    public static HashMap<File, File> getEditFilesWithSameName(File originFile){
        
        HashMap<File, File> files = new HashMap<>();
        
        for(File editFile : new File(Main.dataFolder + "editions" + File.separator).listFiles()){
            
            File file = getFileFromEdit(editFile);
            
            if(file.getName().equals(originFile.getName()) && !file.equals(originFile)){
                files.put(editFile, file);
            }
        }
        return files;
        
    }
    
    public static boolean clearEditDialog(boolean skip){
        if(skip) return true;
        
        Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.confirmation.title"));
        alert.setHeaderText(TR.tr("dialog.confirmation.clearEdit.header"));
        alert.setContentText(TR.tr("dialog.confirmation.irreversible"));
        
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty()) return false;
        return result.get() == ButtonType.OK;
    }
    
    public static void clearEdit(File file, boolean confirm){
        if(clearEditDialog(!confirm)){
            if(MainWindow.mainScreen.getStatus() == MainScreen.Status.OPEN){
                if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                    MainWindow.mainScreen.document.edition.clearEdit(false);
                    return;
                }
            }
            Edition.getEditFile(file).delete();
            MainWindow.filesTab.files.refresh();
        }
    }
    
    public void clearEdit(boolean confirm){
        if(clearEditDialog(!confirm)){
            MainWindow.mainScreen.setSelected(null);
            for(PageRenderer page : document.pages){
                page.clearElements();
            }
            MainWindow.textTab.treeView.onFileSection.updateElementsList();
            MainWindow.gradeTab.treeView.clear();
            Edition.setUnsave();
        }
    }
    
    public static boolean isSave(){
        return isSave.get();
    }
    
    public static void setUnsave(){
        isSave.set(false);
        MainWindow.footerBar.updateStats();
    }
    
    public static BooleanProperty isSaveProperty(){
        return isSave;
    }
}
