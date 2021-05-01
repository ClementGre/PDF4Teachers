package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguagesUpdater;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTab;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.TiersFont;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextListItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
@SuppressWarnings("unchecked")
public class UserData{
    
    @UserDataObject(path = "lastOpenDir")
    public String lastOpenDir = System.getProperty("user.home");
    
    @UserDataObject(path = "customColors")
    public List<String> customColors = new ArrayList<>();
    
    @UserDataObject(path = "uuid")
    public String uuid = UUID.randomUUID().toString();
    
    @UserDataObject(path = "stats.foregroundTime")
    public long foregroundTime = 0;
    @UserDataObject(path = "stats.startsCount")
    public long startsCount = 0;
    
    // OTHER
    @UserDataObject(path = "languages")
    public HashMap<String, Object> languages = new HashMap<>();
    
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
    @UserDataObject(path = "grades.tiersFont")
    public LinkedHashMap<Object, Object> gradesTiersFont = new LinkedHashMap<>();
    
    // PaintTab
    @UserDataObject(path = "paintTab.gallery.paths")
    public List<String> galleryPaths = new ArrayList<>();
    
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
    @UserDataObject(path = "export.settings.attributeMoyLine")
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
    
    // Sub classes :
    private TextElementsData textElementsData;
    private VectorElementsData vectorElementsData;
    public FavouriteImageData favouriteImageData;
    
    // auto tips
    @UserDataObject(path = "AutoTipsValidated")
    public List<Object> autoTipsValidated = new ArrayList<>();
    
    private static Thread userDataSaver = new Thread(() -> {
        while(true){
            try{
                Thread.sleep(1000 * 60);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            if(Main.window.isFocused()){
                MainWindow.userData.foregroundTime++;
                if(MainWindow.userData.foregroundTime % (60 * 50) == 0){
                    Platform.runLater(() -> {
                        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("dialog.donateRequest.title"),
                                TR.tr("dialog.donateRequest.header", (int) (MainWindow.userData.foregroundTime / 60)),
                                TR.tr("dialog.donateRequest.details"));
                        ButtonType paypal = new ButtonType("Paypal", ButtonBar.ButtonData.OTHER);
                        ButtonType github = new ButtonType("GitHub Sponsors", ButtonBar.ButtonData.OTHER);
                        ButtonType ignore = new ButtonType(TR.tr("actions.ignore"), ButtonBar.ButtonData.YES);
                        alert.getButtonTypes().setAll(paypal, github, ignore);
                        Optional<ButtonType> option = alert.showAndWait();
                        if(option.get() == paypal){
                            Main.hostServices.showDocument("https://paypal.me/themsou");
                        }else if(option.get() == github){
                            Main.hostServices.showDocument("https://github.com/sponsors/ClementGre");
                        }
                    });
                }
                if(MainWindow.userData.foregroundTime % (60) == 0){
                    LanguagesUpdater.backgroundStats();
                }
            }
            MainWindow.userData.save();
        }
    }, "userData AutoSaver");
    
    public UserData(){
        if(!userDataSaver.isAlive()) userDataSaver.start();
        
        Platform.runLater(() -> {
            loadDataFromYAML();
            textElementsData = new TextElementsData();
            vectorElementsData = new VectorElementsData();
            favouriteImageData = new FavouriteImageData();
        });
        
    }
    
    public void save(){
        saveData();
        textElementsData.saveData();
        vectorElementsData.saveData();
        favouriteImageData.saveData();
        Main.syncUserData.save();
    }
    
    private void loadDataFromYAML(){
        
        new Thread(() -> {
            
            try{
                new File(Main.dataFolder).mkdirs();
                File file = new File(Main.dataFolder + "userdata.yml");
                if(file.createNewFile()){
                    LanguagesUpdater.backgroundCheck();
                    AutoTipsManager.load();
                    return; // File does not exist or can't create it
                }
                
                Config config = new Config(file);
                config.load();
                
                for(Field field : getClass().getDeclaredFields()){
                    if(field.isAnnotationPresent(UserDataObject.class)){
                        try{
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
            }catch(IOException e){
                e.printStackTrace();
            }
            
            Platform.runLater(() -> {
                if(Main.settings.restoreLastSession.getValue()){
                    for(Object filePath : lastOpenedFiles){
                        Platform.runLater(() -> {
                            File lastFile = new File(filePath.toString());
                            if(lastFile.exists()) MainWindow.filesTab.originalFiles.add(lastFile);
                            MainWindow.filesTab.backOpenFilesList(false);
                        });
                    }
                    File lastFile = new File(lastOpenedFile);
                    if(lastFile.exists() && !MainWindow.mainScreen.hasDocument(false)){
                        Platform.runLater(() -> MainWindow.mainScreen.openFile(lastFile));
                    }
                }
                
                if(Main.settings.getSettingsVersion().equals("1.2.0")){
                    // TEXTS
                    for(Object data : favoritesTextElements){
                        if(data instanceof Map)
                            MainWindow.textTab.treeView.favoritesSection.getChildren().add(TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.FAVORITE_TYPE));
                    }
                    
                    for(Object data : lastsTextElements){
                        if(data instanceof Map)
                            MainWindow.textTab.treeView.lastsSection.getChildren().add(TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.LAST_TYPE));
                    }
                    
                    for(Map.Entry<Object, Object> list : listsOfTextElements.entrySet()){
                        if(list.getValue() instanceof List){
                            ArrayList<TextListItem> listTexts = new ArrayList<>();
                            for(Object data : ((List<?>) list.getValue())){
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
                
                startsCount++;
                
                MainWindow.gradeTab.lockGradeScale.setSelected(lockGradeScale);
                SyncColorPicker.loadCustomsColors(customColors.stream().map(Object::toString).collect(Collectors.toList()));
                TR.loadLanguagesConfig(languages);
                
                LanguagesUpdater.backgroundCheck();
                
                AutoTipsManager.load();
                
            });
        }).start();
    }
    
    private void saveData(){
        
        // SINGLES
        customColors = SyncColorPicker.getCustomColorsList();
        languages = TR.getLanguagesConfig();
        
        // FILES
        lastOpenedFiles = new ArrayList<>();
        for(File file : MainWindow.filesTab.originalFiles){
            lastOpenedFiles.add(file.getAbsolutePath());
        }
        lastOpenedFile = MainWindow.mainScreen.hasDocument(false) ? MainWindow.mainScreen.document.getFile().getAbsolutePath() : "";
        
        // GRADES
        int i = 0;
        gradesTiersFont = new LinkedHashMap<>();
        for(TiersFont font : GradeTab.fontTiers.values()){
            LinkedHashMap<String, Object> data = font.getData();
            gradesTiersFont.put(i + "", data);
            i++;
        }
        lockGradeScale = MainWindow.gradeTab.lockGradeScale.isSelected();
        
        autoTipsValidated = AutoTipsManager.getCompletedAutoTips().stream().map(item -> (Object) item).collect(Collectors.toList());
        
        try{
            new File(Main.dataFolder).mkdirs();
            Config config = new Config(new File(Main.dataFolder + "userdata.yml"));
            
            for(Field field : getClass().getDeclaredFields()){
                if(field.isAnnotationPresent(UserDataObject.class)){
                    try{
                        config.set(field.getAnnotation(UserDataObject.class).path(), field.get(this));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            
            config.save();
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
}
