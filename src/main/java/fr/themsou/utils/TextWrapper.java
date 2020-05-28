package fr.themsou.utils;
import fr.themsou.utils.components.ScratchText;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.regex.Pattern;

public class TextWrapper {

    private String text;
    private Font font;
    private int maxWidth;

    private int wordIndex = 0;
    private int charIndex = 0;
    private String wrappedLine = "";

    public TextWrapper(String text, Font font, int maxWidth){
        this.font = font;
        this.text = text;
        this.maxWidth = maxWidth;

    }

    public String wrapFirstLine(){

        while(text.length() != 0) { // Tant que l'index est plus petit que le nombre de mots, crée une nouvelle ligne et la remplis de mots

            if(text.split(" ").length == 0) break;
            if(text.startsWith(" ")) text = text.replaceFirst(Pattern.quote(" "), "");
            String firstWord = text.split(" ")[0];

            if(!test(firstWord)){
                while(!test(firstWord)){
                    String[] results = fillLineWithChar(firstWord);
                    return results[0];
                }
            }

            String[] results = fillLineWithWord(text);
            return results[0];

        }
        return wrappedLine;
    }

    public String wrap(){

        while(text.length() != 0) { // Tant que l'index est plus petit que le nombre de mots, crée une nouvelle ligne et la remplis de mots

            if(text.split(" ").length == 0) break;
            if(text.startsWith(" ")) text = text.replaceFirst(Pattern.quote(" "), "");
            String firstWord = text.split(" ")[0];

            if(!test(firstWord)){
                String[] results = new String[]{"", ""};
                String passedChars = "";
                while(!test(firstWord)){
                    passedChars += results[0];
                    results = fillLineWithChar(firstWord);
                    firstWord = results[1];
                    appendLine(results[0]);
                }
                if(firstWord.isEmpty()){
                    text = text.replaceFirst(Pattern.quote(passedChars), "");
                    continue;
                }
                else{
                    text = text.replaceFirst(Pattern.quote(passedChars + results[0]), "");
                    continue;
                }

            }

            String[] results = fillLineWithWord(text);
            text = results[1];
            appendLine(results[0]);

        }
        return wrappedLine;
    }

    private void appendLine(String text){
        wrappedLine += wrappedLine.isEmpty() ? text : "\n" + text;
    }

    private boolean test(String line){
        ScratchText toTest = new ScratchText(line); toTest.setFont(font);
        return toTest.getBoundsInParent().getWidth() < maxWidth;
    }

    private String[] fillLineWithWord(String text){

        String line = text.split(" ")[0];
        wordIndex = 1;
        while(wordIndex < text.split(" ").length){ // Remplis la ligne avec le maximum de mots puis renvoie la ligne

            String lastLine = line;
            line += " " + text.split(" ")[wordIndex];

            if(!test(line)){
                return new String[]{lastLine, text.replaceFirst(Pattern.quote(lastLine), "")};
            }else wordIndex++;

        }
        wordIndex = 0;
        return new String[]{line, text.replaceFirst(Pattern.quote(line), "")};
    }
    private String[] fillLineWithChar(String word){

        String line = word.substring(0, 1);
        charIndex = 1;
        while(charIndex < word.length()){ // Remplis la ligne avec le maximum de mots puis renvoie la ligne
            String lastLine = line;
            line = word.substring(0, charIndex+1);

            if(!test(line)){
                return new String[]{lastLine, word.replaceFirst(Pattern.quote(lastLine), "")};
            }else charIndex++;
        }
        charIndex = 0;
        return new String[]{line, word.replaceFirst(Pattern.quote(line), "")};

    }

}
