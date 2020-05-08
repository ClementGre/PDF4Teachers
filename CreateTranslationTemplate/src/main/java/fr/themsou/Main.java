package fr.themsou;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {

    static ArrayList<String> codeKeys = new ArrayList<>();
    static HashMap<String, ArrayList<String>> codeFilesKeys = new HashMap<>();

    static ArrayList<String> existingKeys = new ArrayList<>();
    static HashMap<String, HashMap<String, String>> existingFilesTranslations = new HashMap<>();

    public static boolean READ_EXISTING_FILE = true;
    public static String PROJECT_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers";
    public static String ALREADY_TRANSLATED_FILE_NAME = "English US.txt";

    public static void main(String[] args) {

        File in = new File(PROJECT_PATH + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "translations" + File.separator + ALREADY_TRANSLATED_FILE_NAME);
        File dir = new File(PROJECT_PATH + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "fr" + File.separator + "themsou");
        File out = new File(PROJECT_PATH + File.separator + "template.txt");

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

            HashMap<String, String> translationsToAdd = new HashMap<>();
            String fileName = null;
            String line;
            while((line = reader.readLine()) != null){
                if(line.startsWith("# ")){
                    if(fileName != null){
                        existingFilesTranslations.put(fileName, translationsToAdd);
                        translationsToAdd = new HashMap<>();
                    }
                    fileName = line.replaceFirst(Pattern.quote("# "), "");
                }else{
                    String key = removeAfterNotEscaped(line, "=");
                    String value = removeBeforeNotEscaped(line, "=");
                    if(!existingKeys.contains(key)){
                        existingKeys.add(key);
                        translationsToAdd.put(key, value);

                        if(value.isEmpty()) System.out.println("WARNING : no translations in the existing file for : " + key);
                    }else System.out.println("WARNING : a key is twice in the existing file : " + key);
                }
            }
            if(fileName != null) existingFilesTranslations.put(fileName, translationsToAdd);

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

            ArrayList<String> textsToAdd = new ArrayList<>();
            int i = 0;
            for(String trStart : trStarts){
                if(i != 0){
                    String key = removeAfter(trStart, "\"");
                    if(!codeKeys.contains(key)){
                        codeKeys.add(key);
                        textsToAdd.add(key);
                    }
                }
                i++;
            }
            codeFilesKeys.put(fileName, textsToAdd);

        }catch (IOException e){ e.printStackTrace(); }

    }

    public static void write(File file){

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            for(Map.Entry<String, HashMap<String, String>> fileTranslations : existingFilesTranslations.entrySet()){
                if(codeFilesKeys.containsKey(fileTranslations.getKey())) {
                    writer.write("# " + fileTranslations.getKey());
                    writer.newLine();
                    for(Map.Entry<String, String> translations : fileTranslations.getValue().entrySet()){
                        if(codeFilesKeys.get(fileTranslations.getKey()).contains(translations.getKey())){
                            writer.write(translations.getKey() + "=" + translations.getValue());
                            writer.newLine();
                            codeFilesKeys.get(fileTranslations.getKey()).remove(translations.getKey());
                        }else{
                            System.out.println("Remove existing translation from file (unused) : " + translations.getKey() + "=" + translations.getValue());
                        }
                    }

                    for(String key : codeFilesKeys.get(fileTranslations.getKey())){
                        writer.write(key + "=");
                        System.out.println("Add translation line to : " + key);
                        writer.newLine();
                    }
                    codeFilesKeys.remove(fileTranslations.getKey());

                }else{
                    System.out.println("Remove existing translation file from file (unused) : # " + fileTranslations.getKey());
                }
            }

            for(Map.Entry<String, ArrayList<String>> fileKeys : codeFilesKeys.entrySet()){
                writer.write("# " + fileKeys.getKey());
                System.out.println("Add file : # " + fileKeys.getKey());
                writer.newLine();
                for(String key : fileKeys.getValue()){
                    writer.write(key + "=");
                    System.out.println("Add translation line to : " + key);
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