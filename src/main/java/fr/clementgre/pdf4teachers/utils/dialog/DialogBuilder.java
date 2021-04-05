package fr.clementgre.pdf4teachers.utils.dialog;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
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

public class DialogBuilder{
    
    
    public static Alert getAlert(Alert.AlertType type, String title){
        return getAlert(type, title, null, null);
    }
    
    public static Alert getAlert(Alert.AlertType type, String title, String header){
        return getAlert(type, title, header, null);
    }
    
    public static Alert getAlertBooth(Alert.AlertType type, String titleHeader){
        return getAlert(type, titleHeader, titleHeader, null);
    }
    
    public static Alert getAlert(Alert.AlertType type, String title, String header, String content){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        
        
        if(Main.window != null){
            if(Main.window.getScene() != null) alert.initOwner(Main.window);
            if(MainWindow.paintTab.galleryWindow != null){
                if(MainWindow.paintTab.galleryWindow.isFocused()) alert.initOwner(MainWindow.paintTab.galleryWindow);
            }
        }
        
        if(header != null){
            alert.setHeaderText(header);
        }
        if(content != null){
            alert.setContentText(content);
        }
        
        setupDialog(alert);
        return alert;
    }
    
    public static boolean showConfirmationDialog(boolean irreversible, String header){
        Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.confirmation.title"), header);
        if(irreversible) alert.setContentText(TR.tr("dialog.confirmation.irreversible"));
    
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty()) return false;
        return result.get() == ButtonType.OK;
    }
    
    public static void showAlertWithOpenDirButton(String title, String header, String details, File dirToBrowse){
        showAlertWithOpenDirButton(title, header, details, dirToBrowse.getAbsolutePath());
    }
    
    public static void showAlertWithOpenDirButton(String title, String header, String details, String pathToBrowse){
        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, title, header, details);
        
        ButtonType open = new ButtonType(TR.tr("dialog.file.openFolderButton"), ButtonBar.ButtonData.YES);
        alert.getButtonTypes().add(open);
        
        Optional<ButtonType> optionSelected = alert.showAndWait();
        if(optionSelected.get() == open){
            PlatformUtils.openDirectory(pathToBrowse);
        }
    }
    
    public static <T> ChoiceDialog<T> getChoiceDialog(T selected, List<T> values){
        ChoiceDialog<T> alert = new ChoiceDialog<T>(selected, values);
        
        if(Main.window != null){
            if(Main.window.getScene() != null) alert.initOwner(Main.window);
        }
        
        setupDialog(alert);
        return alert;
    }
    
    public static boolean showWrongAlert(String headerText, String contentText, boolean continueAsk){
        Alert alert = getAlert(Alert.AlertType.ERROR, TR.tr("dialog.error.title"));
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        
        if(continueAsk){
            ButtonType stopAll = new ButtonType(TR.tr("dialog.actionError.cancelAll"), ButtonBar.ButtonData.NO);
            ButtonType continueRender = new ButtonType(TR.tr("dialog.actionError.continue"), ButtonBar.ButtonData.YES);
            alert.getButtonTypes().setAll(stopAll, continueRender);
            
            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == stopAll) return true;
        }else{
            alert.show();
        }
        return false;
    }
    
    public static String unableToCopyFileHeader(String toCopyPath, String destPath, boolean simplify){
        if(simplify){
            toCopyPath = FilesUtils.getPathReplacingUserHome(toCopyPath);
            destPath = FilesUtils.getPathReplacingUserHome(destPath);
        }
        return TR.tr("dialog.copyFileError.title", toCopyPath, destPath);
    }
    
    public static boolean showErrorAlert(String headerText, String error, boolean continueAsk){
        Alert alert = getAlert(Alert.AlertType.ERROR, TR.tr("dialog.error.title"));
        alert.setHeaderText(headerText == null ? TR.tr("dialog.error.title") : headerText);
        alert.setContentText(TR.tr("dialog.error.details"));
        
        TextArea textArea = new TextArea(error);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label(TR.tr("convertWindow.dialog.error.details")), 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        
        if(continueAsk){
            ButtonType stopAll = new ButtonType(TR.tr("dialog.actionError.cancelAll"), ButtonBar.ButtonData.NO);
            ButtonType continueRender = new ButtonType(TR.tr("dialog.actionError.continue"), ButtonBar.ButtonData.YES);
            alert.getButtonTypes().setAll(stopAll, continueRender);
            
            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == stopAll) return true;
        }else{
            alert.show();
        }
        return false;
    }
    
    public static File showPDFFileDialog(boolean syncWithLastOpenDir){
        File[] files = showFilesDialog(syncWithLastOpenDir, false, TR.tr("dialog.file.extensionType.PDF"), "*.pdf");
        return files == null ? null : files[0];
    }
    
    public static File showFileDialog(boolean syncWithLastOpenDir, String extensionsName, String... extensions){
        File[] files = showFilesDialog(syncWithLastOpenDir, false, extensionsName, extensions);
        return files == null ? null : files[0];
    }
    
    public static File[] showPDFFilesDialog(boolean syncWithLastOpenDir){
        return showFilesDialog(syncWithLastOpenDir, true, TR.tr("dialog.file.extensionType.PDF"), "*.pdf");
    }
    
    public static File[] showFilesDialog(boolean syncWithLastOpenDir, String extensionsName, String... extensions){
        return showFilesDialog(syncWithLastOpenDir, true, extensionsName, extensions);
    }
    
    public static File[] showFilesDialog(boolean syncWithLastOpenDir, boolean multiple, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        if(multiple) chooser.setTitle(TR.tr("dialog.file.selectFiles.title"));
        else chooser.setTitle(TR.tr(""));
        chooser.setInitialDirectory((syncWithLastOpenDir && new File(MainWindow.userData.lastOpenDir).exists()) ? new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home")));
        
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
            if(syncWithLastOpenDir)
                MainWindow.userData.lastOpenDir = listFiles.get(0).getParentFile().getAbsolutePath();
            return files;
        }
        return null;
    }
    
    public static File showDirectoryDialog(boolean syncWithLastOpenDir){
        return showDirectoryDialog(syncWithLastOpenDir, Main.window);
    }
    public static File showDirectoryDialog(boolean syncWithLastOpenDir, Stage window){
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(TR.tr("dialog.file.selectFolder.title"));
        chooser.setInitialDirectory((syncWithLastOpenDir && new File(MainWindow.userData.lastOpenDir).exists()) ? new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home")));
        
        File file = chooser.showDialog(window);
        if(file != null){
            if(!file.exists()) return null;
            if(syncWithLastOpenDir) MainWindow.userData.lastOpenDir = file.getAbsolutePath();
            return file;
        }
        return null;
    }
    
    public static File showSaveDialog(boolean syncWithLastOpenDir, String initialFileName, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        chooser.setTitle(TR.tr("dialog.file.saveFile.title"));
        chooser.setInitialFileName(initialFileName);
        chooser.setInitialDirectory((syncWithLastOpenDir && new File(MainWindow.userData.lastOpenDir).exists()) ? new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home")));
        
        File file = chooser.showSaveDialog(Main.window);
        if(file != null){
            if(syncWithLastOpenDir) MainWindow.userData.lastOpenDir = file.getParent();
            return file;
        }
        return null;
    }
    
    public static void setupDialog(Dialog<?> dialog){
        
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PaneUtils.class.getResource("/logo.png") + ""));
        StyleManager.putStyle(dialog.getDialogPane(), Style.DEFAULT);
        
        dialog.setOnShowing(e -> new Thread(() -> {
            
            try{
                Thread.sleep(400);
            }catch(InterruptedException ex){
                ex.printStackTrace();
            }
            
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
