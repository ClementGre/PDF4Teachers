package fr.themsou.panel.leftBar.grades;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.geometry.Insets;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

public class GradeTreeView extends TreeView<String> {


    public GradeTreeView(LBGradeTab gradeTab){

        disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));
        prefHeightProperty().bind(gradeTab.pane.heightProperty().subtract(layoutYProperty()));
        prefWidthProperty().bind(gradeTab.pane.widthProperty());

        setCellFactory(new Callback<>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                return new TreeCell<>() {
                    @Override protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);

                        // Enpty cell or String Data
                        if(empty || item != null){
                            setGraphic(null);
                            setStyle(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                            setTooltip(null);
                            return;
                        }
                        // TreeGradeData
                        if(getTreeItem() instanceof GradeTreeItem){
                            ((GradeTreeItem) getTreeItem()).updateCell(this);
                            return;
                        }
                        // Other
                        setStyle(null);
                        setGraphic(null);
                        setContextMenu(null);
                        setOnMouseClicked(null);
                        setTooltip(null);
                    }
                };
            }
        });
    }

    // Clear without removing elements from pages
    public void hardClear(){
        setRoot(null);
        generateRoot(false);
    }
    // Clear and removing element from pages
    public void clear(){
        if(getRoot() != null) ((GradeTreeItem) getRoot()).getCore().delete();
        generateRoot(false);
    }

    public void generateRoot(boolean update){
        MainWindow.lbGradeTab.newGradeElement(TR.tr("Total"), -1, 0, 0, "", update);
        //MainWindow.lbGradeTab.newGradeElement(TR.tr("Bonus"), -1, 0, 0, "\\Total", update);
    }

    public void addElement(GradeElement element){

        if(element.getParentPath().isEmpty()){
            // ELEMENT IS ROOT
            if(getRoot() != null) ((GradeTreeItem) getRoot()).getCore().delete();
            setRoot(element.toGradeTreeItem());
            getSelectionModel().select(getRoot());
        }else{
            // OTHER
            GradeTreeItem treeElement = element.toGradeTreeItem();
            addToList(getGradeTreeItemParent(element), treeElement);
            getRoot().setExpanded(true);
        }
    }
    public void removeElement(GradeElement element){

        if(element.getParentPath().isEmpty()){
            // ELEMENT IS ROOT
            ((GradeTreeItem) getRoot()).deleteChildren();
            setRoot(null);
        }else{
            // OTHER
            GradeTreeItem treeElement = getGradeTreeItem((GradeTreeItem) getRoot(), element);
            treeElement.deleteChildren();
            GradeTreeItem parent = (GradeTreeItem) treeElement.getParent();
            parent.getChildren().remove(treeElement);
            parent.reIndexChildren();
            parent.makeSum();
        }
    }

    public GradeTreeItem getGradeTreeItemParent(GradeElement element){

        // ELEMENT IS SUB-ROOT
        String[] path = Builders.cleanArray(element.getParentPath().split(Pattern.quote("\\")));
        if(path[0].equals(((GradeTreeItem)getRoot()).getCore().getName()) && path.length == 1){
            return (GradeTreeItem) getRoot();
        }

        // OTHER
        path = Builders.cleanArray(element.getParentPath()
                .replaceFirst(Pattern.quote(((GradeTreeItem)getRoot()).getCore().getName()), "")
                .split(Pattern.quote("\\")));

        GradeTreeItem parent = (GradeTreeItem) getRoot();
        for(String parentName : path){

            // Cherche l'enfant qui correspond au nom du chemin
            for(int i = 0; i < parent.getChildren().size(); i++){
                GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);
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
    public GradeTreeItem getGradeTreeItem(GradeTreeItem parent, GradeElement element){

        for(int i = 0; i < parent.getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);
            if(children.getCore().equals(element)){
                return children;
            }else if(children.hasSubGrade()){
                // Si l'élément a des enfants, on refait le test sur ses enfants
                GradeTreeItem testChildren = getGradeTreeItem(children, element);
                if(testChildren != null) return testChildren;
            }
        }
        return null;

    }

    private void addToList(GradeTreeItem parent, GradeTreeItem element){

        int index = element.getCore().getIndex();
        int before = 0;

        for(int i = 0; i < parent.getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);
            if(children.getCore().getIndex() < index){
                before++;
            }
        }
        parent.getChildren().add(before, element);
    }

    public static GradeElement getNextGrade(int page, int y){

        ArrayList<GradeElement> items = getGradesArrayByCoordinates();
        GradeElement before = null;
        GradeElement after = items.size() >= 2 ? items.get(1) : null;

        int i = 0;
        for(GradeElement grade : items){
            int minPage = 0; int minY = 0;
            if(before != null){
                minPage = before.getPageNumber();
                minY = (int) before.getLayoutY();
            }
            int maxPage = 999999; int maxY = 999999;
            if(after != null){
                maxPage = after.getPageNumber();
                maxY = (int) after.getLayoutY();
            }
            if((page == maxPage && y < maxY || page < maxPage) && (page == minPage && y > minY || page > minPage)){
                return grade;
            }
            i++;
            before = grade;
            after = items.size() >= i+2 ? items.get(i+1) : null;
        }
        return null;
    }
    public static GradeTreeItem getNextLogicGrade(){
        ArrayList<GradeTreeItem> items = getGradesArray(GradeTreeView.getTotal());
        for(GradeTreeItem grade : items){
            if(!grade.hasSubGrade() && grade.getCore().getValue() == -1) return grade;
        }
        return null;
    }
    public static GradeTreeItem getNextLogicGradeNonNull(){
        ArrayList<GradeTreeItem> items = getGradesArray(GradeTreeView.getTotal());
        for(GradeTreeItem grade : items){
            if(!grade.hasSubGrade() && grade.getCore().getValue() == -1) return grade;
        }
        for(GradeTreeItem grade : items){
            if(!grade.hasSubGrade()) return grade;
        }
        return items.get(0);
    }

    public static void defineNaNLocations(){
        ArrayList<GradeTreeItem> items = getGradesArray(GradeTreeView.getTotal());
        ArrayList<GradeTreeItem> itemsToSend = new ArrayList<>();

        boolean afterItemHaveToDropDown = false;
        int i = 0;
        for(GradeTreeItem item : items){

            if(item.getCore().getValue() == -1 && item.isRoot()){ // ramène le root tout en haut si il n'a pas de valeur
                if(item.getCore().getPageNumber() != 0) item.getCore().switchPage(0);
                item.getCore().setRealY(0);
            }

            // Drop down grades if item is visible
            if(item.getCore().getValue() != -1){
                // Ramène tous les éléments au niveau de celui-ci
                for(GradeTreeItem itemToSend : itemsToSend){
                    if(itemToSend.getCore().getPageNumber() != item.getCore().getPageNumber()) itemToSend.getCore().switchPage(item.getCore().getPageNumber());
                    itemToSend.getCore().setRealY((int) ((item.getCore().getLayoutY() - itemToSend.getCore().getLayoutBounds().getHeight()) * Element.GRID_HEIGHT / item.getCore().getPage().getHeight()));
                }
                itemsToSend = new ArrayList<>();
            }

            if(items.size() > i+1){
                GradeTreeItem afterItem = items.get(i+1);

                if(afterItem.getCore().getValue() == -1){ // si l'élément d'après n'a pas de valeur
                    if((item.getCore().getValue() != -1 || item.hasSubGrade()) && !afterItemHaveToDropDown){
                        // Cas 1 : Ramène l'élément d'après à celui-ci

                        if(afterItem.getCore().getPageNumber() != item.getCore().getPageNumber()){
                            afterItem.getCore().switchPage(item.getCore().getPageNumber());
                        }
                        afterItem.getCore().setRealY((int) ((item.getCore().getLayoutY() + afterItem.getCore().getLayoutBounds().getHeight()) * Element.GRID_HEIGHT / item.getCore().getPage().getHeight()));

                        afterItemHaveToDropDown = false;
                    }else{
                        // Cas 2 : Demande a envoyer plus bas l'élément d'après
                        itemsToSend.add(afterItem);
                        afterItemHaveToDropDown = true;
                    }
                }else{
                    afterItemHaveToDropDown = false;
                }
            }
            i++;
        }
        // Drop down all others items in the dropDown array
        for(GradeTreeItem itemToSend : itemsToSend){
            if(itemToSend.getCore().getPageNumber() != MainWindow.mainScreen.document.pages.size()-1){
                itemToSend.getCore().switchPage(MainWindow.mainScreen.document.pages.size()-1);
            }
            itemToSend.getCore().setRealY((int) Element.GRID_HEIGHT);
        }
    }

    public static GradeTreeItem getTotal(){
        return (GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot();
    }

    public static ArrayList<GradeTreeItem> getGradesArray(GradeTreeItem root){
        ArrayList<GradeTreeItem> items = new ArrayList<>();
        items.add(root);

        for(int i = 0; i < root.getChildren().size(); i++){
            if(((GradeTreeItem) root.getChildren().get(i)).hasSubGrade()){
                items.addAll(getGradesArray((GradeTreeItem) root.getChildren().get(i)));
            }else{
                items.add((GradeTreeItem) root.getChildren().get(i));
            }
        }
        return items;
    }
    public static ArrayList<GradeElement> getGradesArrayByCoordinates(){

        ArrayList<GradeElement> grades = new ArrayList<>();

        for(PageRenderer page : MainWindow.mainScreen.document.pages){
            ArrayList<GradeElement> pageGrades = new ArrayList<>();
            for(Element element : page.getElements()){
                if(element instanceof GradeElement) pageGrades.add((GradeElement) element);
            }

            pageGrades.sort(Comparator.comparingInt(Element::getRealY));
            grades.addAll(pageGrades);
        }
        return grades;
    }


    public static String getElementPath(GradeTreeItem parent){
        return parent.getCore().getParentPath() + "\\" + parent.getCore().getName();
    }
    public static int getElementTier(String parentPath){
        return Builders.cleanArray(parentPath.split(Pattern.quote("\\"))).length;
    }
}
