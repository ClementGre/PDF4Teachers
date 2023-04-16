/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class FilesUtils {

    public static long getSize(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                return Files.size(path);
            }

            try (var paths = Files.walk(path)) {
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

    public static float convertBytesToMegaBytes(long bytes) {

        return (float) (bytes / 1000) / 1000f;

    }

    public static String getExtension(Path path) {
        return getExtension(path.getFileName().toString());
    }

    public static String getNameWithoutExtension(Path path) {
        var fileName = path.getFileName().toString();
        int lastIndexOfDot = fileName.lastIndexOf('.');

        if (lastIndexOfDot == -1) {
            return fileName;
        }

        return fileName.substring(0, lastIndexOfDot);
    }

    // Always return lower case extension without the dot.
    public static String getExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) return "";
        return fileName.substring(lastIndexOfDot + 1).toLowerCase();
    }

    public static boolean isInSameDir(Path firstPath, Path secondPath) {
        return firstPath.getParent().equals(secondPath.getParent());
    }

    public static String getPathReplacingUserHome(Path path) {
        return getPathReplacingUserHome(path.toString());
    }

    public static String getPathReplacingUserHome(String pathString) {
        var userHome = System.getProperty("user.home");
        return pathString.startsWith(userHome) ? pathString.replaceFirst(Pattern.quote(userHome), "~") : pathString;
    }

    public static List<File> listFiles(File dir, String[] extensions, boolean recursive) {
        try (var stream = Files.walk(dir.toPath(), recursive ? Integer.MAX_VALUE : 1)) {
            return stream
                    .filter(path -> matchesExtensionAndNotHidden(path, extensions))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static void copyFileUsingStream(Path source, Path destination) throws IOException {
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void moveDir(Path source, Path destination) {
        try {
            Files.createDirectories(destination);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create dir " + destination, e);
        }

        try (var paths = Files.walk(source)) {
            paths.forEach(sourcePath -> {
                try {
                    var destinationPath = destination.resolve(source.relativize(sourcePath));
                    Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    Log.eNotified(e);
                }
            });
        } catch (IOException e) {
            Log.eNotified(e);
        }
    }

    // Moves from ~/.PDF4Teachers/ to Main.dataFolder
    public static void moveDataFolder(String newDataFolderPath) {
        var oldDataFolderPath = Paths.get(System.getProperty("user.home"), ".PDF4Teachers");
        Log.i("Moving data folder from " + oldDataFolderPath + " to " + newDataFolderPath);

        if (oldDataFolderPath.toString().equals(newDataFolderPath)) return;

        moveDir(oldDataFolderPath, Paths.get(newDataFolderPath));

        PlatformUtils.runLaterOnUIThread(5000, () -> {
            MainWindow.showNotification(AlertIconType.INFORMATION, TR.tr("moveDataFolderNotification", FilesUtils.getPathReplacingUserHome(Paths.get(newDataFolderPath))), 20);
        });
    }

    private static boolean matchesExtensionAndNotHidden(Path path, String[] extensions) {
        try {
            if (Files.isDirectory(path) || Files.isHidden(path)) {
                return false;
            }

            var contentType = Files.probeContentType(path);

            if (contentType != null) {
                var fileExtension = contentType.substring(contentType.lastIndexOf('/') + 1).toLowerCase();
                return StringUtils.containsIgnoreCase(extensions, fileExtension);
            }

            return StringUtils.containsIgnoreCase(extensions, getExtension(path.getFileName().toString()));

        } catch (IOException e) {
            return false;
        }
    }

}
