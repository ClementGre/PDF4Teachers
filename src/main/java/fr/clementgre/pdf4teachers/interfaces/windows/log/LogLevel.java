/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.log;

import fr.clementgre.pdf4teachers.utils.ConsoleColors;

public enum LogLevel {
//                         "CONSOLE"
    ERROR(5,   "ERROR  ", true, ConsoleColors.RED),
    WARNING(4, "WARN   ", true, ConsoleColors.YELLOW),
    INFO(3,    "INFO   ", true, ConsoleColors.GREEN),
    DEBUG(2,   "DEBUG  ", true, ConsoleColors.CYAN),
    TRACE(1,   "TRACE  ", true, ConsoleColors.PURPLE);
    
    
    private final int level;
    private final String name;
    private final boolean red;
    private final String color;
    LogLevel(int level, String name, boolean red, String color){
        this.level = level;
        this.name = name;
        this.red = red;
        this.color = color;
    }
    
    public int getLevel(){
        return level;
    }
    public String getName(){
        return name;
    }
    public boolean isRed(){
        return red;
    }
    public String getColor(){
        return color;
    }
}
