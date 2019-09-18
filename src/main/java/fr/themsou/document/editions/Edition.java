package fr.themsou.document.editions;

import fr.themsou.document.Document;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.EditRender;
import fr.themsou.utils.Location;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Edition {

    private File file;
    private File editFile;

    public EditRender editRender;
    public Document document;

    public ArrayList<Element> elements = new ArrayList<>();

    public Edition(File file, Document document){
        this.document = document;
        this.file = file;
        this.editFile = getEditFile(file);
        load();
    }

    public void load(){

        this.editRender = new EditRender(this, document.rendered[0].getWidth(null), document.rendered[0].getHeight(null));
        new File(System.getProperty("user.home") + "/.PDFTeacher/").mkdirs();

        try{
            if(editFile.createNewFile()){ //file was created

            }else{ // file already exist
                DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(editFile)));


                while(reader.available() != 0){
                    byte elementType = reader.readByte();

                    switch (elementType){
                        case 1:
                            addElement(TextElement.readDataAndCreate(reader));
                        break;
                        case 2:
                            //elements.add(TextElement.readDataAndCreate(reader));
                        break;
                        case 3:
                            ///elements.add(TextElement.readDataAndCreate(reader));
                        break;
                    }
                }
                reader.close();
            }
        }catch (IOException e){ e.printStackTrace(); }
        //addElement(new TextElement(null, 0, new Font("Arial", Font.PLAIN, 70), "Très grosse erreur !", new Color(172, 51, 53)));
        //addElement(new TextElement(new Location(200, 200), 0, new Font("Arial", Font.PLAIN, 70), "Hey !", Color.BLACK));
        //addElement(new TextElement(null, 0, new Font("Arial", Font.PLAIN, 70), "Bonjour.", new Color(32, 158, 16)));

    }

    public void save(){

        try{
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(editFile, false)));

            for(int i = 0; i < elements.size(); i++){
                elements.get(i).writeData(writer);
            }

            writer.flush();
            writer.close();

        }catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void clear(){
        elements = new ArrayList<>();
    }

    public void addElement(Element element){

        if(element != null){

            if(element.getLocation() == null){
                element.setLocation(new Location(editRender.getWidth() / 2, editRender.getHeight() / 2));
            }
            elements.add(element);
            System.out.println("add " + ((TextElement)element).getContent());
        }
    }
    public void removeElement(Element element){

        if(element != null){

            elements.remove(element);
        }
    }

    public static File getEditFile(File file){

        return new File(System.getProperty("user.home") + "/.PDFTeacher/" +  file.getParentFile().getAbsolutePath().replace("/", "!E") + "!E" + file.getName() + ".edit");

    }


}
