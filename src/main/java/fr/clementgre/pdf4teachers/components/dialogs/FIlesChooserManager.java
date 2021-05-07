package fr.clementgre.pdf4teachers.components.dialogs;

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
    
    /***************************************************************************
    ************************ OPEN FILE(s) DIALOG *******************************
    ***************************************************************************/
    
    // One file
    public static File showPDFFileDialog(SyncVar syncVar){
        File[] files = showFilesDialog(syncVar, false, TR.tr("dialog.file.extensionType.PDF"), "*.pdf");
        return files == null ? null : files[0];
    }
    public static File showFileDialog(SyncVar syncVar, String extensionsName, String... extensions){
        File[] files = showFilesDialog(syncVar, false, extensionsName, extensions);
        return files == null ? null : files[0];
    }
    
    // Multiple
    public static File[] showPDFFilesDialog(SyncVar syncVar){
        return showFilesDialog(syncVar, true, TR.tr("dialog.file.extensionType.PDF"), "*.pdf");
    }
    public static File[] showFilesDialog(SyncVar syncVar, String extensionsName, String... extensions){
        return showFilesDialog(syncVar, true, extensionsName, extensions);
    }
    
    // Main
    public static File[] showFilesDialog(SyncVar syncVar, boolean multiple, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        if(multiple) chooser.setTitle(TR.tr("dialog.file.selectFiles.title"));
        else chooser.setTitle(TR.tr(""));
        chooser.setInitialDirectory(pathToExistingFile(getPathFromSyncVar(syncVar), syncVar.getFallback()));
        
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
            setPathFromSyncVar(syncVar, listFiles.get(0).getParentFile().getAbsolutePath());
            
            return files;
        }
        return null;
    }
    
    /***************************************************************************
     ********************** CHOOSE DIRECTORY DIALOG ****************************
     ***************************************************************************/
    
    public static File showDirectoryDialog(SyncVar syncVar){
        return showDirectoryDialog(null, syncVar, Main.window);
    }
    public static File showDirectoryDialog(String preferredValue, SyncVar syncVar){
        return showDirectoryDialog(preferredValue, syncVar, Main.window);
    }
    public static File showDirectoryDialog(String preferredValue, SyncVar syncVar, Stage window){
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(TR.tr("dialog.file.selectFolder.title"));
        chooser.setInitialDirectory(pathToExistingFile(preferredValue, getPathFromSyncVar(syncVar), syncVar.getFallback()));
        
        File file = chooser.showDialog(window);
        if(file != null){
            if(!file.exists()) return null;
            setPathFromSyncVar(syncVar, file.getAbsolutePath());
            return file;
        }
        return null;
    }
    
    /***************************************************************************
     ************************** SAVE FILE DIALOG *******************************
     ***************************************************************************/

    public static File showSaveDialog(SyncVar syncVar, String initialFileName, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        chooser.setTitle(TR.tr("dialog.file.saveFile.title"));
        chooser.setInitialFileName(initialFileName);
        chooser.setInitialDirectory(pathToExistingFile(getPathFromSyncVar(syncVar), syncVar.getFallback()));
    
        File file = chooser.showSaveDialog(Main.window);
        if(file != null){
            setPathFromSyncVar(syncVar, file.getParentFile().getAbsolutePath());
            return file;
        }
        return null;
    }
    
    /***************************************************************************
     ************************** SyncVar MANAGEMENT *****************************
     ***************************************************************************/

    public enum SyncVar{
        LAST_OPEN_DIR(System.getProperty("user.home")),
        LAST_CONVERT_SRC_DIR(System.getProperty("user.home")),
        LAST_GALLERY_OPEN_DIR(System.getProperty("user.home")),
        NO_SYNC(System.getProperty("user.home"));
    
        private String fallback;
        SyncVar(String fallback){
            this.fallback = fallback;
        }
        public String getFallback(){
            return fallback;
        }
    }
    
    public static String getPathFromSyncVar(SyncVar syncVar){
        if(syncVar == SyncVar.LAST_OPEN_DIR) return MainWindow.userData.lastOpenDir;
        else if(syncVar == SyncVar.LAST_CONVERT_SRC_DIR) return MainWindow.userData.lastConvertSrcDir;
        else if(syncVar == SyncVar.LAST_GALLERY_OPEN_DIR) return MainWindow.userData.galleryLastOpenPath;
        else if(syncVar == SyncVar.NO_SYNC) return syncVar.fallback;
        return null;
    }
    public static void setPathFromSyncVar(SyncVar syncVar, String value){
        if(syncVar == SyncVar.LAST_OPEN_DIR) MainWindow.userData.lastOpenDir = value;
        else if(syncVar == SyncVar.LAST_CONVERT_SRC_DIR) MainWindow.userData.lastConvertSrcDir = value;
        else if(syncVar == SyncVar.LAST_GALLERY_OPEN_DIR) MainWindow.userData.galleryLastOpenPath = value;
    }
    
    
    public static File pathToExistingFile(String preferredValue, SyncVar syncVar){
        return new File(pathToExistingPath(preferredValue, getPathFromSyncVar(syncVar), null));
    }
    public static File pathToExistingFile(String preferredValue, SyncVar syncVar, String fallback){
        return new File(pathToExistingPath(preferredValue, getPathFromSyncVar(syncVar), fallback));
    }
    public static File pathToExistingFile(String preferredValue, String path){
        return new File(pathToExistingPath(preferredValue, path, null));
    }
    public static File pathToExistingFile(String preferredValue, String path, String fallback){
        return new File(pathToExistingPath(preferredValue, path, fallback));
    }
    
    public static String pathToExistingPath(String preferredValue, SyncVar syncVar){
        return pathToExistingPath(preferredValue, getPathFromSyncVar(syncVar), null);
    }
    public static String pathToExistingPath(String preferredValue, SyncVar syncVar, String fallback){
        return pathToExistingPath(preferredValue, getPathFromSyncVar(syncVar), fallback);
    }
    public static String pathToExistingPath(String preferredValue, String path){
        return pathToExistingPath(preferredValue, path, null);
    }
    public static String pathToExistingPath(String preferredValue, String path, String fallback){
        if(preferredValue != null && new File(preferredValue).exists()) return preferredValue;
        else if(path != null && new File(path).exists()) return path;
        else if(fallback != null && new File(fallback).exists()) return fallback;
        else return System.getProperty("user.home");
    }
}
