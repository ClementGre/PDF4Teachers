package fr.clementgre.pdf4teachers.interfaces.autotips;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.application.Platform;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoTipsManager {

    @ToolTipVar(actionKey = "", prerequisiteKey = "document", objectWhereDisplay = "mainscreen")
    private static final String documentContextMenu = TR.ct("Faites un clic droit sur la page pour effectuer des actions rapides");

    @ToolTipVar(actionKey = "newtextelement", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String newTextElement = TR.ct("Faites un double clic sur le document pour ajouter un élément textuel plus rapidement. Vous pouvez aussi utiliser Ctrl+T");

    private static HashMap<String, AutoTipTooltip> uiTips = new HashMap<>();

    public static void setup(){
        Main.settings.allowAutoTips.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                addAllTips();
            }else{
                removeAllTips();
            }
        });
    }
    public static void load(){
        uiTips.clear();

        if(!Main.settings.allowAutoTips.getValue()) return;

        boolean stillHasAuto = false;

        for(Field field : AutoTipsManager.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(ToolTipVar.class)){
                try{

                    String name = field.getName();
                    if(!MainWindow.userData.autoTipsValidated.contains(name)){

                        ToolTipVar data = field.getAnnotation(ToolTipVar.class);
                        uiTips.put(name, new AutoTipTooltip(name, data.actionKey(), data.prerequisiteKey(), data.objectWhereDisplay(), (String) field.get(AutoTipsManager.class)));

                        if(data.actionKey().isEmpty()) stillHasAuto = true;

                    }else{
                        System.out.println("skiped " + name + " because of validated");
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        if(!stillHasAuto) return;

        new Thread(() -> {
            while (true){
                try{
                    Thread.sleep(10*1000);
                }catch(InterruptedException e){ e.printStackTrace(); }

                Platform.runLater(AutoTipsManager::showRandom);
            }
        }, "AutoTipsTimer").start();
    }
    public static List<String> getCompletedAutoTips(){

        List<String> completed = new ArrayList<>();

        for(Field field : AutoTipsManager.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(ToolTipVar.class)){
                try{
                    String name = field.getName();
                    if(!uiTips.containsKey(name)){
                        completed.add(name);
                    }else{
                        System.out.println("skiped " + name + " because of existing in the list");
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return completed;
    }

    public static boolean showRandom(){

        for(AutoTipTooltip uiTip : uiTips.values()){
            if(uiTip.getActionKey().isEmpty()){
                if(showByObject(uiTip)) return true;
            }
        }
        return false;
    }

    public static boolean showByAction(String actionKey){

        for(AutoTipTooltip uiTip : uiTips.values()){
            if(uiTip.getActionKey().equalsIgnoreCase(actionKey)){
                if(showByObject(uiTip)) return true;
            }
        }
        return false;
    }
    public static boolean showByName(String name){

        if(uiTips.containsKey(name)){
            return showByObject(uiTips.get(name));
        }
        return false;
    }
    public static boolean showByObject(AutoTipTooltip uiTip){

        if(isPrerequisiteValid(uiTip.getPrerequisiteKey())){
            uiTip.show(Main.window);
            return true;
        }
        return false;
    }

    public static void removeTip(String name){
        uiTips.remove(name);
    }
    public static void removeAllTips(){
        uiTips.clear();
    }
    public static void addAllTips(){
        MainWindow.userData.autoTipsValidated.clear();
        load();
    }

    private static boolean isPrerequisiteValid(String prerequisiteKey){
        if(prerequisiteKey.isEmpty()) return true;
        switch (prerequisiteKey){
            case "document" -> {
                return MainWindow.mainScreen.hasDocument(false);
            }
        }
        return false;
    }
}
