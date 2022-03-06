/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.splitpdf;

import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;

public record SplitEngine(SplitWindow splitWindow, int detectedPages) {
    
    public void process(){
    
    }
    public void updateDetectedPages(CallBack callBack){
    
    }
}
