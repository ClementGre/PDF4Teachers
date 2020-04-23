package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen;
import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import javafx.geometry.Insets;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.Arrays;
import java.util.regex.Pattern;

public class NoteTreeView extends TreeView<String> {


    public NoteTreeView(LBNoteTab noteTab){

        disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));
        prefHeightProperty().bind(noteTab.pane.heightProperty().subtract(layoutYProperty()));
        prefWidthProperty().bind(noteTab.pane.widthProperty());

        setCellFactory(new Callback<>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                return new TreeCell<>() {
                    @Override protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);

                        // Null
                        if(empty){
                            setGraphic(null);
                            setStyle(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                            return;
                        }
                        // String Data
                        if(item != null){
                            setGraphic(null);
                            setStyle(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                            return;
                        }
                        // TreeNoteData
                        if(getTreeItem() instanceof NoteTreeItem){
                            ((NoteTreeItem) getTreeItem()).updateCell(this);
                            return;
                        }

                        // Other
                        setStyle(null);
                        setGraphic(null);
                        setContextMenu(null);
                        setOnMouseClicked(null);

                    }
                };
            }
        });
    }

    public void clear(){
        System.out.println("clear");
        if(getRoot() != null) ((NoteTreeItem)getRoot()).getCore().delete();
        generateRoot();
    }

    public void generateRoot(){
        System.out.println("Generate root");
        Main.lbNoteTab.newNoteElement(TR.tr("Total"), -1, 20, 0, "");

        // DEBUG
        Main.lbNoteTab.newNoteElement("Bonus", -1, 3, 0, "Total");

        Main.lbNoteTab.newNoteElement("Exercice 1"   , -1, 10.5, 1, "Total");
        Main.lbNoteTab.newNoteElement("a"            , -1, 3.5, 0, "Total\\Exercice 1");
        Main.lbNoteTab.newNoteElement("b"            , -1, 3, 1, "Total\\Exercice 1");
        Main.lbNoteTab.newNoteElement("c"            , -1, 4, 2, "Total\\Exercice 1");

        Main.lbNoteTab.newNoteElement("Exercice 2"   , -1, 6.5, 2, "Total");
        Main.lbNoteTab.newNoteElement("a"            , -1, 3, 0, "Total\\Exercice 2");
        Main.lbNoteTab.newNoteElement("b"            , -1, 3, 1, "Total\\Exercice 2");
        Main.lbNoteTab.newNoteElement("c"            , -1, 1.5, 2, "Total\\Exercice 2");


    }

    public void addElement(NoteElement element){

        System.out.println("add element " + element.getName());
        if(element.getParentPath().isEmpty()){
            // ELEMENT IS ROOT
            if(getRoot() != null) ((NoteTreeItem) getRoot()).getCore().delete();
            setRoot(element.toNoteTreeItem());
        }else{
            // OTHER
            NoteTreeItem treeElement = element.toNoteTreeItem();
            addToList(getNoteTreeItemParent(element), treeElement);
            getRoot().setExpanded(true);
        }
    }
    public void removeElement(NoteElement element){

        System.out.println("remove element " + element.getName());
        if(element.getParentPath().isEmpty()){
            // ELEMENT IS ROOT
            ((NoteTreeItem) getRoot()).deleteChildren();
            setRoot(null);
        }else{
            // OTHER
            NoteTreeItem treeElement = getNoteTreeItem((NoteTreeItem) getRoot(), element);
            treeElement.deleteChildren();
            NoteTreeItem parent = (NoteTreeItem) treeElement.getParent();
            parent.getChildren().remove(treeElement);
            parent.reIndexChildren();
            parent.makeSum();
        }
    }

    public NoteTreeItem getNoteTreeItemParent(NoteElement element){

        // ELEMENT IS SUB-ROOT
        String[] path = Builders.cleanArray(element.getParentPath().split(Pattern.quote("\\")));
        if(path[0].equals(((NoteTreeItem)getRoot()).getCore().getName()) && path.length == 1){
            return (NoteTreeItem) getRoot();
        }

        // OTHER
        path = Builders.cleanArray(element.getParentPath()
                .replaceFirst(Pattern.quote(((NoteTreeItem)getRoot()).getCore().getName()), "")
                .split(Pattern.quote("\\")));

        NoteTreeItem parent = (NoteTreeItem) getRoot();
        for(String parentName : path){

            // Cherche l'enfant qui correspond au nom du chemin
            for(int i = 0; i < parent.getChildren().size(); i++){
                NoteTreeItem children = (NoteTreeItem) parent.getChildren().get(i);
                if(children.getCore().getName().equals(parentName)){
                    parent = children;
                    break;
                }
            }
        }
        if(parent.equals(getRoot())){
            System.err.println("L'element Note \"" + element.getName() + "\" a ete place dans Root car aucun parent ne lui a été retrouve.");
            System.err.println("ParentPath = " + element.getParentPath());
        }
        return parent;
    }
    public NoteTreeItem getNoteTreeItem(NoteTreeItem parent, NoteElement element){

        for(int i = 0; i < parent.getChildren().size(); i++){
            NoteTreeItem children = (NoteTreeItem) parent.getChildren().get(i);
            if(children.getCore().equals(element)){
                return children;
            }else if(children.hasSubNote()){
                // Si l'élément a des enfants, on refait le test sur ses enfants
                NoteTreeItem testChildren = getNoteTreeItem(children, element);
                if(testChildren != null) return testChildren;
            }
        }
        return null;

    }

    private void addToList(NoteTreeItem parent, NoteTreeItem element){

        int index = element.getCore().getIndex();
        int before = 0;

        for(int i = 0; i < parent.getChildren().size(); i++){
            NoteTreeItem children = (NoteTreeItem) parent.getChildren().get(i);
            if(children.getCore().getIndex() < index){
                before++;
            }
        }
        parent.getChildren().add(before, element);
    }


    public static String getElementPath(NoteTreeItem parent){
        return parent.getCore().getParentPath() + "\\" + parent.getCore().getName();
    }
    public static int getElementTier(String parentPath){
        if(parentPath.isEmpty()) return 0;
        return parentPath.split(Pattern.quote("\\")).length;
    }
}
