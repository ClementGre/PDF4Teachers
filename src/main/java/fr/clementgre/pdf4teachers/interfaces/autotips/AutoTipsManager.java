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
    private static final String documentContextMenu = TR.ct("Faites un clic droit sur la page pour effectuer des actions rapides.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "document", objectWhereDisplay = "mainscreen")
    private static final String documentZoom = TR.ct("Utilisez Ctrl + Molette pour zoomer");


    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textElementsLists = TR.ct("Vous pouvez enregistrer les éléments favoris / précédents à part avec l'icone de sauvegarde. Pour recharger la liste d'éléments, utilisez l'icone de liste.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textElementsSort = TR.ct("Vous pouvez trier les éléments textuels grâce à l'icone de tri.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textAddLink = TR.ct("Utilisez Shift+Clic (ou Shift + Entrée) pour ajouter et lier : toute modification apportée à l'élément sur le document sera appliqué à l'élément de la liste.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textKeyboardShortcutAddFavorite = TR.ct("Utilisez Ctrl+[1-9] pour ajouter l'élément favoris qui correspond au chiffre entré.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textSettingsMenuBar = TR.ct("Vous pouvez réduire la taille d'affichage des éléments dans les listes via le menu préférences de la barre de menu.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "gradetabselected", objectWhereDisplay = "leftbar")
    private static final String gradeCustomFont = TR.ct("Cliquez sur le bouton engrenage pour modifier les polices et les comportements des notes.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "gradetabselected", objectWhereDisplay = "leftbar")
    private static final String gradeExportCsv = TR.ct("Cliquez sur le bouton exportation pour exporter vos notes dans un tableur (.csv)");

    @ToolTipVar(actionKey = "", prerequisiteKey = "gradetabselected", objectWhereDisplay = "leftbar")
    private static final String gradeCopyGradeScale = TR.ct("Cliquez sur le bouton lien pour copier le barème sur d'autres documents.");


		@ToolTipVar(actionKey = "opendocument", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textAddLink = TR.ct("Toutes les modifications apportés au document sont enregistrés à part. Utilisez le menu Fichier > Exporter pour regénérer le fichier PDF. (Les modifications de pages (rotations, supressions, ajout de pages) modifieront le fichier d'origine.)");

    @ToolTipVar(actionKey = "newtextelement", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String newTextElement = TR.ct("Faites un double clic sur le document pour ajouter un élément textuel plus rapidement. Vous pouvez aussi utiliser Ctrl+T");

    @ToolTipVar(actionKey = "gradescalelock", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeScaleLock = TR.ct("Vous venez de veerouiller le barème. Vous ne pourrez plus l'éditer.");

    @ToolTipVar(actionKey = "gradescaleinvert", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeScaleInvert = TR.ct("Le mode de comptage des points par retranchement utilise le total comme note par défaut et compte négativement les points entrés.");

    @ToolTipVar(actionKey = "gradeselect", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeContextMenu = TR.ct("Faites un clic droit sur une note pour éditer sa valeur et accéder à de nombreuses options.");

    @ToolTipVar(actionKey = "gradecreate", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String createGradeSameTiers = TR.ct("Le système de barème fonctionne hiérarchiquement. Pour créer une note de même niveau que celle sélexionné, cliquez sur le + de sa note parente, ou pressez Ctrl+G");

    @ToolTipVar(actionKey = "graderename", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeBonus = TR.ct("Vous pouvez appeler une note \"Bonus [...]\" pour que son total ne soit pas compté.");

    @ToolTipVar(actionKey = "graderename", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeKeyboardShortcuts = TR.ct("Utilisez TAB et Entrée pour vous déplacer plus rapidement parmis les notes du barème.");

    @ToolTipVar(actionKey = "textEdit", prerequisiteKey = "hastextsimilarelements", objectWhereDisplay = "")
    private static final String textAutoCompletion = TR.ct("Utilisez les flèches du clavier puis entrée pour ajouter un élément semblable de la liste.");

    @ToolTipVar(actionKey = "textEdit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textAutoWrap = TR.ct("Des retours à la ligne sont ajoutés automatiquement si l'élément est plus large que la page. Faites un clic droit sur le champ de texte pour supprimer les retours à la ligne inutiles (si la taile de police a baissé ou si vous avez édité le texte au milieu par exemple).");

    @ToolTipVar(actionKey = "textEdit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textLatex = TR.ct("Les éléments textuels peuvent être écrits en LaTeX s'il commencent par $. Le LaTeX permet d'écrire des équations mathématiques et d'afficher des symboles spéciaux.");

    @ToolTipVar(actionKey = "textEdit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textUrl = TR.ct("Les éléments textuels commençant par www. ou http:// ou https:// seront traduits en liens cliquables lors de l'exportation.");

    @ToolTipVar(actionKey = "textselect", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textContextMenu = TR.ct("Ajoutez un élément textuel aux favoris grâce à son menu contextuel (clic droit).");

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
