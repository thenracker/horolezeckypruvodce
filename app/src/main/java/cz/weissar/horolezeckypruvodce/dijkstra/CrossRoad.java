package cz.weissar.horolezeckypruvodce.dijkstra;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by petrw on 07.12.2015.
 */
public class CrossRoad {

    private ArrayList<Road> sousedniRoads;
    private LatLng pribliznaPozice;

    private double vzdalenost; //pro výpočet dje
    private CrossRoad predchozi;
    private int poradi;

    public CrossRoad(){
        sousedniRoads = new ArrayList<>();
        poradi = -1;
        vzdalenost = 0;
    }
    //FUNKCE NEJKRATŠÍ CESTA
    public Road shortestRoad(ArrayList<Road> usedRoads){

        Collections.sort(sousedniRoads, new Comparator<Road>() {
            @Override
            public int compare(Road lhs, Road rhs) {
                return Double.compare(lhs.getTotalLength(), rhs.getTotalLength()); //return (lhs.getTotalLength()<rhs.getTotalLength())?0:1;
            }
        });
        for(Road r : sousedniRoads){
            if(!usedRoads.contains(r))
                return r;
        }
        return null; //nyní máme seřazeno od nejkratší po nejdelší
    }

    public Road getPossibleRoad(ArrayList<CrossRoad> usedCross){
        Road road = null;
        for(Road r : sousedniRoads){
            if(!usedCross.contains(r.getNextCrossroad(this))){
                if(road == null || road.getTotalLength() > r.getTotalLength())
                    road = r;
            }
            else{
                //KDYŽ OBSAHUJE, TAK ZMĚNÍME CESTU A DÉLKU?
                if(r.getNextCrossroad(this).getVzdalenost() > getVzdalenost()+r.getTotalLength()){
                    road = r;
                    r.getNextCrossroad(this).setPredchozi(this);
                    r.getNextCrossroad(this).setVzdalenost(getVzdalenost()+r.getTotalLength());
                }
            }
        }
        return road;
    }

    private void sortCollections(){
        Collections.sort(sousedniRoads, new Comparator<Road>() {
            @Override
            public int compare(Road lhs, Road rhs) {
                return Double.compare(lhs.getTotalLength(), rhs.getTotalLength()); //return (lhs.getTotalLength()<rhs.getTotalLength())?0:1;
            }
        });
    }

    public void addSousedniRoad(Road r){
        sousedniRoads.add(r);
        r.addCrossroad(this); //a přidáme k cestě křižovatku
        sortCollections(); //a seřadíme dle nejkratší dříve
    }

    public double getVzdalenost() {
        return vzdalenost;
    }

    public void setVzdalenost(double vzdalenost) {
        this.vzdalenost = vzdalenost;
    }

    public CrossRoad getPredchozi() {
        return predchozi;
    }

    public void setPredchozi(CrossRoad predchozi) {
        this.predchozi = predchozi;
    }

    public ArrayList<Road> getSousedniRoads() {
        return sousedniRoads;
    }

    public void setSousedniRoads(ArrayList<Road> sousedniRoads) {
        this.sousedniRoads = sousedniRoads;
    }

    public LatLng getPribliznaPozice() {
        return pribliznaPozice;
    }

    public void setPribliznaPozice(LatLng pribliznaPozice) {
        this.pribliznaPozice = pribliznaPozice;
    }
}
