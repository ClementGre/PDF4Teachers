/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.log;

import fr.clementgre.pdf4teachers.document.render.display.PDFPagesRender;
import fr.clementgre.pdf4teachers.utils.ConsoleColors;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogsOutputStream extends OutputStream {
    
    private final StringBuffer buffer = new StringBuffer();
    
    private final StringBuffer output;
    private final PrintStream consoleOutput;
    private final boolean errorStream;
    
    public LogsOutputStream(StringBuffer output, PrintStream consoleOutput, boolean errorStream) {
        this.output = output;
        this.consoleOutput = consoleOutput;
        this.errorStream = errorStream;
    }
    
    public void write(int i) throws IOException{
        buffer.append(i);
    }
    public void write(byte @NotNull [] bytes, int i, int i1) throws IOException{
        buffer.append(new String(bytes, i, i1));
    }
    
    @Override
    public void flush() throws IOException{
        super.flush();
        
        if(buffer.length() > 0){
            output.append(Log.formatConsoleLog(buffer.toString(), errorStream));
            consoleOutput.print(Log.formatConsoleLogColored(buffer.toString(), errorStream));
            
            onLineAdded(buffer.toString());
            buffer.setLength(0);
        }
    }
    
    private void onLineAdded(String line){
        if(line.contains("GRAVE: GlyphDescription for index")){
            Platform.runLater(PDFPagesRender::renderAdvertisement);
        }
    }
    
    private String getColorization(){
        if(errorStream) return ConsoleColors.RED;
        else return "";
    }
    
}
