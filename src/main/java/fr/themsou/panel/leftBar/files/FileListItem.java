package fr.themsou.panel.leftBar.files;

import fr.themsou.document.editions.Edition;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.File;

public class FileListItem extends ListCell<File>{

    VBox pane;
    HBox nameBox;
    Text name;
    Text path;

    ImageView check = new ImageView();
    ImageView checkLow = new ImageView();

    ContextMenu menu;
    EventHandler<MouseEvent> onClick = e -> {
        if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) MainWindow.mainScreen.openFile(getItem());
    };

    public FileListItem(){
        setupGraphic();
    }

    public void setupGraphic(){
        setStyle("-fx-padding: 2 15;");

        pane = new VBox();
        nameBox = new HBox();
        name = new Text();
        path = new Text();

        HBox.setMargin(checkLow, new Insets(0, 4, 0, 0));
        HBox.setMargin(check, new Insets(0, 4, 0, 0));

        path.setFont(new Font(10));
        pane.getChildren().addAll(nameBox, path);
        setGraphic(pane);

        menu = FileListView.getNewMenu();
    }

    @Override
    public void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);

        if(empty || getItem() == null){
            setGraphic(null);
            setTooltip(null);
            setContextMenu(null);
        }else{

            path.setText(getItem().getAbsolutePath().replace(System.getProperty("user.home"), "~").replace(getItem().getName(), ""));
            path.setFill(Color.BLACK);

            name.setText(StringUtils.removeAfterLastRejex(file.getName(), ".pdf"));
            name.setFont(Font.font(null, FontWeight.NORMAL, 12));

            nameBox.getChildren().clear();
            menu.setId(file.getAbsolutePath());

            try{
                double[] elementsCount = Edition.countElements(Edition.getEditFile(file));

                if(elementsCount.length > 0){ // has edit file
                    String grade = (elementsCount[4] == -1 ? "?" : Main.format.format(elementsCount[4])) + "/" + Main.format.format(elementsCount[5]);

                    if(elementsCount[0] > 0){ // Has Elements

                        name.setFont(Font.font(null, FontWeight.BOLD, 12));

                        path.setText(path.getText() + " | " + Main.format.format(elementsCount[0]) + " " + TR.tr("Éléments") + " | " + grade);
                        setTooltip(new Tooltip(Main.format.format(elementsCount[0]) + " " + TR.tr("Éléments") + " | " + grade + "\n" + Main.format.format(elementsCount[1]) + " " + TR.tr("Commentaires") + "\n" + Main.format.format(elementsCount[2]) + "/" + Main.format.format(elementsCount[6]) + " " + TR.tr("Notes") + "\n" + Main.format.format(elementsCount[3]) + " " + TR.tr("Figures")));

                        if(elementsCount[2] == elementsCount[6]){ // Edition completed : Green check
                            if(check.getImage() == null) check.setImage(new Image(getClass().getResource("/img/FilesTab/check.png") + ""));
                            nameBox.getChildren().add(check);
                        }else if(elementsCount[2] >= 1){ // Edition semi-completed : Orange check
                            if(checkLow.getImage() == null) checkLow.setImage(new Image(getClass().getResource("/img/FilesTab/check_low.png") + ""));
                            nameBox.getChildren().add(checkLow);
                        }

                    }else{ // Don't have elements
                        path.setText(path.getText() + " | " + TR.tr("Non édité") + " | " + grade);
                        setTooltip(new Tooltip(TR.tr("Non édité") + " | " + grade + "\n" + Main.format.format(elementsCount[6]) + " " + TR.tr("Barèmes")));
                    }
                }else{ // don't have edit file
                    path.setText(path.getText() + " | " + TR.tr("Non édité"));
                    setTooltip(new Tooltip(TR.tr("Non édité")));
                }
            }catch(Exception e){
                path.setFill(Color.RED);
                path.setText(path.getText() + " | " + TR.tr("Impossible de récupérer les informations"));
                setTooltip(new Tooltip(e.getMessage()));
            }

            nameBox.getChildren().add(name);

            setGraphic(pane);
            setContextMenu(menu);
            setOnMouseClicked(onClick);
        }
    }

}
