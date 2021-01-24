package fr.clementgre.pdf4teachers.panel.sidebar;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class SideBar extends TabPane {

    public static Tab draggingTab = null;
    public static final String DRAG_TAB_KEY = "dragPDF4TeacherSideBarTab";

    public static int DEFAULT_WIDTH = 270;
    public static int MAX_WIDTH = 400;

    private final boolean left;
    public SideBar(boolean left){
        this.left = left;

        setStyle("-fx-tab-max-width: 22px;");

        setMaxWidth(DEFAULT_WIDTH);
        setMinWidth(DEFAULT_WIDTH);
        setPrefWidth(DEFAULT_WIDTH);
        setWidth(DEFAULT_WIDTH);

        Platform.runLater(() -> {
            setMaxWidth(MAX_WIDTH);
            setMinWidth(0);
        });

        SplitPane.setResizableWithParent(this, false);

        AtomicReference<TabPane> previewLastTabPane = new AtomicReference<>(null);
        AtomicReference<Tab> previewTab = new AtomicReference<>(null);

        setOnDragOver(e -> {
            final Dragboard dragboard = e.getDragboard();
            if(dragboard.hasString() && DRAG_TAB_KEY.equals(dragboard.getString())){
                if(draggingTab != null){

                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();

                    if(draggingTab.getTabPane() == this){ // Skip if tab is already in preview / already in this tab
                        int actualIndex = getTabs().indexOf(draggingTab);
                        int targetIndex = StringUtils.clamp((int) ((e.getX()-5) / 55), 0, getTabs().size()-1);

                        if(actualIndex != targetIndex){
                            getTabs().remove(draggingTab);
                            getTabs().add(targetIndex, draggingTab);
                            getSelectionModel().select(draggingTab);
                        }

                        return;
                    }

                    previewLastTabPane.set(draggingTab.getTabPane());
                    previewTab.set(draggingTab);


                    draggingTab.getTabPane().getTabs().remove(draggingTab);
                    int targetIndex = StringUtils.clamp((int) ((e.getX()-5) / 55), 0, getTabs().size()-1);
                    getTabs().add(targetIndex, draggingTab);
                    getSelectionModel().select(draggingTab);


                }
            }

        });
        setOnDragExited(e -> { // Remove the tab of this TabPane and re-add it into its original TabPane
            final Dragboard dragboard = e.getDragboard();
            if(dragboard.hasString() && DRAG_TAB_KEY.equals(dragboard.getString())){
                if(draggingTab != null && previewTab.get() != null){ // Check there is a tab who is temporary in this TabPane
                    if(draggingTab.getTabPane() != previewLastTabPane.get()){ // Check the Tab is not already into the target TabPane
                        getTabs().remove(draggingTab);
                        previewLastTabPane.get().getTabs().add(draggingTab);
                        previewLastTabPane.get().getSelectionModel().select(draggingTab);
                    }
                    previewTab.set(null);
                    previewLastTabPane.set(null);
                }
            }
        });
        setOnDragDropped(event -> { // Complete drop : Make the preview final
            final Dragboard dragboard = event.getDragboard();
            if(dragboard.hasString() && DRAG_TAB_KEY.equals(dragboard.getString())){
                if(draggingTab != null){
                    previewTab.set(null);
                    previewLastTabPane.set(null);

                    event.setDropCompleted(true);
                    event.consume();
                }
            }
        });

    }

    public static void moveTab(Tab tab){
        if(isIntoLeftBar(tab)){
            MainWindow.leftBar.getSelectionModel().select(tab);
        }else if(isIntoRightBar(tab)){
            MainWindow.rightBar.getSelectionModel().select(tab);
        }
    }

    public static void selectTab(Tab tab){
        if(isIntoLeftBar(tab)){
            MainWindow.leftBar.getSelectionModel().select(tab);
        }else if(isIntoRightBar(tab)){
            MainWindow.rightBar.getSelectionModel().select(tab);
        }
    }

    public static boolean isIntoLeftBar(Tab tab){
        return MainWindow.leftBar.getTabs().contains(tab);
    }
    public static boolean isIntoRightBar(Tab tab){
        return MainWindow.rightBar.getTabs().contains(tab);
    }

    public static void setupDividers(SplitPane mainPane){
    }
}
