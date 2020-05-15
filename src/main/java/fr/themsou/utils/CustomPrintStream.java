package fr.themsou.utils;

import java.io.*;

public class CustomPrintStream extends PrintStream {

    private static final String newLine = System.getProperty("line.separator");
    private final PrintStream original;

    private StringBuffer text;

    public CustomPrintStream(PrintStream original, StringBuffer out){
        super(new OutputStream(){
            @Override public void write(int b){
                original.write(b);
                out.append((char) b);
            }
        });
        this.original = original;
        this.text = out;
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
        return original.printf(s, args);
    }

}
