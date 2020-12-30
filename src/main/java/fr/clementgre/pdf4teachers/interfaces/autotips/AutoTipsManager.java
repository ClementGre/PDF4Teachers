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
    private static final String textElementsLists = TR.ct("Vous pouvez enregistrer les éléments favoris / précédents à part avec l'icône de sauvegarde. Pour recharger la liste d'éléments, utilisez l'icône de liste.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textElementsSort = TR.ct("Vous pouvez trier les éléments textuels grâce à l'icône de tri.");

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textAddLink = TR.ct("Utilisez Shift+Clic (ou Shift+Entrée) pour ajouter et lier : toute modification apportée à l'élément sur le document sera appliquée à l'élément de la liste.");

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


    @ToolTipVar(actionKey = "opendocument", prerequisiteKey = "", objectWhereDisplay = "mainscreen")
    private static final String editSystemAndExportation = TR.ct("Toutes les modifications apportés au document sont enregistrés dans un fichier spécial, à part. Utilisez le menu Fichier > Exporter pour regénérer le fichier PDF. Attention : les modifications de pages (rotations, suppression / ajout de pages) modifient immédiatement le fichier d'origine.");

    @ToolTipVar(actionKey = "newtextelement", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String newTextElement = TR.ct("Faites un double clic sur le document pour ajouter un nouvel élément textuel plus rapidement (ou Ctrl+T).");

    @ToolTipVar(actionKey = "gradescalelock", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeScaleLock = TR.ct("Vous venez de verrouiller le barème. Vous ne pourrez plus créer ou supprimer de notes et modifier le total ou le nom des notes.");

    @ToolTipVar(actionKey = "gradescaleinvert", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeScaleInvert = TR.ct("Le mode de comptage des points par retranchement initialise chaque note à son total au lieu de 0. L'absence de saisie de note mène donc à la note totale maximale.");

    @ToolTipVar(actionKey = "gradeselect", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeContextMenu = TR.ct("Faites un clic droit sur une note pour éditer sa valeur et accéder à de nombreuses options.");

    @ToolTipVar(actionKey = "gradecreate", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String createGradeSameTiers = TR.ct("Le système de barème fonctionne hiérarchiquement. Pour créer une note de même niveau que celle sélectionnée, cliquez sur le + de sa note parente, ou pressez Ctrl+G.");

    @ToolTipVar(actionKey = "graderename", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeBonus = TR.ct("Vous pouvez appeler une note \"Bonus<...>\" pour qu'elle ne soit pas comptée dans le total du barème (mais comptée dans la note).");

    @ToolTipVar(actionKey = "graderename", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeKeyboardShortcuts = TR.ct("Utilisez TAB et Entrée pour vous déplacer plus rapidement parmi les notes du barème.");

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "hastextsimilarelements", objectWhereDisplay = "")
    private static final String textAutoCompletion = TR.ct("Utilisez les flèches du clavier puis entrée pour ajouter un élément semblable de la liste.");

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textAutoWrap = TR.ct("Des retours à la ligne sont ajoutés automatiquement si l'élément est plus large que la page. Faites un clic droit sur le champ de saisie de texte pour supprimer les retours à la ligne inutiles (par exemple si la taille de police a été réduite).");

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textLatex = TR.ct("Les éléments textuels peuvent être écrits en LaTeX s'il commencent par $. Le LaTeX permet d'écrire des équations mathématiques et d'afficher des symboles spéciaux.");

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textUrl = TR.ct("Les éléments textuels commençant par www. ou http:// ou https:// seront traduits en liens cliquables lors de l'exportation.");

    @ToolTipVar(actionKey = "textselect", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textContextMenu = TR.ct("Ajoutez un élément textuel aux favoris grâce à son menu contextuel (clic droit).");

    @ToolTipVar(actionKey = "textdoubleclick", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textDuplicate = TR.ct("Double cliquez sur un élément textuel pour le dupliquer.");

    @ToolTipVar(actionKey = "gradedoubleclick", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeSet0WithDoubleClick = TR.ct("Double cliquez sur une note pour la mettre à 0.");

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

    private static Thread autoTipsThread = new Thread(() -> {
        while (true){
            try{
                Thread.sleep(3*60*1000);
            }catch(InterruptedException e){ e.printStackTrace(); }

            Platform.runLater(AutoTipsManager::showRandom);
        }
    }, "AutoTipsTimer");

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

                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        if(!stillHasAuto) return;
        if(!autoTipsThread.isAlive()) autoTipsThread.start();

    }
    public static List<String> getCompletedAutoTips(){

        List<String> completed = new ArrayList<>();

        for(Field field : AutoTipsManager.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(ToolTipVar.class)){
                try{
                    String name = field.getName();
                    if(!uiTips.containsKey(name)){
                        completed.add(name);
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
            case "texttabselected" -> {
                return MainWindow.textTab.isSelected();
            }
            case "gradetabselected" -> {
                return MainWindow.gradeTab.isSelected();
            }
            case "hastextsimilarelements" -> {
                return MainWindow.textTab.treeView.getSelectionModel().getSelectedIndices().size() >= 2;
            }
        }
        return false;
    }
}
