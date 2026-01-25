/*
 * Copyright (c) 2021-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Originally created by azakhary on 4/24/2015
 * see <a href="https://github.com/UnderwaterApps/overlap2d/blob/master/overlap2d/src/com/uwsoft/editor/proxy/FontManager.java">...</a>
 * (Apache 2.0 license)
 * Edited by Clement Grennerat
 */
public class SystemFontsMapper {

    private static final String[] FONT_EXTENSIONS = {"ttf", "otf", "ttc"};
    private static final double MILLIS_PER_SECOND = 1000d;
    private static final int NOTIFICATION_TIMEOUT_SECONDS = 30;

    // Family, Paths
    private final HashMap<String, FontPaths> systemFontMap = new HashMap<>();
    
    public static String[] getSystemFontNames(){
        try{
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        }catch(UnsatisfiedLinkError e){
            Log.eNotified(e);
            return new String[0];
        }
    }

    public List<File> getAllSystemFontFiles(){
        if(PlatformUtils.isWindows()){
            return scanDirectoriesForFonts(List.of(
                    System.getenv("WINDIR") + File.separator + "Fonts",
                    System.getenv("LOCALAPPDATA") + File.separator + "Microsoft\\Windows\\Fonts"
            ));
        }
        if(PlatformUtils.isMac()){
            return scanDirectoriesForFonts(List.of(
                    System.getProperty("user.home") + File.separator + "Library/Fonts",
                    "/Library/Fonts",
                    "/System/Library/Fonts"
            ));
        }
        if(PlatformUtils.isLinux()){
            // Use fc-list to get font files directly (works on NixOS and other non-standard setups)
            List<File> fcListFiles = getLinuxFontFilesViaFcList();
            if(!fcListFiles.isEmpty()) return fcListFiles;

            // Fallback to directory scanning if fc-list fails
            return scanDirectoriesForFonts(getLinuxFontDirs());
        }
        return List.of();
    }

    public void loadFontsFromSystemFiles(){
        String[] systemFonts = getSystemFontNames();
        Log.i("Indexing system fonts... (" + systemFonts.length + " fonts)");

        new Thread(() -> {
            long time = System.currentTimeMillis();

            systemFontMap.clear();
            int pdfBoxErrors = 0;
            for(File file : getAllSystemFontFiles()){
                try{
                    Font[] fonts = Font.loadFonts(new FileInputStream(file.getAbsolutePath()), -1);
                    if(fonts == null) continue;
                    if (Arrays.stream(fonts)
                            .filter(font -> StringUtils.containsIgnoreCase(systemFonts, font.getFamily()))
                            .anyMatch(font -> !addFontToMap(font, file.getAbsolutePath()))) {
                        pdfBoxErrors++;
                    }
                }catch(IOException e){Log.eNotified(e);}
            }

            double ping = (System.currentTimeMillis() - time) / MILLIS_PER_SECOND;
            Log.i("Loaded " + systemFontMap.size() + '/' + systemFonts.length + " fonts in " + ping + "s (" + pdfBoxErrors + " unable to load due to PDFBox restrictions, usually missing tables)");

            Platform.runLater(FontUtils::fontsUpdated);
        }, "System fonts loader").start();
    }

    public void loadFontsFromCache(List<FontPaths> fontPathss){
        for(FontPaths fontPaths : fontPathss){
            systemFontMap.put(fontPaths.getName(), fontPaths);
        }
        Platform.runLater(FontUtils::fontsUpdated);
    }

    public FontPaths getFontPathsFromName(String fontFamily){
        return systemFontMap.get(fontFamily);
    }

    public HashMap<String, FontPaths> getFonts(){
        return systemFontMap;
    }

    public boolean hasFont(String fontFamily){
        return systemFontMap.containsKey(fontFamily);
    }
    
    private List<File> scanDirectoriesForFonts(List<String> directories){
        return directories.stream()
                .map(File::new)
                .filter(File::exists)
                .flatMap(fontDirectory -> FilesUtils.listFiles(fontDirectory, FONT_EXTENSIONS, true).stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<File> getLinuxFontFilesViaFcList(){
        try{
            ProcessBuilder pb = new ProcessBuilder("fc-list", "--format=%{file}\n");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            process.waitFor();

            return Arrays.stream(output.split("\n"))
                    .filter(line -> !line.isEmpty())
                    .map(File::new)
                    .filter(file -> file.exists() && file.canRead())
                    .toList();
        }catch(IOException | InterruptedException e){
            Log.eNotified(e);
            return List.of();
        }
    }

    private List<String> getLinuxFontDirs(){
        String userHome = System.getProperty("user.home");
        Stream<String> standardPaths = Stream.of(
                userHome + "/.local/share/fonts",
                userHome + "/.fonts",
                "/usr/share/fonts/truetype",
                "/usr/share/fonts/TTF",
                "/usr/local/share/fonts/"
        );

        // Add font directories from XDG_DATA_DIRS (for NixOS and other non-standard setups)
        String xdgDataDirs = System.getenv("XDG_DATA_DIRS");
        Stream<String> xdgPaths = (xdgDataDirs != null && !xdgDataDirs.isEmpty())
                ? Arrays.stream(xdgDataDirs.split(":"))
                    .filter(dir -> !dir.isEmpty())
                    .map(dir -> dir + "/fonts")
                : Stream.empty();

        List<String> result = Stream.concat(xdgPaths, standardPaths)
                .map(File::new)
                .filter(file -> file.exists() && file.isDirectory() && file.canRead())
                .map(File::getAbsolutePath)
                .toList();

        if(result.isEmpty()){
            Platform.runLater(() ->
                MainWindow.showNotification(AlertIconType.WARNING,
                    "Error: unable to load system fonts, no directory is readable", NOTIFICATION_TIMEOUT_SECONDS));
        }
        return result;
    }

    private boolean addFontToMap(Font font, String path){
        // Check if PDFBox is able to load the font, otherwise, cancel the add.
        try(PDDocument doc = new PDDocument();
            FileInputStream fis = new FileInputStream(path)){
            PDType0Font.load(doc, fis);
        }catch(IOException e){
            return false;
        }

        FontPaths paths;
        if(systemFontMap.containsKey(font.getFamily())){
            paths = systemFontMap.get(font.getFamily());
        }else{
            paths = new FontPaths(font.getFamily());
        }
        paths.addPathAuto(path, FontUtils.getFontWeight(font, false), FontUtils.getFontPosture(font).equals(FontPosture.ITALIC));
        systemFontMap.put(font.getFamily(), paths);

        return true;
    }
}