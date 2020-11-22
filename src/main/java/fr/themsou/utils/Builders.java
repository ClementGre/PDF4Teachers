package fr.themsou.utils;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.components.NodeMenuItem;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class Builders {

    public static ImageView buildImage(Image image, int width, int height) {
        ImageView imageView = new ImageView(image);

        if(width == 0 && height == 0) return imageView;

        if(width == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }
    public static ImageView buildImage(String imgPath, int width, int height) {
        return buildImage(imgPath, width, height, null);
    }
    public static ImageView buildImage(String imgPath, int width, int height, Effect effect) {
        ImageView imageView = new ImageView(new Image(imgPath));

        if(effect != null) imageView.setEffect(effect);

        if(width == 0 && height == 0) return imageView;

        if(width == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }
    public static ImageView buildImage(FileInputStream file, int width, int height) {
        ImageView imageView = new ImageView(new Image(file));

        if(width == 0 && height == 0) return imageView;

        if(width == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }

    public static void setPosition(Region element, double x, double y, double width, double height, boolean force){

        if(x >= 0){
            element.setLayoutX(x);
        }if(y >= 0){
            element.setLayoutY(y);
        }
        element.setPrefSize(width, height);

        if(force){
            element.setStyle("-fx-min-width: " + width + ";");
            element.setStyle("-fx-min-height: " + height + ";");
            element.minWidthProperty().bind(new SimpleDoubleProperty(width));
            element.minHeightProperty().bind(new SimpleDoubleProperty(height));
        }
    }

    public static void setHBoxPosition(Region element, double width, double height, Insets margin){

        if(width == -1){
            HBox.setHgrow(element, Priority.ALWAYS);
            element.setMaxWidth(Double.MAX_VALUE);
        }else if(width != 0){
            element.setPrefWidth(width);
            element.minWidthProperty().bind(new SimpleDoubleProperty(width));
        }
        if(height == -1){
            VBox.setVgrow(element, Priority.ALWAYS);
        }else if(height != 0){
            element.setPrefHeight(height);
            element.minHeightProperty().bind(new SimpleDoubleProperty(height));
        }
        HBox.setMargin(element, margin);
    }
    public static void setHBoxPosition(Region element, double width, double height, double margin){
        setHBoxPosition(element, width, height, new Insets(margin, margin, margin, margin));
    }
    public static void setHBoxPosition(Region element, double width, double height, double marginLeftRight, double marginTopBottom){
        setHBoxPosition(element, width, height, new Insets(marginTopBottom, marginLeftRight, marginTopBottom, marginLeftRight));
    }

    public static void setVBoxPosition(Region element, double width, double height, Insets margin){

        if(width == -1){
            HBox.setHgrow(element, Priority.ALWAYS);
            element.setMaxWidth(Double.MAX_VALUE);
        }else if(width != 0){
            element.setPrefWidth(width);
            element.minWidthProperty().bind(new SimpleDoubleProperty(width));
        }
        if(height == -1){
            VBox.setVgrow(element, Priority.ALWAYS);
        }else if(height != 0){
            element.setPrefHeight(height);
            element.minHeightProperty().bind(new SimpleDoubleProperty(height));
        }
        VBox.setMargin(element, margin);
    }
    public static void setVBoxPosition(Region element, double width, double height, double margin){
        setVBoxPosition(element, width, height, new Insets(margin, margin, margin, margin));
    }
    public static void setVBoxPosition(Region element, double width, double height, double marginLeftRight, double marginTopBottom){
        setVBoxPosition(element, width, height, new Insets(marginTopBottom, marginLeftRight, marginTopBottom, marginLeftRight));
    }

    public static void setMenuSize(Menu menu){

        for(MenuItem subMenu : menu.getItems()){
            subMenu.setStyle("-fx-font-size: 13;");
            if(subMenu instanceof Menu){
                setMenuSize((Menu) subMenu);
            }
        }
    }

    public static Alert getAlert(Alert.AlertType type, String title){
        Alert alert = new Alert(type);
        alert.setTitle(title);

        alert.initOwner(Main.window.getScene().getWindow());

        setupDialog(alert);
        return alert;
    }
    public static boolean showErrorAlert(String headerText, String error, boolean continueAsk){
        Alert alert = Builders.getAlert(Alert.AlertType.ERROR, TR.tr("Une erreur est survenue"));
        alert.setHeaderText(headerText);
        alert.setContentText(TR.tr("Ctrl+Alt+C pour accéder aux logs"));

        TextArea textArea = new TextArea(error);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label(TR.tr("L'erreur survenue est la suivante :")), 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);

        if(continueAsk){
            ButtonType stopAll = new ButtonType(TR.tr("Arreter tout"), ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType continueRender = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.NEXT_FORWARD);
            alert.getButtonTypes().setAll(stopAll, continueRender);

            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == stopAll) return true;
        }else{
            alert.show();
        }
        return false;
    }
    public static File[] showFilesDialog(boolean syncWithLastOpenDir, boolean multiple, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        chooser.setTitle(TR.tr("Sélectionner un ou plusieurs fichier"));
        chooser.setInitialDirectory((syncWithLastOpenDir && UserData.lastOpenDir.exists()) ? UserData.lastOpenDir : new File(System.getProperty("user.home")));
        List<File> listFiles;
        if(multiple) listFiles = chooser.showOpenMultipleDialog(Main.window);
        else listFiles = Collections.singletonList(chooser.showOpenDialog(Main.window));

        if(listFiles != null){
            if(listFiles.size() == 0) return null;
            File[] files = new File[listFiles.size()];
            files = listFiles.toArray(files);

            if(syncWithLastOpenDir) UserData.lastOpenDir = files[0].getParentFile();
            return files;
        }
        return null;
    }
    public static File showDirectoryDialog(boolean syncWithLastOpenDir){
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(TR.tr("Sélectionner un dossier"));
        chooser.setInitialDirectory((syncWithLastOpenDir && UserData.lastOpenDir.exists()) ? UserData.lastOpenDir : new File(System.getProperty("user.home")));

        File file = chooser.showDialog(Main.window);
        if(file != null){
            if(!file.exists()) return null;
            if(syncWithLastOpenDir) UserData.lastOpenDir = file.getParentFile();
            return file;
        }
        return null;
    }
    public static void setupDialog(Dialog dialog){

        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Builders.class.getResource("/logo.png")+""));
        StyleManager.putStyle(dialog.getDialogPane(), Style.DEFAULT);

        dialog.setOnShowing(e -> new Thread(() -> {

            try{
                Thread.sleep(400);
            }catch(InterruptedException ex){ ex.printStackTrace();  }

            Platform.runLater(() -> {
                if(dialog.isShowing()){
                    if(dialog.getDialogPane().getScene().getWindow().getWidth() < 100){
                        dialog.getDialogPane().getScene().getWindow().setWidth(500);
                        dialog.getDialogPane().getScene().getWindow().setHeight(200);
                    }
                }
            });

        }, "AlertResizer").start());
    }

    public static String[] cleanArray(String[] array) {
        return Arrays.stream(array).filter(x -> !x.isBlank()).toArray(String[]::new);
    }

    public static Tooltip genToolTip(String text){
        return new Tooltip(new TextWrapper(text, null, 350).wrap());

    }


}
