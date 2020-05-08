package fr.themsou;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Main {

    static ArrayList<String> texts = new ArrayList<>();

    public static void main(String[] args) {

        File dir = new File("C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\java\\fr\\themsou");
        File out = new File("C:\\Users\\Clement\\Downloads\\template.txt");

        fetchFiles(dir);
        write(out);

    }

    public static void fetchFiles(File dir){
        for(File file : dir.listFiles()){
            if(file.isDirectory()) fetchFiles(file);
            else readFile(file);
        }
    }

    public static void readFile(File file){

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder fileText = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null){
                while(line.startsWith(" ")){
                    line = line.substring(1);
                }
                fileText.append(line);
            }
            reader.close();

            String[] trStarts = fileText.toString()
                    .replaceAll(Pattern.quote("\" + \""), "")
                    .replaceAll(Pattern.quote("\"+ \""), "")
                    .replaceAll(Pattern.quote("\" +\""), "")
                    .replaceAll(Pattern.quote("\"+\""), "")
                    .split(Pattern.quote("TR.tr(\""));

            texts.add("# " + file.getName().replaceAll(Pattern.quote(".java"), ""));
            int i = 0;
            for(String trStart : trStarts){
                if(i != 0){
                    String text = removeAfter(trStart, "\"") + "=";
                    if(!texts.contains(text)) texts.add(text);
                }
                i++;
            }

        }catch (IOException e){ e.printStackTrace(); }

    }

    public static void write(File file){

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            for(String line : texts){
                writer.write(line);
                writer.newLine();
            }

            writer.flush();
            writer.close();

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public static String removeAfter(String string, String rejex){

        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }
}
