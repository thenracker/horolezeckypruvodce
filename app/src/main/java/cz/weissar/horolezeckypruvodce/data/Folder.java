package cz.weissar.horolezeckypruvodce.data;

import java.util.ArrayList;

/**
 * Created by petrw on 31.10.2015.
 */
public class Folder implements IElement {

    private Document document;
    private String name;
    private ArrayList<Placemark> placemarks;

    public Folder(Document document, String name) {
        this.document = document;
        this.name = name;
        this.placemarks = new ArrayList<>();
    }

    //FUNCTIONS
    public EElement getElementType(){return EElement.FOLDER;}
    public void addPlacemark(Placemark placemark){
        placemarks.add(placemark);
    }

    @Override
    public String toString() {
        return name;
    }
    //GETTERS & SETTERS

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Placemark> getPlacemarks() {
        return placemarks;
    }

    public void setPlacemarks(ArrayList<Placemark> placemarks) {
        this.placemarks = placemarks;
    }
}
