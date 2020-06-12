package fr.themsou.utils;

import fr.themsou.document.render.display.PDFPagesRender;
import javafx.application.Platform;

import java.io.*;
import java.util.Locale;

public class CustomPrintStream extends PrintStream {

    private static final String newLine = System.getProperty("line.separator");
    private final PrintStream original;

    private static final String catchText = "GRAVE: GlyphDescription for index";
    private static String actualText = "";

    private StringBuffer text;

    public CustomPrintStream(PrintStream original, StringBuffer out){
        super(new OutputStream(){
            @Override public void write(int b){
                original.write(b);
                out.append((char) b);
                writeByte(b);
            }
        });
        this.original = original;
        this.text = out;

    }

    public static void writeByte(int b){
        actualText += (char) b;
        if(actualText.equalsIgnoreCase(catchText)){
            Platform.runLater(PDFPagesRender::renderAdvertisement);
            actualText = "";

        }else if(actualText.endsWith("\n")){
            actualText = "";
        }
    }

    public void print(double d){
        text.append(d);
        original.print(d);
    }

    public void print(String s){
        text.append(s);
        original.print(s);
    }

    public void println(String s){
        text.append(s).append(newLine);
        original.println(s);
    }

    public void println(){
        text.append(newLine);
        original.println();
    }

    public PrintStream printf(String s, Object... args) {
        text.append(String.format(s, args));
        original.printf(s, args);
        return this;
    }
    public PrintStream printf(Locale l, String s, Object... args){
        text.append(String.format(s, args));
        original.printf(s, args);
        return this;
    }

    private void write(String s) {
        System.out.println("write : " + s);
    }

}
