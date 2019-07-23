package fr.themsou.devices;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("serial")
public class FileDrop extends DropTarget {

	@SuppressWarnings({"unchecked" })
	public boolean isDragAcceptable(DropTargetDragEvent e){
		try{
			Transferable t = e.getTransferable();
	        DataFlavor[] dataFlavors = t.getTransferDataFlavors();
	        for(DataFlavor df : dataFlavors){
	            if(df.isFlavorJavaFileListType()){
	                File[] filesArray = (File[]) ((List<File>) t.getTransferData(df)).toArray();
	                File file = (File) filesArray[0];
	                
	                if(getFileExtension(file).equals("jpg") || getFileExtension(file).equals("png") || getFileExtension(file).equals("jpeg") || getFileExtension(file).equals("tif")){
	                	return true;
	                }
	            }
	        }
		}catch(UnsupportedFlavorException e2){
	    }catch(IOException ex){ ex.printStackTrace(); }
		
		return false;
	}
	@SuppressWarnings("rawtypes")
	public boolean isDropAcceptable(DropTargetDropEvent e){
		
		Transferable transférable = e.getTransferable();
		DataFlavor[] types = e.getCurrentDataFlavors();
		
        for(DataFlavor type : types){
           try{
              if(type.equals(DataFlavor.javaFileListFlavor)){
            	  
                 Iterator iterator = ((List) transférable.getTransferData(type)).iterator();
                 File file = (File) iterator.next();
                
                 if(getFileExtension(file).equals("jpg") || getFileExtension(file).equals("png") || getFileExtension(file).equals("jpeg") || getFileExtension(file).equals("tif")){
                	 
                	 return true;
                 }
                 
                   
              }
           }
           catch (Exception e1){ e1.printStackTrace(); }
        }
		
		return false;
		
	}

	public void dragEnter(DropTargetDragEvent e){
		if (!isDragAcceptable(e)){
			e.rejectDrag();
			return;
		}
	}

	public void dragOver(DropTargetDragEvent e){
		// les indices visuels peuvent être ajoutés ici
	}

	public void dropActionChanged(DropTargetDragEvent e){
		if (!isDragAcceptable(e)){
			e.rejectDrag();
			return;
		}
	}

	public void dragExit(DropTargetEvent e){
		
	}
	@SuppressWarnings("rawtypes")
	public void drop(DropTargetDropEvent e){
		
		/*if(!isDropAcceptable(e)){
			e.rejectDrop();
			return;
		}*/
		e.acceptDrop(1);
		
		Transferable transférable = e.getTransferable();
		DataFlavor[] types = transférable.getTransferDataFlavors();
		
        for(DataFlavor type : types){
           try{
              if(type.equals(DataFlavor.javaFileListFlavor)){
            	  
                 Iterator iterator = ((List) transférable.getTransferData(type)).iterator();
                 File file = (File) iterator.next();
                 
                 if(getFileExtension(file).equals("pdf")){
                	 
                	// ACEPT
                	 
                 }else{
                	 e.rejectDrop();
                	 return;
                 }
                   
              }else if(type.equals(DataFlavor.stringFlavor)){
                 //String chaîne = (String) transférable.getTransferData(type);
              }
           }
           catch (Exception e1){ e1.printStackTrace(); }
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
