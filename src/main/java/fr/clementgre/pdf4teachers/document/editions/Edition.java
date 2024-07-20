/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.elements.*;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ConfirmAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class Edition{
    
    private final File file;
    private final File editFile;
    private static final BooleanProperty isSave = new SimpleBooleanProperty(true);
    
    public Document document;
    
    public Edition(File file, Document document){
        this.document = document;
        this.file = file;
        this.editFile = getEditFile(file);
    }
    
    // LOAD ORDER: Texts < Images < Vectors < Grades
    public boolean load(boolean updateScrollValue){
        new File(Main.dataFolder + "editions").mkdirs();
        MainWindow.gradeTab.treeView.clearElements(true, false); // Generate root in case of no root in edition
        
        try{
            if(!editFile.exists()) return true; // File does not exist
            Config config = new Config(editFile);
            config.load();
            int versionID = (int) config.getLong("versionID");
    
            boolean upscaleGrid = versionID == 0; // Between 1.2.1 and 1.3.0, the grid size was multiplied by 100
            
            Double lastScrollValue = config.getDoubleNull("lastScrollValue");
            if(lastScrollValue != null && updateScrollValue) document.setCurrentScrollValue(lastScrollValue);
    
            loadItemsInPage(config.getSection("vectors").entrySet(), elementData -> {
                VectorElement.readYAMLDataAndCreate(elementData.getValue(), elementData.getKey());
            });
            loadItemsInPage(config.getSection("images").entrySet(), elementData -> {
                ImageElement.readYAMLDataAndCreate(elementData.getValue(), elementData.getKey());
            });
            loadItemsInPage(config.getSection("texts").entrySet(), elementData -> {
                TextElement.readYAMLDataAndCreate(elementData.getValue(), elementData.getKey(), upscaleGrid);
            });
            // There is only one SkillTableElement (the grid) that contains all the skills
            SkillTableElement.readYAMLDataAndCreate(config.getSection("skills"));
            
            for(Object data : config.getList("grades")){
                if(data instanceof Map) GradeElement.readYAMLDataAndCreate((HashMap<String, Object>) data, upscaleGrid);
            }
            
            isSave.set(true);
            MainWindow.gradeTab.treeView.updateAllSum();
            MainWindow.textTab.treeView.onFileSection.updateElementsList();
            
            return true;
        }catch(IOException e){
            Log.eNotified(e, "Can't load edition");
            return false;
        }
    }
    
    public void save(boolean toast){
        if(Edition.isSave()){
            saveLastScrollValue();
            return;
        }
        
        try{
            editFile.createNewFile();
            Config config = new Config(editFile);
            
            LinkedHashMap<String, ArrayList<Object>> texts = new LinkedHashMap<>();
            LinkedHashMap<String, ArrayList<Object>> images = new LinkedHashMap<>();
            LinkedHashMap<String, ArrayList<Object>> vectors = new LinkedHashMap<>();
            ArrayList<Object> grades = new ArrayList<>();
            LinkedHashMap<Object, Object> skills = new LinkedHashMap<>();
            
            
            // NON GRADES ELEMENTS
            int counter = 0;
            for(PageRenderer page : document.getPages()){
                ArrayList<Object> pageVectorsData = getPageDataFromElements(page.getElements(), VectorElement.class);
                if(pageVectorsData != null){
                    vectors.put("page" + page.getPage(), pageVectorsData);
                    counter += pageVectorsData.size();
                }
                ArrayList<Object> pageImagesData = getPageDataFromElements(page.getElements(), ImageElement.class);
                if(pageImagesData != null){
                    images.put("page" + page.getPage(), pageImagesData);
                    counter += pageImagesData.size();
                }
                ArrayList<Object> pageTextsData = getPageDataFromElements(page.getElements(), TextElement.class);
                if(pageTextsData != null){
                    texts.put("page" + page.getPage(), pageTextsData);
                    counter += pageTextsData.size();
                }
    
                // There is only one SkillTableElement (the grid) that contains all the skills
                if(skills.isEmpty()){
                    SkillTableElement skillTableElement = (SkillTableElement) page.getElements().stream().filter(e -> e instanceof SkillTableElement).findAny().orElse(null);
                    if(skillTableElement != null && skillTableElement.getAssessmentId() != 0){
                        skills = skillTableElement.getYAMLData();
                        counter ++;
                    }
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
                config.base.put("lastScrollValue", document.getLastScrollValue());
                config.base.put("texts", texts);
                config.base.put("grades", grades);
                config.base.put("images", images);
                config.base.put("vectors", vectors);
                config.base.put("skills", skills);
                config.set("versionID", Main.VERSION_ID);
                config.save();
            }
            
        }catch(IOException e){
            Log.eNotified(e, "Can't save edition");
        }
        
        isSave.set(true);
        if(toast) MainWindow.footerBar.showToast(Color.web("#008e00"), Color.WHITE, TR.tr("footerBar.messages.saved"));
        MainWindow.filesTab.files.refresh();
        
    }
    
    public void saveLastScrollValue(){
        if(!editFile.exists()) return;
        try{
            Config config = new Config(editFile);
            config.load();
            
            config.base.put("lastScrollValue", document.getLastScrollValue());
            
            config.save();
        }catch(Exception e){
            Log.eNotified(e);
        }
    }
    
    private ArrayList<Object> getPageDataFromElements(ArrayList<Element> elements, Class<? extends Element> acceptedElements){
        ArrayList<Object> pageData = elements.stream()
                .filter(acceptedElements::isInstance)
                .map(Element::getYAMLData)
                .collect(Collectors.toCollection(ArrayList::new));
        if(!pageData.isEmpty()) return pageData;
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ///////////////////////////// STATIC //////////////////////////////
    ///////////////////////////////////////////////////////////////////
    
    public static Element[] simpleLoad(File editFile) throws Exception{
        
        if(!editFile.exists()){ //file does not exist
            return new Element[0];
        }
        
        // file exist
        Config config = new Config(editFile);
        config.load();
        int versionID = (int) config.getLong("versionID");
        
        boolean upscaleGrid = versionID == 0; // Between 1.2.1 and 1.3.0, the grid size was multiplied by 100
        
        ArrayList<Element> elements = new ArrayList<>();
        
        loadItemsInPage(config.getSection("vectors").entrySet(), elementData -> {
            elements.add(VectorElement.readYAMLDataAndGive(elementData.getValue(), false, elementData.getKey()));
        });
        loadItemsInPage(config.getSection("images").entrySet(), elementData -> {
            elements.add(ImageElement.readYAMLDataAndGive(elementData.getValue(), false, elementData.getKey()));
        });
        loadItemsInPage(config.getSection("texts").entrySet(), elementData -> {
            elements.add(TextElement.readYAMLDataAndGive(elementData.getValue(), false, elementData.getKey(), upscaleGrid));
        });
        
        for(Object data : config.getList("grades")){
            if(data instanceof Map)
                elements.add(GradeElement.readYAMLDataAndGive((HashMap<String, Object>) data, false, upscaleGrid));
        }
        
        // There is only one SkillTableElement (the grid) that contains all the skills
        elements.add(SkillTableElement.readYAMLDataAndGive(config.getSection("skills"), false));
        
        return elements.toArray(new Element[0]);
    }
    
    // For each element of a page map, call the callback.
    // addCallBack : Key : Page | Value : Element Data
    private static void loadItemsInPage(Set<Map.Entry<String, Object>> data, CallBackArg<Map.Entry<Integer, HashMap<String, Object>>> addCallBack){
        for(Map.Entry<String, Object> pageData : data){
            Integer page = MathUtils.parseIntOrNull(pageData.getKey().replaceFirst("page", ""));
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
            LinkedHashMap<Object, Object> skills = new LinkedHashMap<>();
            ArrayList<Object> grades = new ArrayList<>();
            
            int counter = 0;
            for(Element element : elements){
                
                if(!(element instanceof GradeElement)){
                    if(element instanceof VectorElement){
                        if(vectors.containsKey("page" + element.getPageNumber())){
                            vectors.get("page" + element.getPageNumber()).add(element.getYAMLData());
                        }else{
                            vectors.put("page" + element.getPageNumber(), new ArrayList<>(Collections.singletonList(element.getYAMLData())));
                        }
                    }else if(element instanceof ImageElement){
                        if(images.containsKey("page" + element.getPageNumber())){
                            images.get("page" + element.getPageNumber()).add(element.getYAMLData());
                        }else{
                            images.put("page" + element.getPageNumber(), new ArrayList<>(Collections.singletonList(element.getYAMLData())));
                        }
                    }else if(element instanceof TextElement){
                        if(texts.containsKey("page" + element.getPageNumber())){
                            texts.get("page" + element.getPageNumber()).add(element.getYAMLData());
                        }else{
                            texts.put("page" + element.getPageNumber(), new ArrayList<>(Collections.singletonList(element.getYAMLData())));
                        }
                    }else if(element instanceof SkillTableElement skillTableElement){
                        if(skillTableElement.getAssessmentId() != 0){
                            skills = element.getYAMLData();
                        }else counter--;
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
                config.base.put("skills", skills);
                config.set("versionID", Main.VERSION_ID);
                config.save();
            }
            
        }catch(IOException e){
            Log.eNotified(e);
        }
    }
    
    public static int countElements(File editFile) throws Exception{
        
        if(!editFile.exists()){ //file does not exist
            return 0;
        }
        
        // file already exist
        Config config = new Config(editFile);
        config.load();
        
        int count = Stream.of("texts", "images", "vectors").mapToInt(s -> countSection(config.getSection(s))).sum();
        
        count += config.getList("grades")
                .stream()
                .filter(data -> data instanceof HashMap)
                .map(data -> GradeElement.getYAMLDataStats(convertInstanceOfObject(data, HashMap.class)))
                .filter(stats -> stats[0] != -1)
                .count();
        long assessmentId = Config.getLong(config.getSection("skills"), "assessmentId");
        if(assessmentId != 0) count++;
        
        return count;
    }
    
    public static EditionStats getEditionStats(File editFile) throws Exception{
        
        if(!editFile.exists()){ //file does not exist
            return null;
            
        }
        
        // file already exist
        Config config = new Config(editFile);
        config.load();
        
        int texts = countSection(config.getSection("texts"));
        int graphics = countSection(config.getSection("images")) + countSection(config.getSection("vectors"));
        
        double[] totalGrade = {-1, 0}; // Root grade value and total
        int grades = 0; // All grade element count
        int filledGrades = 0; // All entered grade
        
        for(Object data : config.getList("grades")){
            if(data instanceof HashMap){
                double[] stats = GradeElement.getYAMLDataStats(convertInstanceOfObject(data, HashMap.class));
                if(stats.length == 2) totalGrade = stats; // get the root grade value and the root grade total
                if(stats[0] != -1) filledGrades++;
                grades++;
            }
        }
        
        int totalCount = texts + graphics + filledGrades;
        
        long assessmentId = Config.getLong(config.getSection("skills"), "assessmentId");
        int skills = 0;
        int filledNotations = 0;
        SkillsAssessment assessment = null;
        if(assessmentId != 0){
            assessment = MainWindow.skillsTab.getAssessments().stream().filter(a -> a.getId() == assessmentId).findFirst().orElse(null);
            if(assessment != null){
                skills = assessment.getSkills().size();
                ArrayList<Object> notationsData = Config.getList(config.getSection("skills"), "list");
                for(Object notationData : notationsData){
                    if(notationData instanceof HashMap){
                        long skillId = Config.getLong((HashMap<String, Object>) notationData, "skillId");
                        long notationId = Config.getLong((HashMap<String, Object>) notationData, "notationId");
                        if(Skill.getById(assessment, skillId) != null){ // Check the skill does not belong to another assessment
                            if(notationId < 0 || Notation.getById(assessment, notationId) != null){  // Check the notation belongs to the assessment
                                filledNotations++;
                            }
                        }
                    }
                }
            }
        }
        if(filledNotations > 0) totalCount++;
        
        return new EditionStats(totalCount, texts, graphics, grades, filledGrades, totalGrade[0], totalGrade[1], assessment, skills, filledNotations);
    }
    
    public record EditionStats(int totalElements, int texts, int graphics, int grades, int filledGrades, double totalGradeValue, double totalGradeOutOf, SkillsAssessment assessment, int skills, int filledNotations){}

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
            Integer page = MathUtils.parseIntOrNull(pageData.getKey().replaceFirst("page", ""));
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
        path = StringUtils.removeAfterLastOccurrence(path, ".yml");
        
        if(!editFile.getName().contains("!P!") && PlatformUtils.isWindows()){
            return new File(File.separator + path);
        }
        if(!editFile.getName().startsWith("!E!") && !PlatformUtils.isWindows()){
            return new File(File.separator + path);
        }
        return new File(path);
    }
    
    public static void mergeEditFileWithEditFile(File fromEdit, File destEdit){
        try{
            Files.copy(fromEdit.toPath(), destEdit.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){
            Log.e(e);
            new ErrorAlert(ErrorAlert.unableToCopyFileHeader(fromEdit.getAbsolutePath(), destEdit.getAbsolutePath(), true), e.getMessage(), false).showAndWait();
        }
    }
    
    public static HashMap<File, File> getEditFilesWithSameName(File originFile){
        
        HashMap<File, File> files = new HashMap<>();
        
        for(File editFile : Objects.requireNonNull(new File(Main.dataFolder + "editions" + File.separator).listFiles())){
            
            File file = getFileFromEdit(editFile);
            
            if(file.getName().equals(originFile.getName()) && !file.equals(originFile)){
                files.put(editFile, file);
            }
        }
        return files;
        
    }
    
    public static void clearEdit(File file, boolean confirm){
        if(!confirm || new ConfirmAlert(true, TR.tr("dialog.confirmation.clearEdit.header")).execute()){
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
        if(!confirm || new ConfirmAlert(false, TR.tr("dialog.confirmation.clearEdit.header")).execute()){
            MainWindow.mainScreen.setSelected(null);
            for(PageRenderer page : document.getPages()){
                page.clearElements();
            }
            MainWindow.textTab.treeView.onFileSection.updateElementsList();
            MainWindow.skillsTab.clearEditRelatedData();
            MainWindow.gradeTab.treeView.clearElements(true, false);
            Edition.setUnsave("Clear edit");
            MainWindow.mainScreen.document.edition.save(false);
        }
    }
    
    public static boolean isSave(){
        return isSave.get();
    }
    
    public static void setUnsave(String sourceDebug){
        if(false) Log.t("Unsave Edition from: " + sourceDebug);
        
        isSave.set(false);
        MainWindow.footerBar.updateStats();
    }
    
    public static BooleanProperty isSaveProperty(){
        return isSave;
    }
}
