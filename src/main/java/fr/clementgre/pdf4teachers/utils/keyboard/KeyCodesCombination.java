/*
 * Copyright (c) 2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */
// This code comes in part from the JavaFX source code (javafx.scene.input.KeyCodeCombination)
package fr.clementgre.pdf4teachers.utils.keyboard;

import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.List;

public class KeyCodesCombination extends KeyCombination {
    
    private final List<KeyCode> codes;
    
    public final List<KeyCode> getCodes(){
        return codes;
    }
    
    public KeyCodesCombination(final List<KeyCode> code,
                               final ModifierValue shift,
                               final ModifierValue control,
                               final ModifierValue alt,
                               final ModifierValue meta,
                               final ModifierValue shortcut){
        super(shift, control, alt, meta, shortcut);
        this.codes = code;
    }
    public KeyCodesCombination(final List<KeyCode> codes,
                               final Modifier... modifiers){
        super(modifiers);
        this.codes = codes;
    }
    public KeyCodesCombination(final KeyCode code1, final KeyCode code2,
                               final Modifier... modifiers){
        super(modifiers);
        this.codes = Arrays.asList(code1, code2);
    }
    public KeyCodesCombination(final KeyCode code1, final KeyCode code2, final KeyCode code3,
                               final Modifier... modifiers){
        super(modifiers);
        this.codes = Arrays.asList(code1, code2, code3);
    }
    public KeyCodesCombination(final KeyCode code1, final KeyCode code2, final KeyCode code3, final KeyCode code4,
                               final Modifier... modifiers){
        super(modifiers);
        this.codes = Arrays.asList(code1, code2, code3, code4);
    }
    public KeyCodesCombination(final KeyCode code1, final KeyCode code2, final KeyCode code3, final KeyCode code4, final KeyCode code5,
                               final Modifier... modifiers){
        super(modifiers);
        this.codes = Arrays.asList(code1, code2, code3, code4, code5);
    }
    
    @Override
    public boolean match(KeyEvent event){
        return getCodes().stream().anyMatch(c -> c == event.getCode()) && super.match(event);
    }
    
    @Override
    public String getName(){
        StringBuilder sb = new StringBuilder();
        
        sb.append(super.getName());
        
        if(!sb.isEmpty()){
            sb.append("+");
        }
        
        sb.append(codes.stream().map(KeyCode::getName).reduce((a, b) -> a + "/" + b).orElse(""));
        
        return sb.toString();
    }
    
    @Override
    public String getDisplayText(){
        
        String codesString = codes.stream().map(code -> {
            char c = getSingleChar(code);
            if(c != 0){
                return Character.toString(c);
            }
            
            // Compute a name based on the enum name, e.g. F13 becomes F13 and
            // NUM_LOCK becomes Num Lock
            String name = code.toString();
            
            // We only want the first letter to be upper-case, so we convert 'ENTER'
            // to 'Enter' -- and we also convert multiple words separated by _
            // such that each individual word is capitalized and the underline replaced
            // by spaces.
            StringBuilder sb = new StringBuilder();
            String[] words = name.split("_");
            for(String word : words){
                if(!sb.isEmpty()){
                    sb.append(' ');
                }
                sb.append(word.charAt(0));
                sb.append(word.substring(1).toLowerCase());
            }
            return sb.toString();
        }).reduce((a, b) -> a + "/" + b).orElse("");
        
        return super.getDisplayText() + codesString;
    }
    
    @Override
    public boolean equals(final Object obj){
        if(this == obj){
            return true;
        }
        
        if(!(obj instanceof KeyCodesCombination)){
            return false;
        }
        
        return (getCodes().equals(((KeyCodesCombination) obj).getCodes()))
                && super.equals(obj);
    }
    
    @Override
    public int hashCode(){
        return 23 * super.hashCode() + codes.hashCode();
    }
    
    private static char getSingleChar(KeyCode code){
        switch(code){
            case ENTER:
                return '↵';
            case LEFT:
                return '←';
            case UP:
                return '↑';
            case RIGHT:
                return '→';
            case DOWN:
                return '↓';
            case COMMA:
                return ',';
            case MINUS:
                return '-';
            case PERIOD:
                return '.';
            case SLASH:
                return '/';
            case SEMICOLON:
                return ';';
            case EQUALS:
                return '=';
            case OPEN_BRACKET:
                return '[';
            case BACK_SLASH:
                return '\\';
            case CLOSE_BRACKET:
                return ']';
            case MULTIPLY:
                return '*';
            case ADD:
                return '+';
            case SUBTRACT:
                return '-';
            case DECIMAL:
                return '.';
            case DIVIDE:
                return '/';
            case BACK_QUOTE:
                return '`';
            case QUOTE:
                return '"';
            case AMPERSAND:
                return '&';
            case ASTERISK:
                return '*';
            case LESS:
                return '<';
            case GREATER:
                return '>';
            case BRACELEFT:
                return '{';
            case BRACERIGHT:
                return '}';
            case AT:
                return '@';
            case COLON:
                return ':';
            case CIRCUMFLEX:
                return '^';
            case DOLLAR:
                return '$';
            case EURO_SIGN:
                return '€';
            case EXCLAMATION_MARK:
                return '!';
            case LEFT_PARENTHESIS:
                return '(';
            case NUMBER_SIGN:
                return '#';
            case PLUS:
                return '+';
            case RIGHT_PARENTHESIS:
                return ')';
            case UNDERSCORE:
                return '_';
            case DIGIT0:
                return '0';
            case DIGIT1:
                return '1';
            case DIGIT2:
                return '2';
            case DIGIT3:
                return '3';
            case DIGIT4:
                return '4';
            case DIGIT5:
                return '5';
            case DIGIT6:
                return '6';
            case DIGIT7:
                return '7';
            case DIGIT8:
                return '8';
            case DIGIT9:
                return '9';
            default:
                break;
        }
        
        /*
         ** On Mac we display these unicode symbols,
         ** otherwise we default to the Text version of the char.
         */
        if(PlatformUtils.isMac()){
            switch(code){
                case BACK_SPACE:
                    return '⌫';
                case ESCAPE:
                    return '⎋';
                case DELETE:
                    return '⌦';
                default:
                    break;
            }
        }
        return 0;
    }
}
