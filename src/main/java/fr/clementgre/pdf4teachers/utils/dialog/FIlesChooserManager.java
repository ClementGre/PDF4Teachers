package fr.clementgre.pdf4teachers.utils.dialog;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class FIlesChooserManager{
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
}
