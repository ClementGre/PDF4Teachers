package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.panel.leftBar.grades.GradeTab;
import fr.clementgre.pdf4teachers.panel.leftBar.grades.TiersFont;
import fr.clementgre.pdf4teachers.panel.leftBar.texts.TextListItem;
import fr.clementgre.pdf4teachers.panel.leftBar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.leftBar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import javafx.application.Platform;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class UserData {

    @UserDataObject(path = "lastOpenDir")
    public String lastOpenDir = System.getProperty("user.home");

    @UserDataObject(path = "customColors")
    public List<String> customColors = new ArrayList<>();

    // FILES (FilesTab)
    @UserDataObject(path = "files.lastFile")
    public String lastOpenedFile = "";
    @UserDataObject(path = "files.lastFiles")
    public List<String> lastOpenedFiles = new ArrayList<>();

    // TEXTS Sections
    @UserDataObject(path = "texts.favorites")
    public List<Object> favoritesTextElements = new ArrayList<>();
    @UserDataObject(path = "texts.lasts")
    public List<Object> lastsTextElements = new ArrayList<>();
    @UserDataObject(path = "texts.lists")
    public HashMap<Object, Object> listsOfTextElements = new HashMap<>();
    @UserDataObject(path = "texts.lastFont.font")
    public String textLastFontName = "Open Sans";
    @UserDataObject(path = "texts.lastFont.size")
    public double textLastFontSize = 14;
    @UserDataObject(path = "texts.lastFont.color")
    public String textLastFontColor = "";
    @UserDataObject(path = "texts.lastFont.bold")
    public boolean textLastFontBold = false;
    @UserDataObject(path = "texts.lastFont.italic")
    public boolean textLastFontItalic = false;

    // GradesTab
    @UserDataObject(path = "grades.lockGradeScale")
    public boolean lockGradeScale = false;
    @UserDataObject(path = "grades.sumByDecrement")
    public boolean sumByDecrement = false;
    @UserDataObject(path = "grades.tiersFont")
    public LinkedHashMap<Object, Object> gradesTiersFont = new LinkedHashMap<>();

    // GradesExport Params & PdfExport Params
    @UserDataObject(path = "export.fields.fileName")
    public String lastExportFileName = "";
    @UserDataObject(path = "export.fields.fileNamePrefix")
    public String lastExportFileNamePrefix = "";
    @UserDataObject(path = "export.fields.fileNameSuffix")
    public String lastExportFileNameSuffix = "";
    @UserDataObject(path = "export.fields.fileNameReplace")
    public String lastExportFileNameReplace = "";
    @UserDataObject(path = "export.fields.fileNameBy")
    public String lastExportFileNameBy = "";
    @UserDataObject(path = "export.fields.studentNameReplace")
    public String lastExportStudentNameReplace = "";
    @UserDataObject(path = "export.fields.studentNameBy")
    public String lastExportStudentNameBy = "";
    @UserDataObject(path = "export.settings.onlySameGradeScale")
    public boolean settingsOnlySameGradeScale = true;
    @UserDataObject(path = "export.settings.onlyCompleted")
    public boolean settingsOnlyCompleted = false;
    @UserDataObject(path = "export.settings.onlySameDir")
    public boolean settingsOnlySameDir = false;
    @UserDataObject(path = "export.settings.attributeTotalLine")
    public boolean settingsAttributeTotalLine = false;
    @UserDataObject( path = "export.settings.attributeMoyLine")
    public boolean settingsAttributeMoyLine = true;
    @UserDataObject(path = "export.settings.withTxtElements")
    public boolean settingsWithTxtElements = false;
    @UserDataObject(path = "export.settings.tiersExportSlider")
    public long settingsTiersExportSlider = 2;

    // Convert Params
    @UserDataObject(path = "convert.fields.srcDir")
    public String lastConvertSrcDir = System.getProperty("user.home");
    @UserDataObject(path = "convert.fields.outFileName")
    public String lastConvertFileName = ".pdf";
    @UserDataObject(path = "convert.fields.convertDefinition")
    public String lastConvertDefinition = "";
    @UserDataObject(path = "convert.fields.convertFormat")
    public String lastConvertFormat = "";
    @UserDataObject(path = "convert.settings.convertAloneImages")
    public boolean settingsConvertAloneImages = true;
    @UserDataObject(path = "convert.settings.convertVoidFile")
    public boolean settingsConvertVoidFiles = true;

    private TextElementsData textElementsData;

    public UserData(){

        if(!Main.settings.getSettingsVersion().isEmpty()){
            loadDataFromYAML();
            textElementsData = new TextElementsData();
        }
    }
    public void save(){
        saveData();
        textElementsData.saveData();
    }

    private void loadDataFromYAML(){

        new Thread(() -> {

            try{
                new File(Main.dataFolder).mkdirs();
                File file = new File(Main.dataFolder + "userdata.yml");
                if(file.createNewFile()) return; // File does not exist or can't create it

                Config config = new Config(file);
                config.load();

                for(Field field : getClass().getDeclaredFields()) {
                    if(field.isAnnotationPresent(UserDataObject.class)){
                        try {
                            if(field.getType() == String.class){
                                String value = config.getString(field.getAnnotation(UserDataObject.class).path());
                                if(!value.isEmpty()) field.set(this, value);
                            }else if(field.getType() == boolean.class){
                                Boolean value = config.getBooleanNull(field.getAnnotation(UserDataObject.class).path());
                                if(value != null) field.set(this, value);
                            }else if(field.getType() == long.class){
                                Long value = config.getLongNull(field.getAnnotation(UserDataObject.class).path());
                                if(value != null) field.set(this, value);
                            }else if(field.getType() == double.class){
                                Double value = config.getDoubleNull(field.getAnnotation(UserDataObject.class).path());
                                if(value != null) field.set(this, value);
                            }else if(field.getType() == List.class){
                                List<Object> value = config.getListNull(field.getAnnotation(UserDataObject.class).path());
                                if(value != null) field.set(this, value);
                            }else if(field.getType() == HashMap.class){
                                field.set(this, config.getSection(field.getAnnotation(UserDataObject.class).path()));
                            }else if(field.getType() == LinkedHashMap.class){
                                field.set(this, config.getLinkedSection(field.getAnnotation(UserDataObject.class).path()));
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }catch(IOException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                if(Main.settings.isRestoreLastSession()){
                    for(Object filePath : lastOpenedFiles){
                        Platform.runLater(() ->{
                            File lastFile = new File(filePath.toString());
                            if(lastFile.exists()) MainWindow.filesTab.originalFiles.add(lastFile);
                            MainWindow.filesTab.backOpenFilesList(false);
                        });
                    }
                    File lastFile = new File(lastOpenedFile);
                    if(lastFile.exists()){
                        MainWindow.mainScreen.openFile(lastFile);
                    }
                }

                if(Main.settings.getSettingsVersion().equals("1.2.0")){
                    // TEXTS
                    for(Object data : favoritesTextElements){
                        if(data instanceof Map) MainWindow.textTab.treeView.favoritesSection.getChildren().add(TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.FAVORITE_TYPE));
                    }

                    for(Object data : lastsTextElements){
                        if(data instanceof Map) MainWindow.textTab.treeView.lastsSection.getChildren().add(TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.LAST_TYPE));
                    }

                    for(Map.Entry<Object, Object> list : listsOfTextElements.entrySet()){
                        if(list.getValue() instanceof List){
                            ArrayList<TextListItem> listTexts = new ArrayList<>();
                            for(Object data : ((List<Object>) list.getValue())){
                                listTexts.add(TextListItem.readYAMLDataAndGive(Config.castSection(data)));
                            }
                            TextTreeSection.lists.put(list.getKey().toString(), listTexts);
                        }
                    }

                }
                favoritesTextElements = new ArrayList<>();
                lastsTextElements = new ArrayList<>();
                listsOfTextElements = new LinkedHashMap<>();

                // GRADES
                int i = 0;
                for(Object font : gradesTiersFont.values()){
                    if(font instanceof Map){
                        GradeTab.fontTiers.put(i, TiersFont.getInstance((HashMap<String, Object>) font));
                    }
                    i++;
                }
                MainWindow.gradeTab.updateElementsFont();

                MainWindow.gradeTab.lockGradeScale.setSelected(lockGradeScale);
                MainWindow.gradeTab.sumByDecrement.setSelected(sumByDecrement);
                SyncColorPicker.loadCustomsColors(customColors.stream().map(Object::toString).collect(Collectors.toList()));
            });
        }).start();
    }

    private void saveData(){

        // SINGLES
        customColors = SyncColorPicker.getCustomColorsList();

        // FILES
        lastOpenedFiles = new ArrayList<>();
        for(File file : MainWindow.filesTab.originalFiles){
            lastOpenedFiles.add(file.getAbsolutePath());
        }
        lastOpenedFile =  MainWindow.mainScreen.hasDocument(false) ? MainWindow.mainScreen.document.getFile().getAbsolutePath() : "";

        // GRADES
        int i = 0;
        gradesTiersFont = new LinkedHashMap<>();
        for(TiersFont font : GradeTab.fontTiers.values()){
            LinkedHashMap<String, Object> data = font.getData();
            gradesTiersFont.put(i+"", data); i++;
        }
        lockGradeScale = MainWindow.gradeTab.lockGradeScale.isSelected();
        sumByDecrement = MainWindow.gradeTab.sumByDecrement.isSelected();


        try{
            new File(Main.dataFolder).mkdirs();
            Config config = new Config(new File(Main.dataFolder + "userdata.yml"));

            for(Field field : getClass().getDeclaredFields()) {
                if(field.isAnnotationPresent(UserDataObject.class)){
                    try{
                        config.set(field.getAnnotation(UserDataObject.class).path(), field.get(this));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            config.save();
        }catch(IOException e) {
            e.printStackTrace();
        }

    }
}
