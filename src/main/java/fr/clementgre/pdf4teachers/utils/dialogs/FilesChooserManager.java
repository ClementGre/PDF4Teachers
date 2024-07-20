/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilesChooserManager{

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
        chooser.setInitialDirectory(pathToExistingFile(getPathFromSyncVar(syncVar)));

        List<File> listFiles = null;
        if(multiple) listFiles = chooser.showOpenMultipleDialog(Main.window);
        else{
            File file = chooser.showOpenDialog(Main.window);
            if(file != null) listFiles = Collections.singletonList(file);
        }

        if(listFiles != null){
            if(listFiles.isEmpty()) return null;
            File[] files = new File[listFiles.size()];
            files = listFiles.toArray(files);
            setPathFromSyncVar(syncVar, listFiles.getFirst().getParentFile().getAbsolutePath());

            return files;
        }
        return null;
    }

    /***************************************************************************
     ********************** CHOOSE DIRECTORY DIALOG ****************************
     ***************************************************************************/
    public static File showDirectoryDialog(SyncVar syncVar){
        return showDirectoryDialog(null, syncVar, null, Main.window);
    }
    public static File showDirectoryDialog(SyncVar syncVar, Stage window){
        return showDirectoryDialog(null, syncVar, null, window);
    }
    public static File showDirectoryDialog(String preferred, SyncVar syncVar, String fallback){
        return showDirectoryDialog(preferred, syncVar, fallback, Main.window);
    }
    public static File showDirectoryDialog(String preferred, SyncVar syncVar, String fallback, Stage window){

        File file = showDirectoryDialog(pathToExistingPath(preferred, getPathFromSyncVar(syncVar), fallback), window);
        if(file != null && file.exists()){
            setPathFromSyncVar(syncVar, file.getAbsolutePath());
            return file;
        }
        return null;
    }
    public static File showDirectoryDialog(String... paths){
        return showDirectoryDialog(pathToExistingPath(paths), Main.window);
    }
    public static File showDirectoryDialog(String path, Stage window){
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(TR.tr("dialog.file.selectFolder.title"));
        chooser.setInitialDirectory(new File(path));

        return chooser.showDialog(window);
    }

    /***************************************************************************
     ************************** SAVE FILE DIALOG *******************************
     ***************************************************************************/

    public static File showSaveDialog(SyncVar syncVar, String initialFileName, String extensionsName, String... extensions){

        File file = showSaveDialog(pathToExistingPath(getPathFromSyncVar(syncVar)), initialFileName, extensionsName, extensions);
        if(file != null){
            setPathFromSyncVar(syncVar, file.getParent());
            return file;
        }
        return null;
    }

    private static File showSaveDialog(String path, String initialFileName, String extensionsName, String... extensions){
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionsName, extensions));
        chooser.setTitle(TR.tr("dialog.file.saveFile.title"));
        chooser.setInitialFileName(initialFileName);
        chooser.setInitialDirectory(new File(path));
        File file = chooser.showSaveDialog(Main.window);

        // On MacOS, the os save dialog removes the extension -> we add it back
        File finalFile = file;
        if(Arrays.stream(extensions).noneMatch(ext -> finalFile.getName().endsWith(ext))){
            file = new File(file.getAbsolutePath() + chooser.getSelectedExtensionFilter().getExtensions().getFirst());
        }
        return file;
    }

    /***************************************************************************
     ************************** SyncVar MANAGEMENT *****************************
     ***************************************************************************/

    public enum SyncVar{
        LAST_OPEN_DIR,
        LAST_CONVERT_SRC_DIR,
        LAST_GALLERY_OPEN_DIR
    }
    public static String getPathFromSyncVar(SyncVar syncVar){
        if(syncVar == SyncVar.LAST_OPEN_DIR) return MainWindow.userData.lastOpenDir;
        if(syncVar == SyncVar.LAST_CONVERT_SRC_DIR) return MainWindow.userData.lastConvertSrcDir;
        if(syncVar == SyncVar.LAST_GALLERY_OPEN_DIR) return MainWindow.userData.galleryLastOpenPath;
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
    public static File pathToExistingFile(String... paths){
        return new File(pathToExistingPath(paths));
    }

    public static String pathToExistingPath(String preferredValue, SyncVar syncVar){
        return pathToExistingPath(preferredValue, getPathFromSyncVar(syncVar), null);
    }
    public static String pathToExistingPath(String preferredValue, SyncVar syncVar, String fallback){
        return pathToExistingPath(preferredValue, getPathFromSyncVar(syncVar), fallback);
    }
    public static String pathToExistingPath(String... paths){
        for(String path : paths){
            if(path != null && new File(path).exists()) return path;
        }
        return System.getProperty("user.home");
    }
}
