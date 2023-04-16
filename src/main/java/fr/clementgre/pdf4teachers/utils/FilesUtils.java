/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class FilesUtils {

    public static File HOME_DIR = new File(System.getProperty("user.home"));

    public static long getSize(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                return Files.size(path);
            }

            try (Stream<Path> paths = Files.walk(path)) {
                return paths
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .sum();
            }
        } catch (IOException e) {
            return 0;
        }
    }

    public static float convertOctetToMo(long octet) {

        return (float) (octet / 1000) / 1000f;

    }

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }
    
    public static String getNameWithoutExtension(File file){
        return StringUtils.removeAfterLastOccurrence(file.getName(), "." + FilesUtils.getExtension(file));
    }
    
    // Always return lower case extension without the dot.
    public static String getExtension(String fileName) {
        String[] splitted = fileName.split(Pattern.quote("."));
        if(splitted.length == 0 || splitted.length == 1) return "";
        return splitted[splitted.length - 1].toLowerCase();
    }

    public static boolean isInSameDir(File file1, File file2) {
        return file1.getParentFile().getAbsolutePath().equals(file2.getParentFile().getAbsolutePath());
    }

    public static String getPathReplacingUserHome(File file) {
        return getPathReplacingUserHome(file.getAbsolutePath());
    }

    public static String getPathReplacingUserHome(String path) {
        if (path.startsWith(System.getProperty("user.home"))) {
            return path.replaceFirst(Pattern.quote(System.getProperty("user.home")), "~");
        } else return path;
    }

    public static List<File> listFiles(File dir, String[] extensions, boolean recursive) {
        File[] allFiles = dir.listFiles();
        if (allFiles == null) return Collections.emptyList();

        ArrayList<File> files = new ArrayList<>();
        for (File file : allFiles) {
            if (file.isDirectory()) {
                if (recursive) {
                    files.addAll(listFiles(file, extensions, true));
                }
            } else {
                if (!file.isHidden() && StringUtils.contains(extensions, getExtension(file.getName()))) {
                    files.add(file);
                }
            }

        }
        return files;
    }
    
    public static void copyFileUsingStream(File source, File dest) throws IOException{
        
        try(InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)){
            byte[] buffer = new byte[1024];
            int length;
            while((length = is.read(buffer)) > 0){
                os.write(buffer, 0, length);
            }
        }
    }
    
    public static void moveDir(File source, File output){
        if(!output.mkdirs()) throw new RuntimeException("Unable to create dir " + output.getAbsolutePath());
        
        for(File file : Objects.requireNonNull(source.listFiles())){
            File destFile = new File(output.getAbsolutePath() + "/" + file.getName());
            if(file.isDirectory()){
                moveDir(file, destFile);
            }else{
                try{
                    copyFileUsingStream(file, destFile);
                    file.delete();
                }catch(IOException e){
                    Log.eNotified(e);
                }
            }
        }
        source.delete();
    }
    
    // Moves from ~/.PDF4Teachers/ to Main.dataFolder
    public static void moveDataFolder(String newDataFolderPath){
        File oldDataFolder = new File(System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator);
        Log.i("Moving data folder from " + oldDataFolder.getAbsolutePath() + " to " + newDataFolderPath);
        
        if(oldDataFolder.getAbsolutePath().equals(newDataFolderPath)) return;
        
        FilesUtils.moveDir(oldDataFolder, new File(newDataFolderPath));
        
        PlatformUtils.runLaterOnUIThread(5000, () -> {
            MainWindow.showNotification(AlertIconType.INFORMATION, TR.tr("moveDataFolderNotification", FilesUtils.getPathReplacingUserHome(newDataFolderPath)), 20);
        });
    }
    
   
}
