package cz.weissar.horolezeckypruvodce.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by petrw on 31.10.2015.
 */
public class Placemark implements IElement{

    private Folder folder;
    private String name;
    private EPlacemark type;
    private ArrayList<LatLng> coordinates;

    //nasledujici mohou byt null
    private String description;
    private Boolean confirmed;
    private int iconId;

    public Placemark(Folder folder, String name) {
        this.folder = folder;
        this.name = name;
        coordinates = new ArrayList<>();
    }


    //FUNCTIONS
    public EElement getElementType(){return EElement.PLACEMARK;}
    public void addCoordinate(LatLng coordinate){
        coordinates.add(coordinate);
    }
    public String getStringType(){
        switch (type){
            case POINT: return "Bod";
            case POLYGON: return "Oblast";
            case LINESTRING: return "Cesta";
        }
        return "Nespecifikováno :/";
    }
    @Override
    public String toString() {
        return name;
    }

    public void addCoordinates(String coordinateString){
        String[] coords3 = coordinateString.split(" ");
        for(String s : coords3){
            String[] coords = s.split(",");
            coordinates.add(new LatLng(Double.parseDouble(coords[1]),Double.parseDouble(coords[0])));
        }
    }

    //GETTERS & SETTERS

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EPlacemark getType() {
        return type;
    }

    public void setType(EPlacemark type) {
        this.type = type;
    }

    public ArrayList<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<LatLng> coordinates) {
        this.coordinates = coordinates;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public int getIconId() { return iconId; }

    public void setIconId(int iconId) { this.iconId = iconId; }


}
