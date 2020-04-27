package fr.themsou.utils;

import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.notes.NoteTreeItem;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class TR {

    private static ArrayList<String> template = null;
    private static HashMap<String, String> translations;
    //private static final boolean WRITE_TEMPLATE = false;

    public static String tr(String text){

        if(translations != null){
            String translated = translations.get(text.replaceAll(Pattern.quote("\n"), Pattern.quote("\\n")));
            if(translated != null){
                return translated;
            }
        }

        /*if(WRITE_TEMPLATE){
            writeTemplate(text.replaceAll(Pattern.quote("\n"), Pattern.quote("\\n")));
        }*/

        return text;
    }

    public static boolean loadTranslationFile(String fileName, boolean extract){

        translations = null;

        File folder = new File(Main.dataFolder + "translations");
        folder.mkdirs();

        File file = new File(Main.dataFolder + "translations" + File.separator + fileName + ".yml");

        if(extract){
            try{
                InputStream docRes = Main.class.getResourceAsStream("/translations/" + fileName + ".yml");
                Files.copy(docRes, file.getAbsoluteFile().toPath());
                return loadFileTranslationsData(file);

            }catch(IOException e){ e.printStackTrace(); }

        }else if(file.exists()){
            try{
                return loadFileTranslationsData(file);

            }catch(IOException e){ e.printStackTrace(); }

        }

        Main.format.getDecimalFormatSymbols().setDecimalSeparator('.');

        return false;
    }

    private static boolean loadFileTranslationsData(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line; int i = 0;
        while((line = reader.readLine()) != null){

            if(!line.isBlank()){
                if(line.startsWith("#")) continue;

                String key = line.split(Pattern.quote("="))[0];
                String value = StringUtils.removeBeforeNotEscaped(line, "=");

                if(key != null){
                    if(!key.isBlank() && !value.isBlank()){
                        translations.put(key.replaceAll(Pattern.quote("\\n"), Pattern.quote("\n")), value.replaceAll(Pattern.quote("\\n"), Pattern.quote("\n")));
                        i++;
                    }
                }

            }
        }
        reader.close();

        return i >= 1;

    }

    /*private static void writeTemplate(String text){

        File folder = new File(Main.dataFolder + "translations"); folder.mkdirs();
        File file = new File(Main.dataFolder + "translations" + File.separator + "template.yml");


        if(template == null){
            try{
                file.createNewFile();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                template = new ArrayList<>();

                String line;
                while((line = reader.readLine()) != null){

                    if(!line.isBlank()){
                        if(line.startsWith("#")) continue;

                        String key = line.split(Pattern.quote("="))[0];
                        if(key != null){
                            if(!key.isBlank()){
                                template.add(key.replaceAll(Pattern.quote("\\n"), Pattern.quote("\n")));
                            }
                        }

                    }
                }
                reader.close();
            }catch(IOException e){  e.printStackTrace(); }

        }

        if(template.contains(text)) return;

        try{
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            writer.write(text + "=");
            writer.newLine();
            template.add(text);
            System.out.println("Write template \"" + text + "\"");

            writer.flush();
            writer.close();

        }catch(IOException e){
            e.printStackTrace();
        }

    }*/

}
