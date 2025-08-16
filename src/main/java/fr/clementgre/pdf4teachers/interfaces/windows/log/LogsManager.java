/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.log;

import fr.clementgre.pdf4teachers.utils.ConsoleColors;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class LogsManager {
    
    public static final PrintStream consoleOut = System.out;
    public static final PrintStream consoleErr = System.err;
    
    public static final StringBuffer logsOutput = new StringBuffer();
    
    public static void copyLogs(){
        try{
            LogsOutputStream outOutputStream = new LogsOutputStream(logsOutput, consoleOut, false);
            LogsOutputStream errOutputStream = new LogsOutputStream(logsOutput, consoleOut, true);
            
            PrintStream outPrintStream = new PrintStream(outOutputStream, true, StandardCharsets.UTF_8);
            PrintStream errPrintStream = new PrintStream(errOutputStream, true, StandardCharsets.UTF_8);
            
            System.setOut(outPrintStream);
            System.setErr(errPrintStream);
        }catch(Exception e){
            Log.eNotified(e);
        }
    }
    
    public static void println(){
        logsOutput.append("\n");
        consoleOut.println();
    }
    public static void println(String s){
        logsOutput.append(removeColorization(s)).append("\n");
        consoleOut.println(s);
    }
    public static void print(String s){
        logsOutput.append(removeColorization(s));
        consoleOut.print(s);
    }
    public static void printErr(Throwable e){
        StringWriter sw = new StringWriter();
        try(PrintWriter pw = new PrintWriter(sw)){
            e.printStackTrace(pw);
        }
        logsOutput.append(sw.getBuffer());
        
        consoleOut.print(ConsoleColors.RED_BRIGHT);
        e.printStackTrace(consoleOut);
        consoleOut.print(ConsoleColors.RESET);
    }
    
    private static String removeColorization(String text){
        return text.replaceAll("(\033\\[.;..m)|(\033\\[..m)|(\033\\[.;...m)|(\033\\[.m)", "");
    }
    
    
    public static String getLogs(){
        return logsOutput.toString();
    }
    public static int getLogsLength(){
        return logsOutput.length();
    }
}
