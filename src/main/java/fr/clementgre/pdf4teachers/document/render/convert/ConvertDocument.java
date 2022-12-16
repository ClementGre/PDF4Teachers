/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.convert;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.OKAlert;

import java.io.File;
import java.io.IOException;

public class ConvertDocument {
    
    public ConvertWindow convertWindow;
    
    public ConvertDocument(){
        convertWindow = new ConvertWindow(null, (convertedFiles) -> {
            
            int converted = 0;
            AlreadyExistDialogManager alreadyExistDialogManager = new AlreadyExistDialogManager(true);
            for(ConvertedFile file : convertedFiles){
                
                if(file.file.exists()){
                    AlreadyExistDialogManager.ResultType result = alreadyExistDialogManager.showAndWait(file.file);
                    boolean doBreak = false;
                    switch(result){
                        case RENAME -> file.file = AlreadyExistDialogManager.rename(file.file);
                        case STOP -> {
                            try{
                                file.document.close();
                            }catch(IOException e){
                                Log.eNotified(e);
                            }
                            doBreak = true;
                        }
                        case SKIP -> {
                            try{
                                file.document.close();
                            }catch(IOException e){
                                Log.eNotified(e);
                            }
                            continue;
                        }
                    }
                    if(doBreak) break;
                }
                
                if(MainWindow.mainScreen.hasDocument(false)){
                    if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.file.getAbsolutePath())){
                        if(!MainWindow.mainScreen.closeFile(true, false)) continue;
                    }
                }
                
                try{
                    file.document.save(file.file);
                    file.document.close();
                }catch(IOException e){
                    Log.eNotified(e);
                }
                
                
                MainWindow.filesTab.openFiles(new File[]{file.file});
                converted++;
            }
            
            OKAlert alert = new OKAlert(TR.tr("convertWindow.dialog.completed.title"));
            
            if(converted == 0) alert.setHeaderText(TR.tr("convertWindow.dialog.completed.header.noDocument"));
            else if(converted == 1) alert.setHeaderText(TR.tr("convertWindow.dialog.completed.header.oneDocument"));
            else alert.setHeaderText(TR.tr("convertWindow.dialog.completed.header.multipleDocuments", converted));
            
            
            if(converted > 1) alert.setContentText(TR.tr("convertWindow.dialog.completed.details.multipleDocuments"));
            else if(converted != 0) alert.setContentText(TR.tr("convertWindow.dialog.completed.details.oneDocument"));
            
            alert.show();
        });
    }
    
}
