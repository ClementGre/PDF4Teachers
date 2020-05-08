package fr.themsou;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {

    static ArrayList<String> texts = new ArrayList<>();
    static HashMap<String, ArrayList<String>> filesTexts = new HashMap<>();

    static HashMap<String, String> existingTranslations = new HashMap<>();

    public static boolean READ_EXISTING_FILE = true;

    public static void main(String[] args) {

        File in = new File("C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\resources\\translations\\English US.txt");
        File dir = new File("C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\java\\fr\\themsou");
        File out = new File("C:\\Users\\Clement\\Downloads\\template.txt");

        if(READ_EXISTING_FILE){
            System.out.println("-------------------------");
            System.out.println("-> Reading already translated file...");
            System.out.println("-------------------------");
            readExistingFile(in);
        }

        System.out.println("-------------------------");
        System.out.println("-> Reading code files...");
        System.out.println("-------------------------");
        fetchFiles(dir);

        System.out.println("-------------------------");
        System.out.println("-> Writing...");
        System.out.println("-------------------------");
        write(out);

    }

    public static void readExistingFile(File file){

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));

            ArrayList<String> fileTexts = new ArrayList<>();
            String fileName = null;
            String line;
            while((line = reader.readLine()) != null){
                if(line.startsWith("# ")){
                    if(fileName != null){
                        filesTexts.put(fileName, fileTexts);
                        fileTexts = new ArrayList<>();
                    }
                    fileName = line.replaceFirst(Pattern.quote("# "), "");
                }else{
                    String text = removeAfterNotEscaped(line, "=");
                    String value = removeBeforeNotEscaped(line, "=");
                    if(!texts.contains(text)){
                        texts.add(text);
                        fileTexts.add(text);
                        if(!value.isEmpty()){
                            existingTranslations.put(text, value);
                        }else System.err.println("WARNING : " + text + " don't have translation in the existing file");
                    }else System.err.println("WARNING : " + text + " is twice in the existing file");
                }
            }
            if(fileName != null) filesTexts.put(fileName, fileTexts);

        }catch (IOException e){ e.printStackTrace(); }
    }

    public static void fetchFiles(File dir){
        for(File file : dir.listFiles()){
            if(file.isDirectory()) fetchFiles(file);
            else readFile(file);
        }
    }

    public static void readFile(File file){
        String fileName = file.getName().replaceAll(Pattern.quote(".java"), "");
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

            ArrayList textsToAdd = new ArrayList();
            int i = 0;
            for(String trStart : trStarts){
                if(i != 0){
                    String text = removeAfter(trStart, "\"");
                    if(!texts.contains(text)){
                        texts.add(text);
                        textsToAdd.add(text);
                        System.out.println("Add translation line to : " + text);
                    }
                }
                i++;
            }

            if(filesTexts.containsKey(fileName)){
                ArrayList<String> newTexts = filesTexts.get(fileName);
                newTexts.addAll(textsToAdd);
                filesTexts.put(fileName, newTexts);
            }else{
                filesTexts.put(fileName, textsToAdd);
            }

        }catch (IOException e){ e.printStackTrace(); }

    }

    public static void write(File file){

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            for(Map.Entry<String, ArrayList<String>> fileTexts : filesTexts.entrySet()){
                writer.write("# " + fileTexts.getKey());
                writer.newLine();
                for(String text : fileTexts.getValue()){
                    writer.write(text + "=");
                    if(existingTranslations.containsKey(text)){
                        writer.write(existingTranslations.get(text));
                    }else System.out.println("No translation for : " + text);
                    writer.newLine();
                }
            }

            writer.flush();
            writer.close();

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    // UTILS

    public static String removeAfter(String string, String rejex){

        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }
    public static String removeBeforeNotEscaped(String string, String rejex){

        int fromIndex = 0;
        while(true){

            int index = string.indexOf(rejex, fromIndex);
            if(index == -1) return string;

            if(!string.startsWith("\\", index-1)){
                if(index < string.length()) return string.substring(index + rejex.length());
                return "";
            }else{
                fromIndex = index + 1;
            }

        }
    }
    public static String removeAfterNotEscaped(String string, String rejex){

        int fromIndex = 0;
        while(true){

            int index = string.indexOf(rejex, fromIndex);
            if(index == -1) return string;

            if(!string.startsWith("\\", index-1)){
                if(index < string.length()) return string.substring(0, index);
                return "";
            }else{
                fromIndex = index + 1;
            }

        }
    }
}