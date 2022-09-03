/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.log;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.FooterBar;
import fr.clementgre.pdf4teachers.utils.ConsoleColors;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
    
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss.SSS");
    
    // Error
    public static void e(String s){
        l(LogLevel.ERROR, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), s);
    }
    public static void e(Throwable e){
        l(LogLevel.ERROR, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
        LogsManager.printErr(e);
    }
    
    // Error with a notification
    public static void eNotified(Throwable e, String s){
        String className = Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName();
        l(LogLevel.ERROR, className, s);
        LogsManager.printErr(e);
    
        Platform.runLater(() -> MainWindow.footerBar.showToast(
                Color.color(225/255f, 63/255f, 63/255f), Color.WHITE, FooterBar.ToastDuration.LONG,
                "ERROR: " + StringUtils.removeBeforeLastOccurrence(className, ".") + "() " + s + " " + getConsoleMessage()));
    }
    // Error with a notification
    public static void eNotified(Throwable e){
        String className = Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName();
        l(LogLevel.ERROR, className, e.getMessage());
        LogsManager.printErr(e);
    
        Platform.runLater(() -> MainWindow.footerBar.showToast(
                Color.color(225/255f, 63/255f, 63/255f), Color.WHITE, FooterBar.ToastDuration.LONG,
                "ERROR: " + StringUtils.removeBeforeLastOccurrence(className, ".") + "() " + e.getMessage() + " " + getConsoleMessage()));
    }
    private static String getConsoleMessage(){
        return PlatformUtils.isMac() ? "(Cmd+Opt+C to open the console)" : "(Ctrl+Alt+C to open the console)";
    }
    
    // Error with an alert
    public static void eAlerted(Throwable e){
        l(LogLevel.ERROR, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), e.getMessage());
        LogsManager.printErr(e);
    
        Platform.runLater(() -> ErrorAlert.showErrorAlert(e));
    }
    
    
    // Warning
    public static void w(String s){
        l(LogLevel.WARNING, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), s);
    }
    
    // Info
    public static void i(String s){
        l(LogLevel.INFO, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), s);
    }
    
    // Debug
    public static void d(String s){
        l(LogLevel.DEBUG, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), s);
    }
    
    // Trace
    public static void t(String s){
        l(LogLevel.TRACE, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), s);
    }
    
    // Blank line
    public static void b(LogLevel level){
        if(level.getLevel() >= Main.logLevel.getLevel()){
            LogsManager.println();
        }
    }
    
    // Log with level (only for others classes)
    public static void l(LogLevel level, String s){
        l(level, Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getMethodName(), s);
    }
    
    // Log with level and className
    private static void l(LogLevel level, String className, String s){
        if(level.getLevel() >= Main.logLevel.getLevel()){
            LogsManager.println(ConsoleColors.RESET + level.getColor() + dateFormat.format(Calendar.getInstance().getTime()) + " "
                    + level.getName() + " "
                    + ConsoleColors.BLACK_BRIGHT + StringUtils.removeBeforeLastOccurrence(className, ".") + "() "
                    + ConsoleColors.RESET + s);
        }
    }
    
    public static boolean doDebug(){
        return Main.logLevel.getLevel() <= LogLevel.DEBUG.getLevel();
    }
    
    public static String formatConsoleLogColored(String text, boolean error){
        String color = error ? ConsoleColors.RED : ConsoleColors.GREEN;
        String suffix = error ? "[ERROR]" : "[INFO] ";
        
        return ConsoleColors.RESET + color + dateFormat.format(Calendar.getInstance().getTime()) + " "
                + "CONSOLE "
                + ConsoleColors.BLACK_BRIGHT + suffix + " "
                + ConsoleColors.RESET + text;
    }
    
    public static String formatConsoleLog(String text, boolean error){
        String suffix = error ? "[ERROR]" : "[INFO] ";
        
        return dateFormat.format(Calendar.getInstance().getTime()) + " "
                + "CONSOLE " + suffix + " " + text;
    }
    
}
