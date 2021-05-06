package fr.clementgre.pdf4teachers.document.render.convert;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialog.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialog.alerts.OKAlert;

import java.io.File;
import java.io.IOException;

public class ConvertDocument{
    
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
                                e.printStackTrace();
                            }
                            doBreak = true;
                        }
                        case SKIP -> {
                            try{
                                file.document.close();
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                            continue;
                        }
                    }
                    if(doBreak) break;
                }
                
                if(MainWindow.mainScreen.hasDocument(false)){
                    if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.file.getAbsolutePath())){
                        if(!MainWindow.mainScreen.closeFile(true)) continue;
                    }
                }
                
                try{
                    file.document.save(file.file);
                    file.document.close();
                }catch(IOException e){
                    e.printStackTrace();
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
