package fr.themsou.utils;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.interfaces.windows.language.TR;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DialogBuilder {


    public static Alert getAlert(Alert.AlertType type, String title){
        Alert alert = new Alert(type);
        alert.setTitle(title);

        alert.initOwner(Main.window.getScene().getWindow());

        setupDialog(alert);
        return alert;
    }

    public static boolean showErrorAlert(String headerText, String error, boolean continueAsk){
        Alert alert = getAlert(Alert.AlertType.ERROR, TR.tr("Une erreur est survenue"));
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

        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PaneUtils.class.getResource("/logo.png")+""));
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
}
