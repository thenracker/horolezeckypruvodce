package cz.weissar.horolezeckypruvodce.dijkstra;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import cz.weissar.horolezeckypruvodce.data.Placemark;

/**
 * Created by petrw on 07.12.2015.
 */
public class Road {

    private LatLng startPoint, endPoint;
    private double totalLength;
    private Placemark odkazNaPlacemark;
    private CrossRoad[] crossRoads; //velikost array by měla být dvě

    public Road(LatLng startPoint, LatLng endPoint, double totalLength, Placemark odkazNaPlacemark){
        crossRoads = new CrossRoad[2];
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.totalLength = totalLength;
        this.odkazNaPlacemark = odkazNaPlacemark;
    }

    //FUNKCE VRAŤ KŽIŽOVATKU Z DRUHÉ STRANY CESTY (ano určitě tam je)
    public CrossRoad getNextCrossroad(CrossRoad prvni){
        //vrátí druhý konec cesty (když zadáme první)
        if(crossRoads[0] != prvni) return crossRoads[0];
        else return crossRoads[1];
    }
    //PŘIDAT KŘIŽOVATKU
    public void addCrossroad(CrossRoad c){
        if(crossRoads[0] == null)crossRoads[0] = c;
        else crossRoads[1] = c;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }

    public double getTotalLength() {
        return totalLength;
    }

    public ArrayList<LatLng> getCoordinates(){ //odkaz nesmí být prázdný
        return odkazNaPlacemark.getCoordinates();
    }

    public void setTotalLength(double totalLength) {
        this.totalLength = totalLength;
    }

    public Placemark getOdkazNaPlacemark() {
        return odkazNaPlacemark;
    }

    public void setOdkazNaPlacemark(Placemark odkazNaPlacemark) {
        this.odkazNaPlacemark = odkazNaPlacemark;
    }
}
