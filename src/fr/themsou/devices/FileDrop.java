package fr.themsou.devices;

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

import fr.themsou.main.Main;

@SuppressWarnings("serial")
public class FileDrop extends DropTarget {


	@SuppressWarnings("rawtypes")
	public void dragEnter(DropTargetDragEvent e){
		
		Transferable transférable = e.getTransferable();
		DataFlavor[] types = transférable.getTransferDataFlavors();
		
        for(DataFlavor type : types){
           try{
              if(type.equals(DataFlavor.javaFileListFlavor)){
            	  e.acceptDrag(DnDConstants.ACTION_COPY);
                 Iterator iterator = ((List) transférable.getTransferData(type)).iterator();
                 File file = (File) iterator.next();
                 
                 if(getFileExtension(file).equals("pdf")){
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
		
		Transferable transférable = e.getTransferable();
		DataFlavor[] types = transférable.getTransferDataFlavors();
		
        for(DataFlavor type : types){
           try{
              if(type.equals(DataFlavor.javaFileListFlavor)){
            	 e.acceptDrop(DnDConstants.ACTION_COPY);
                 Iterator iterator = ((List) transférable.getTransferData(type)).iterator();
                 File file = (File) iterator.next();
                 
                 if(getFileExtension(file).equals("pdf")){
                	 Main.mainscreen.openFile(file);
                 }else{
                	 e.rejectDrop();
                 }
              }
           }catch (Exception e1){ e1.printStackTrace(); }
        }
        
        e.dropComplete(true);
	}
	
	private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
