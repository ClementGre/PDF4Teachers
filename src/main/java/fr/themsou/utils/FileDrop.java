package fr.themsou.utils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import fr.themsou.windows.MainWindow;

@SuppressWarnings("serial")
public class FileDrop extends DropTarget {

	private int component = 0;
	public FileDrop(int component){
		this.component = component;
	}
	

	@SuppressWarnings("rawtypes")
	public void dragEnter(DropTargetDragEvent e){
		
		Transferable transferable = e.getTransferable();
		DataFlavor[] types = transferable.getTransferDataFlavors();
		
		
        for(DataFlavor type : types){
           try{
              if(type.equals(DataFlavor.javaFileListFlavor)){
            	 e.acceptDrag(DnDConstants.ACTION_COPY);
                 Iterator iterator = ((List) transferable.getTransferData(type)).iterator();
                 File file = (File) iterator.next();
                 
                 if(isFileAcceptable(file)){
                	 e.acceptDrag(1);
                 }else{
                	 e.rejectDrag();
                 }
              }
           }catch (Exception e1){ e1.printStackTrace(); }
        }
	}

	public void dragOver(DropTargetDragEvent e){
		// les indices visuels peuvent être ajoutés ici
	}
	public void dropActionChanged(DropTargetDragEvent e){
		
	}
	public void dragExit(DropTargetEvent e){
		
	}
	@SuppressWarnings("rawtypes")
	public void drop(DropTargetDropEvent e){
		
		Transferable transferable = e.getTransferable();
		DataFlavor[] types = transferable.getTransferDataFlavors();
		
        for(DataFlavor type : types){
           try{
              if(type.equals(DataFlavor.javaFileListFlavor)){
            	 e.acceptDrop(DnDConstants.ACTION_COPY);

				  for(Object o : (List) transferable.getTransferData(type)){
					  File file = (File) o;

					  if (isFileAcceptable(file)) {

						  if (component == 1) {
							  MainWindow.mainScreen.openFile(file);
						  } else if (component == 2) {
							  MainWindow.lbFilesTab.openFiles(new File[]{file});
						  }
					  }

				  }
              }
           }catch (Exception e1){ e1.printStackTrace(); }
        }
        
        e.dropComplete(true);
	}
	
	private boolean isFileAcceptable(File file) {
        String fileName = file.getName();
        String ext = "";
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) 
        	ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        
        if(component == 1){
        	
        	if(ext.equalsIgnoreCase("pdf")){
        		return true;
        	}
        	
        }else if(component == 2){
        	
        	if(ext.equalsIgnoreCase("pdf")){
        		return true;
        		
        	}else if(file.isDirectory()){
        		return true;
        	}
        	
        }
        
        return false;
    }
	
	
	
	
}
