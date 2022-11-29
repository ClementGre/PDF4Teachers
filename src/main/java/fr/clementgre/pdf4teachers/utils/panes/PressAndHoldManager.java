/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.panes;

import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class PressAndHoldManager{
    
    private long startMillis;
    private boolean pressed;
    
    public PressAndHoldManager(Node node, long actionInterval, CallBack handler){
        
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            pressed = true;
            startMillis = System.currentTimeMillis();
    
            new Thread(() -> {
                while(pressed){
                    if(startMillis + 500 < System.currentTimeMillis()) Platform.runLater(handler::call);
                    PlatformUtils.sleepThread(actionInterval);
                }
            }, "pressAndHoldThread").start();
            
        });
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> end());
        node.addEventHandler(MouseEvent.DRAG_DETECTED, e -> end());
        node.addEventHandler(MouseEvent.MOUSE_EXITED, e -> end());
    }
    
    private void end(){
        pressed = false;
    }
    
}
