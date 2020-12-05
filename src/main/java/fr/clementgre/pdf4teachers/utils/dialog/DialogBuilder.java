package fr.clementgre.pdf4teachers.utils.dialog;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
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

    public static boolean showWrongAlert(String headerText,String contentText, boolean continueAsk){
        Alert alert = getAlert(Alert.AlertType.ERROR, TR.tr("Une erreur est survenue"));
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        if(continueAsk){
            ButtonType stopAll = new ButtonType(TR.tr("Arreter tout"), ButtonBar.ButtonData.NO);
            ButtonType continueRender = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.YES);
            alert.getButtonTypes().setAll(stopAll, continueRender);

            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == stopAll) return true;
        }else{
            alert.show();
        }
        return false;
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
            ButtonType stopAll = new ButtonType(TR.tr("Arreter tout"), ButtonBar.ButtonData.NO);
            ButtonType continueRender = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.YES);
            alert.getButtonTypes().setAll(stopAll, continueRender);

            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == stopAll) return true;
        }else{
            alert.show();
        }
        return false;
    }
    public static File showFileDialog(boolean syncWithLastOpenDir){
        File[] files = showFilesDialog(syncWithLastOpenDir, false, TR.tr("Fichier PDF"), "*.pdf");
        return files == null ? null : files[0];
    }
    public static File showFileDialog(boolean syncWithLastOpenDir, String extensionsName, String... extensions){
        File[] files = showFilesDialog(syncWithLastOpenDir, false, extensionsName, extensions);
        return files == null ? null : files[0];
    }
    public static File[] showFilesDialog(boolean syncWithLastOpenDir){
        return showFilesDialog(syncWithLastOpenDir, true, TR.tr("Fichier PDF"), "*.pdf");
    }
    public static File[] showFilesDialog(boolean syncWithLastOpenDir, String extensionsName, String... extensions){
        return showFilesDialog(syncWithLastOpenDir, true, extensionsName, extensions);
    }
    public static File[] showFilesDialog(boolean syncWithLastOpenDir, boolean multiple, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        if(multiple) chooser.setTitle(TR.tr("Sélectionner un ou plusieurs fichier"));
        else chooser.setTitle(TR.tr("Sélectionner un fichier"));
        chooser.setInitialDirectory((syncWithLastOpenDir && new File(MainWindow.userData.lastOpenDir).exists()) ?  new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home")));

        List<File> listFiles = null;
        if(multiple) listFiles = chooser.showOpenMultipleDialog(Main.window);
        else{
            File file = chooser.showOpenDialog(Main.window);
            if(file != null) listFiles = Collections.singletonList(file);
        }

        if(listFiles != null){
            if(listFiles.size() == 0) return null;
            File[] files = new File[listFiles.size()];
            files = listFiles.toArray(files);
            if(syncWithLastOpenDir)  MainWindow.userData.lastOpenDir = listFiles.get(0).getParentFile().getAbsolutePath();
            return files;
        }
        return null;
    }

    public static File showDirectoryDialog(boolean syncWithLastOpenDir){
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(TR.tr("Sélectionner un dossier"));
        chooser.setInitialDirectory((syncWithLastOpenDir &&  new File(MainWindow.userData.lastOpenDir).exists()) ?  new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home")));

        File file = chooser.showDialog(Main.window);
        if(file != null){
            if(!file.exists()) return null;
            if(syncWithLastOpenDir) MainWindow.userData.lastOpenDir = file.getAbsolutePath();
            return file;
        }
        return null;
    }
    public static File showSaveDialog(boolean syncWithLastOpenDir, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        chooser.setTitle(TR.tr("Enregistrer un fichier"));
        chooser.setInitialDirectory((syncWithLastOpenDir &&  new File(MainWindow.userData.lastOpenDir).exists()) ?  new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home")));

        File file = chooser.showSaveDialog(Main.window);
        if(file != null){
            if(syncWithLastOpenDir)  MainWindow.userData.lastOpenDir = file.getParent();
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
