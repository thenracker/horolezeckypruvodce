package cz.weissar.horolezeckypruvodce.data;

import java.util.ArrayList;

/**
 * Created by petrw on 31.10.2015.
 */
public class Document implements IElement {

    private String name;
    private String fileURL;
    private ArrayList<Folder> folders;

    public Document(String name, String fileURL){
        this.name = name;
        this.fileURL = fileURL;
        this.folders = new ArrayList<>();
    }

    //FUNCTIONS
    public EElement getElementType(){return EElement.DOCUMENT;}
    public void addFolder(Folder folder){
        folders.add(folder);
    }

    @Override
    public String toString() {
        return name;
    }
    //GETTERS & SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public ArrayList<Folder> getFolders() {
        return folders;
    }

    public void setFolders(ArrayList<Folder> folders) {
        this.folders = folders;
    }
}
