package cz.weissar.horolezeckypruvodce.dijkstra;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import cz.weissar.horolezeckypruvodce.data.EPlacemark;
import cz.weissar.horolezeckypruvodce.data.Placemark;

/**
 * Created by petrw on 07.12.2015.
 */
public class DajkstrManager {

    private ArrayList<Road> roads;
    private ArrayList<CrossRoad> crossRoads;
    private boolean vysokaPresnost = false; //TODO přepínatelné

    //KONSTRUKTOR DAJKSTR MANAGERA
    //PŘÍJDE MU SEZNAM GEOCEST
    //Z TĚCHTO OBJEKTY TYPU ROAD (ZAČÁTEČNÍ BOD, KONEČNÝ BOD, ODKAZ NA PLACEMARK, CELKOVÁ DÉLKA)
    //Z TĚCHTO ROADS PRO KAŽDÝ KONEČNÝ BOD VYHLEDÁ NEJBLIŽŠÍ BODY A VYTVOŘÍ CROSSROADS
    //- POZOR! MUSÍ SE KONTROLOVAT, JESTLI UŽ TAKOVÁ CROSSROAD NEEXISTUJE!!
    public DajkstrManager(ArrayList<Placemark> placemarks){
        //CREATE ROADS
        this.roads = makeRoads(placemarks);
        //CREATE CROSS ROADS
        this.crossRoads = makeCrossRoads(roads);
    }
/*
    public void showRoadsAndCrossRoads(GoogleMap map){
        List<CrossRoad> passed = new CopyOnWriteArrayList<>();
        List<Road> walked = new CopyOnWriteArrayList<>();
        passed.add(crossRoads.get(0));

        boolean zmena = true;
        while(zmena){
            zmena = false;
            System.out.println("Jedeme "+passed.size()+"/"+crossRoads.size());
            for(CrossRoad c : passed){
                for(Road r : c.getSousedniRoads()){
                    if(!walked.contains(r)){
                        walked.add(r);
                        zmena = true;
                    }
                    if(!passed.contains(r.getNextCrossroad(c))){
                        passed.add(r.getNextCrossroad(c));
                        zmena = true;
                    }
                }
            }
        }
        for(Road r : roads){
            if(!walked.contains(r)){
                PolylineOptions po = new PolylineOptions().color(Color.RED);
                for(LatLng l : r.getOdkazNaPlacemark().getCoordinates()){
                    po.add(l);
                }
                map.addPolyline(po);
            }

        }
    }
*/
    //DAJKSTR FUNCTIONS
/*
    public ArrayList<Road> vratNejkratsiCestu(Placemark myNearestWay, Placemark markerNearestWay){
        //vychozi krizovatky 4 kousky
        ArrayList<CrossRoad> startCrossroads = new ArrayList<>();
        ArrayList<CrossRoad> endCrossroads = new ArrayList<>();
        for(CrossRoad cr : crossRoads)  {
            //POKUD JE TO KOUSÍČEK, TAK TO JE TA NAŠE KŘIŽOVATKA - měly by být 4
            if(areNeighbors(cr.getPribliznaPozice(),myNearestWay.getCoordinates().get(0)) ||
                    areNeighbors(cr.getPribliznaPozice(),myNearestWay.getCoordinates().get(myNearestWay.getCoordinates().size()-1))){
                startCrossroads.add(cr);
            }
            if(areNeighbors(cr.getPribliznaPozice(),markerNearestWay.getCoordinates().get(0)) ||
                    areNeighbors(cr.getPribliznaPozice(),markerNearestWay.getCoordinates().get(markerNearestWay.getCoordinates().size()-1))){
                endCrossroads.add(cr);
            }
        }

        //TODO najit nejkratsi cestu - zapsat do seznamu roads
        double nejkratsiDelka = Double.MAX_VALUE; //mensi hodnota prepise tuto cestu
        ArrayList<Road> nejkratsiCesta = new ArrayList<>();

        int i = 0;
        for(CrossRoad cs : startCrossroads){
              for(CrossRoad ce: endCrossroads){
                  System.out.println("\nPokus č."+startCrossroads.indexOf(cs)+endCrossroads.indexOf(ce));
                  ArrayList<Road> cesta = shortestPath(cs,ce);
//                  System.out.println("Cesta má "+cesta.size()+" členů a délku "+delkaCesty(cesta));
                  //pokud jsme zatim jinou nenasli
                  // nebo
                  // pokud je kratší ALE ZÁROVEŇ se dostala do cíle
                  if(nejkratsiCesta.isEmpty() || (delkaCesty(cesta) < nejkratsiDelka && ce.getSousedniRoads().contains(cesta.get(cesta.size()-1))))
                      nejkratsiCesta = cesta;
              }
        }

        //přidáváme startovní a end cestu - s nimi se totiž nepočítá vlastně více méně?
        //nejkratsiCesta.add(getRoadByPlacemer(myNearestWay));
        //nejkratsiCesta.add(getRoadByPlacemer(markerNearestWay));

        return nejkratsiCesta;
    }

    private Road getRoadByPlacemer(Placemark p){
        for(Road r : roads){
            if(r.getOdkazNaPlacemark() == p)
                return r;
        }
        return null;
    }
*/
    public void setVysokaPresnost(boolean b){
        vysokaPresnost = b;
    }

    private double getCompleteDistance(ArrayList<CrossRoad> al){
        double complet = 0.0;

        for(int i = 0; i < al.size()-1; i++){
            complet += getRoadBetween(al.get(i),al.get(i).getPredchozi()).getTotalLength();
        }

        return complet;
    }

    //<editor-fold desc="DIJKSTRŮV ALGORITMUS první a druhá metoda celkového výpočtu + podpůrné metody">

    //<editor-fold desc="METODA 1. - nalezneme od LatLng nejbližší křižovatky (celkem 4 varianty > najdeme nejkratší) - vracíme hotový seznam LatLng na vykreslení">
    public ArrayList<LatLng> shortestPathBetweenLatLngs(LatLng myLoc, LatLng markerLoc){

        /*TESTOVÁNÍ - Z POLICKÉ STĚNY TO PADALO*/
        //myLoc = new LatLng( 50.5345125, 16.2323142);
        //TODO nezprovoznil jsem - alespoň přidána výjimka

        ArrayList<LatLng> cestaLatLngs = new ArrayList<>();

        //<editor-fold desc="NALEZENÍ NEJBLIŽŠÍCH CEST A JEJICH BODŮ">
        double myLocDist = Double.MAX_VALUE;
        LatLng myLocNearestLatLng = roads.get(0).getStartPoint(); //BTW dáme nějaké hodnoty kvůli mightNotBeenInitialized (stejně se to přepíše)
        Road myLocNearestRoad = roads.get(0);

        double markerDist = Double.MAX_VALUE;
        LatLng markerLocNearestLatLng = roads.get(0).getStartPoint(); //BTW dáme nějaké hodnoty kvůli mightNotBeenInitialized (stejně se to přepíše)
        Road markerLocNearestRoad = roads.get(0);

        for(Road r : roads){
            for(LatLng l : r.getCoordinates()){
                double d = distanceBetweenLatLng(l,myLoc);
                if(d < myLocDist){
                    myLocNearestLatLng = l;
                    myLocNearestRoad = r;
                    myLocDist = d;
                }
                double e = distanceBetweenLatLng(l,markerLoc);
                if(e < markerDist){
                    markerLocNearestLatLng = l;
                    markerLocNearestRoad = r;
                    markerDist = e;
                }
            }
        }
        //NALEZENY NEJBLIŽŠÍ CESTY A NEJBLIŽŠÍ BODY V TĚCHTO CESTÁCH
        //</editor-fold>

        //<editor-fold desc="VÝBĚR KŘIŽOVATEK (DLE ZVOLENÉ PŘESNOSTI BUĎ NEJBLIŽŠÍ DVĚ NEBO VÝPOČET 4 VARIANT">
        CrossRoad myLocC1 = myLocNearestRoad.getNextCrossroad(null);
        CrossRoad myLocC2 = myLocNearestRoad.getNextCrossroad(myLocC1);

        CrossRoad markerLocC1 = markerLocNearestRoad.getNextCrossroad(null);
        CrossRoad markerLocC2 = markerLocNearestRoad.getNextCrossroad(markerLocC1);

        CrossRoad shortestMyLocC = myLocC1;
        CrossRoad shortestMarkerC = markerLocC1; //začáteční, cílová křižovatka

        ArrayList<CrossRoad> shortestRoadByCrossroads;// = new ArrayList<>(); //výsledná cesta

        if(vysokaPresnost){

            shortestRoadByCrossroads = new ArrayList<>();

            ArrayList<CrossRoad> road1 = shortestPath(myLocC1,markerLocC1);
            ArrayList<CrossRoad> road2 = shortestPath(myLocC1,markerLocC2);
            ArrayList<CrossRoad> road3 = shortestPath(myLocC2,markerLocC1);
            ArrayList<CrossRoad> road4 = shortestPath(myLocC2,markerLocC2);

            double d1 = getCompleteDistance(road1);
            double d2 = getCompleteDistance(road2);
            double d3 = getCompleteDistance(road3);
            double d4 = getCompleteDistance(road4);

            double d = Double.MAX_VALUE;

            //nejkratší cesta
            if(d1 < d){
                shortestRoadByCrossroads = road1;
                d = d1;
                //shortestMyLocC = myLocC1; shortestMarkerC = markerLocC1; //HODNOTY ZE KTERÝCH SE KURVA VYCHÁZÍ..
            }
            if(d2 < d){
                shortestRoadByCrossroads = road2;
                d = d2;
                shortestMyLocC = myLocC1; shortestMarkerC = markerLocC2;
            }
            if(d3 < d){
                shortestRoadByCrossroads = road3;
                d = d3;
                shortestMyLocC = myLocC2; shortestMarkerC = markerLocC1;
            }
            if(d4 < d){
                shortestRoadByCrossroads = road4;
                shortestMyLocC = myLocC2; shortestMarkerC = markerLocC2;
            }

        }
        else{
            double d = distanceBetweenLatLng(myLocC1.getPribliznaPozice(),markerLocC1.getPribliznaPozice());

            double e = distanceBetweenLatLng(myLocC2.getPribliznaPozice(),markerLocC1.getPribliznaPozice());
            if(e < d){ d = e; shortestMyLocC = myLocC2; shortestMarkerC = markerLocC1; }

            double f = distanceBetweenLatLng(myLocC1.getPribliznaPozice(),markerLocC2.getPribliznaPozice());
            if(f < d){ d = f; shortestMyLocC = myLocC1; shortestMarkerC = markerLocC2; }

            double g = distanceBetweenLatLng(myLocC2.getPribliznaPozice(),markerLocC2.getPribliznaPozice());
            if(g < d){ shortestMyLocC = myLocC2; shortestMarkerC = markerLocC2; }

            //cílové křižovatky k začáteční
            shortestRoadByCrossroads = shortestPath(shortestMyLocC, shortestMarkerC);
        }
        //</editor-fold>

        //<editor-fold desc="PŘEVOD CEST NA SEZNAM LATLNG VČETNĚ LATLNGS MEZI KŘIŽOVATKOU A NEJBLIŽŠÍM MÍSTEM V CESTĚ">
        for(CrossRoad c : shortestRoadByCrossroads){ //od cílové křižovatky
            if(c.getPredchozi() != null){
                //mezi každou křižovatkou přidáváme latlngs ve správném pořadí
                Road r = getRoadBetween(c,c.getPredchozi());
                if(areNeighbors(r.getStartPoint(),c.getPribliznaPozice())){
                    for(int i = 0; i < r.getCoordinates().size(); i++){
                        cestaLatLngs.add(r.getCoordinates().get(i));
                    }
                }
                else{ //je orientovaná opačně
                    for(int i = r.getCoordinates().size()-1; i >= 0; i--){
                        cestaLatLngs.add(r.getCoordinates().get(i));
                    }
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="DOPLNĚNÍ LATLNGS MEZI MARKEREM, JEHO CESTOU KE KŘIŽOVATCE + PŘIDÁNÍ PŘEDCHOZÍHO SEZNAMU + TO SAMÉ S CÍLEM">

        ArrayList<LatLng> hotovaCestaLatLngs = new ArrayList<>();

        //TODO NEZJISTI, JESTLI TAM MARKER BYL - POČÍTÁ TO Z DRUHÉHO KONCE JAKOBY SE NECHUMELILO
        //POZITIVNÍ JE TODO, ŽE TO JE OPRAVDU JENOM TÍM DOKRESLOVÁNÍM ASI
        //KŘIŽOVATKY TO HLEDÁ NEJSPÍŠ DOBŘE
        //TAKŽE JEN NĚJAK SPRÁVNĚ URČIT NA KTEROU STRANU SE TO POČÍTÁ.. yes..

        //TODO PROSTĚ NÍŽE UVEDENÉ METODY ODMÍTAJÍ SPOLUPRACOVAT...
        //NOT TODO - už jsem pořešil :) jen jsem si nepřepisoval ty křižovatky když byl režim vysoké přesnosti <3
        //Moje chyba :) ale už to jede

        //LatLngs mezi markerem a jeho cestou k cílové křižovatce
        hotovaCestaLatLngs.add(markerLoc);
        boolean markerTamByl = cestaLatLngs.contains(markerLocNearestLatLng);
        /* A ONO se to už asi dělá někde vevnitř -_- TODO wtf?*/
        if(!markerTamByl){
            if(areNeighbors(shortestMarkerC.getPribliznaPozice(), markerLocNearestRoad.getStartPoint())){ //je to ta část cesty k startPointu
                for(int i = markerLocNearestRoad.getCoordinates().indexOf(markerLocNearestLatLng); i >= 0; i--){
                    hotovaCestaLatLngs.add(markerLocNearestRoad.getCoordinates().get(i));
                }
                //IF(VYSOKApRESNOST) A CVIČNÁ SKÁLA - TAK TO KRESLÍ OPAČNĚ.. JAKTO KURVA? - TODO SOLVED - NEPŘEPISOVAL JSEM VÝCHOZÍ KŘIŽOVATKY
                /*
                for(int i = 0; i < markerLocNearestRoad.getCoordinates().indexOf(markerLocNearestLatLng); i++){
                    hotovaCestaLatLngs.add(markerLocNearestRoad.getCoordinates().get(i));
                }*/
            }
            else if(areNeighbors(shortestMarkerC.getPribliznaPozice(), markerLocNearestRoad.getEndPoint())){ //je to ta část cesty k endPointu

                for(int i = markerLocNearestRoad.getCoordinates().indexOf(markerLocNearestLatLng); i < markerLocNearestRoad.getCoordinates().size(); i++){
                    hotovaCestaLatLngs.add(markerLocNearestRoad.getCoordinates().get(i));
                }/*
                for(int i = markerLocNearestRoad.getCoordinates().size()-1; i > markerLocNearestRoad.getCoordinates().indexOf(markerLocNearestLatLng); i--){
                    hotovaCestaLatLngs.add(markerLocNearestRoad.getCoordinates().get(i));
                }*/
            }
        }

        /**/
        //LatLngs cesty (jen přisypání předchozího seznamu) - aniž by se jakoby přetáhlo na oboje strany

        boolean myLocTamByl = false;
        int j = 0;
        if(cestaLatLngs.contains(markerLocNearestLatLng)){ j = cestaLatLngs.indexOf(markerLocNearestLatLng); myLocTamByl = true; }
        for(int i = j; i < cestaLatLngs.size(); i++){
            hotovaCestaLatLngs.add(cestaLatLngs.get(i)); //přidáme všechny (pokud už tam náš index je, zaznamenáme si to a přidáme jen od něj dál)
            if(cestaLatLngs.get(i) == myLocNearestLatLng){
                myLocTamByl = true;
                break;
            }
        }

        //LatLngs mezi cílem, jeho cestou a k startovní křižovatce
        /* A ONO se to už asi dělá někde vevnitř -_- TODO*/

        //TODO - tadyto bude chtít odladit aby to bylo zevnitř skal testovaný..
        //TODO - ty cykly budou určitě jinačí ;) ... někde to musí být snad i-- ne?
        if(!myLocTamByl){
            if(areNeighbors(shortestMyLocC.getPribliznaPozice(), myLocNearestRoad.getStartPoint())){
/*
                for(int i = 0; i < myLocNearestRoad.getCoordinates().indexOf(myLocNearestLatLng); i++){
                    hotovaCestaLatLngs.add(myLocNearestRoad.getCoordinates().get(i));
                }
                for(int i = myLocNearestRoad.getCoordinates().indexOf(myLocNearestLatLng); i < myLocNearestRoad.getCoordinates().size(); i++){
                    hotovaCestaLatLngs.add(myLocNearestRoad.getCoordinates().get(i));
                }

                for(int i = myLocNearestRoad.getCoordinates().indexOf(myLocNearestLatLng); i >= 0; i--){
                    hotovaCestaLatLngs.add(myLocNearestRoad.getCoordinates().get(i));
                }*/
                //TODO - nejspíš zde bude cyklus i--
                for(int i = myLocNearestRoad.getCoordinates().indexOf(myLocNearestLatLng); i < myLocNearestRoad.getCoordinates().size(); i++){
                    hotovaCestaLatLngs.add(myLocNearestRoad.getCoordinates().get(i));
                }

            }else if(areNeighbors(shortestMyLocC.getPribliznaPozice(), myLocNearestRoad.getEndPoint())){
                for(int i = 0; i < myLocNearestRoad.getCoordinates().indexOf(myLocNearestLatLng); i++){
                    hotovaCestaLatLngs.add(myLocNearestRoad.getCoordinates().get(i));
                }
                /*
                for(int i = myLocNearestRoad.getCoordinates().indexOf(myLocNearestLatLng); i < myLocNearestRoad.getCoordinates().size(); i++){
                    hotovaCestaLatLngs.add(myLocNearestRoad.getCoordinates().get(i));
                }*/
            }
        }

        /**/
        hotovaCestaLatLngs.add(myLoc);
        //KOMPLET :) vracíme

        return hotovaCestaLatLngs;
        //</editor-fold>

    }
    //</editor-fold>

    //<editor-fold desc="METODA 2. - nalzeneme nejkratší cestu mezi křižovatkami (resp je to seznam uzlů (křižovatek))">
    //TODO MOŽNO PŘEDĚLAT NA PUBLIC A VYZKOUŠET SI JI VYKRESLOVAT ?
    @Deprecated
    public ArrayList<CrossRoad> shortestPathPublic(LatLng myLoc, LatLng markerLoc){
        //POSTUP NALEZENÍ NEJBLIŽŠÍCH KŘIŽOVATEK - DLE VZDUŠENÉ VZDÁLENOSTI JEN TESTÍK
        double myLocDist = Double.MAX_VALUE;
        Road myLocNearestRoad = roads.get(0);

        double markerDist = Double.MAX_VALUE;
        Road markerLocNearestRoad = roads.get(0);

        for(Road r : roads){
            for(LatLng l : r.getCoordinates()){
                double d = distanceBetweenLatLng(l,myLoc);
                if(d < myLocDist){
                    myLocNearestRoad = r;
                    myLocDist = d;
                }
                double e = distanceBetweenLatLng(l,markerLoc);
                if(e < markerDist){
                    markerLocNearestRoad = r;
                    markerDist = e;
                }
            }
        }
        CrossRoad myLocC1 = myLocNearestRoad.getNextCrossroad(null);
        CrossRoad myLocC2 = myLocNearestRoad.getNextCrossroad(myLocC1);

        CrossRoad markerLocC1 = markerLocNearestRoad.getNextCrossroad(null);
        CrossRoad markerLocC2 = markerLocNearestRoad.getNextCrossroad(markerLocC1);

        CrossRoad shortestMyLocC = myLocC1, shortestMarkerC = markerLocC1; //začáteční, cílová křižovatka
        double d = distanceBetweenLatLng(myLocC1.getPribliznaPozice(),markerLocC1.getPribliznaPozice());

        double e = distanceBetweenLatLng(myLocC2.getPribliznaPozice(),markerLocC1.getPribliznaPozice());
        if(e < d){ d = e; shortestMyLocC = myLocC2; shortestMarkerC = markerLocC1;}

        double f = distanceBetweenLatLng(myLocC1.getPribliznaPozice(),markerLocC2.getPribliznaPozice());
        if(f < d){ d = f; shortestMyLocC = myLocC1; shortestMarkerC = markerLocC2;}

        double g = distanceBetweenLatLng(myLocC2.getPribliznaPozice(),markerLocC2.getPribliznaPozice());
        if(g < d){ shortestMyLocC = myLocC2; shortestMarkerC = markerLocC2;}


        if(vysokaPresnost){
            //POKUD VYSOKA PŘESNOST, TAK TO SPOČÍTÁ ZNOVU A VRÁTÍ NEJKRATŠÍ CESTU DLE TAMTOHO
            ArrayList<CrossRoad> shortestRoadByCrossroads = new ArrayList<>();

            ArrayList<CrossRoad> road1 = shortestPath(myLocC1,markerLocC1);
            ArrayList<CrossRoad> road2 = shortestPath(myLocC1,markerLocC2);
            ArrayList<CrossRoad> road3 = shortestPath(myLocC2,markerLocC1);
            ArrayList<CrossRoad> road4 = shortestPath(myLocC2,markerLocC2);

            double d1 = getCompleteDistance(road1);
            double d2 = getCompleteDistance(road2);
            double d3 = getCompleteDistance(road3);
            double d4 = getCompleteDistance(road4);

            d = Double.MAX_VALUE;

            //nejkratší cesta
            if(d1 < d){
                shortestRoadByCrossroads = road1;
                d = d1;
            }
            if(d2 < d){
                shortestRoadByCrossroads = road2;
                d = d2;
            }
            if(d3 < d){
                shortestRoadByCrossroads = road3;
                d = d3;
            }
            if(d4 < d){
                shortestRoadByCrossroads = road4;
            }
            return shortestRoadByCrossroads; //A VRÁTÍ SPRÁVNÝ SEZNAM
        }


        return shortestPath(shortestMyLocC,shortestMarkerC);
    }


    private ArrayList<CrossRoad> shortestPath(CrossRoad start, CrossRoad cil){

        ArrayList<CrossRoad> path = new ArrayList<>();
        start.setVzdalenost(0.0);
        path.add(start); //přidáváme startovní uzel

        ArrayList<Road> usedRoads = new ArrayList<>(); //abychom cesty neopakovali

        while(true && usedRoads.size() != roads.size()){
            CrossRoad next = null;
            for(CrossRoad c : path){
                //NAJDEME NEJKRATŠÍ CESTU Z KŘIŽOVATKY
                //PO TÉTO CESTĚ SI NAČTEME POTENCIONÁLNÍ UZEL - která zatím nebyla použita
                Road potencionalniCesta = c.getPossibleRoad(path); //vrátí nejkratší cestu, která však zatím nebyla použita

                if(potencionalniCesta != null){// && !usedRoads.contains(potencionalniCesta)){

                    CrossRoad potencionalni = potencionalniCesta.getNextCrossroad(c);

                    potencionalni.setVzdalenost(c.getVzdalenost() + potencionalniCesta.getTotalLength());
                    potencionalni.setPredchozi(c);

                    //POKUD ZATÍM ŽÁDNÁ NENÍ, NEBO POKUD JSME NAŠLI KRATŠÍ, ALE ZÁROVEŇ TAM UŽ NENÍ
                    if((next == null || next.getVzdalenost() > potencionalni.getVzdalenost())) {
                        next = potencionalni;
                        usedRoads.add(potencionalniCesta); //už ji nechceme znovu využívat
                    }
                }
            }
            //nějakou jsme určo našli - tu přidáme
            if(next != null && !path.contains(next)){
                path.add(next);
            }

            //JE POSLEDNÍ UZEL V PATH TEN CÍLOVÝ? ANO? BREAK!
            if(path.get(path.size()-1) == cil) break; //nalezeno
        }

        ArrayList<CrossRoad> posloupnostKrizovatek = new ArrayList<>();
        CrossRoad nynejsi;// = path.get(path.size()-1); //začneme od poslední
        nynejsi = cil;
        posloupnostKrizovatek.add(cil);

        while(nynejsi != start && nynejsi != null){
            posloupnostKrizovatek.add(nynejsi.getPredchozi());
            nynejsi = nynejsi.getPredchozi();
        }

        return posloupnostKrizovatek;
    }
    //</editor-fold>

    //<editor-fold desc="METODA vracející cestu mezi křitovatkami">
    private Road getRoadBetween(CrossRoad c1, CrossRoad c2){
        Road r = null;
        if(c1 != null && c2 != null)
        for(Road rr : c1.getSousedniRoads()){
            if(c2.getSousedniRoads().contains(rr))
                r = rr;
        }
        return r;
    }
    //</editor-fold>

    //<editor-fold desc="METODA vracející délku seznamu cest">
    private double delkaCesty(ArrayList<Road> roads){
        double vzdalenost = 0.0;
        for(Road r : roads){
            vzdalenost += r.getTotalLength();
        }
        return vzdalenost;
    }
    //</editor-fold>

    //<editor-fold desc="METODA vracející zda-li jsou dvoje LatLng sousedi (tolerance 10 metrů)">
    private boolean areNeighbors(LatLng l1, LatLng l2){

        return distanceBetweenLatLng(l1,l2) < 10.0; //10 metrů? stačí to?
    }
    //</editor-fold>

    //<editor-fold desc="METODA vracející přesnou vzdálenost mezi dvěma LatLng">
    private double distanceBetweenLatLng(LatLng l1, LatLng l2){

        double latitude1 = l1.latitude;
        double longitude1 = l1.longitude;
        double latitude2 = l2.latitude;
        double longitude2 = l2.longitude;

        if(latitude1 == latitude2 && longitude1 == longitude2)return 0;

        return 14.6 * 6371 * Math.acos(Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude2 - longitude1));

    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="GET & SET">
    public ArrayList<Road> getRoads() {
        return roads;
    }

    public void setRoads(ArrayList<Road> roads) {
        this.roads = roads;
    }

    public ArrayList<CrossRoad> getCrossRoads() {
        return crossRoads;
    }

    public void setCrossRoads(ArrayList<CrossRoad> crossRoads) {
        this.crossRoads = crossRoads;
    }
    //</editor-fold>

    //<editor-fold desc="INIT FUNCTIONS makeRoads, makeCrossRoads">
    private ArrayList<Road> makeRoads(ArrayList<Placemark> placemarks){
        ArrayList<Road> roadss = new ArrayList<>();
        //VYTVOŘENÍ ROADS
        for(Placemark p : placemarks){
            if(p.getType() == EPlacemark.LINESTRING){//pojistka aby tam nešly hovna ;)
                //VÝPOČET CELKOVÉ VZDÁLENOSTI
                double totalLength = 0;
                for(int i = 0; i < p.getCoordinates().size()-1; i++){
                    totalLength += distanceBetweenLatLng(p.getCoordinates().get(i), p.getCoordinates().get(i+1)); //pro každý bod vypočteme vzdálenost
                }
                //VYTVOŘENÍ CESTY
                roadss.add(new Road(p.getCoordinates().get(0), p.getCoordinates().get(p.getCoordinates().size()-1),totalLength,p));
            }
        }
        return roadss;
    }
    private ArrayList<CrossRoad> makeCrossRoads(ArrayList<Road> roadss){
        ArrayList<CrossRoad> crossRoadss = new ArrayList<>();

        //POMOCNÁ TŘÍDA MYLATLNG
        class MyLatLng{
            LatLng latlng;
            Road road;
            MyLatLng(LatLng latlng, Road road){
                this.latlng = latlng;
                this.road = road;
            }
        }

        //VYTVOŘENÍ VLASTNÍCH LATLNG S CESTOU
        ArrayList<MyLatLng> myLatLngs = new ArrayList<>();
        for(Road r : roadss){
            myLatLngs.add(new MyLatLng(r.getStartPoint(),r));
            myLatLngs.add(new MyLatLng(r.getEndPoint(), r));
        }

        //PROJETÍ MYLATLNGS A TVORBA KŘIŽOVATEK
        ArrayList<MyLatLng> pouziteLatLngs = new ArrayList<>();//seznam již použitých latlngs

        for(int i = 0; i < myLatLngs.size(); i++){
            if(!pouziteLatLngs.contains(myLatLngs.get(i))){ //pokud jiz neni v nejake krizovatce zapsána
                CrossRoad cr = new CrossRoad();
                pouziteLatLngs.add(myLatLngs.get(i));
                cr.addSousedniRoad(myLatLngs.get(i).road);
                cr.setPribliznaPozice(myLatLngs.get(i).latlng); //ulozime i pribliznou pozici
                for(int k = i+1; k < myLatLngs.size(); k++){
                    //if(!pouziteLatLngs.contains(myLatLngs.get(k)))
                    if(areNeighbors(myLatLngs.get(i).latlng,myLatLngs.get(k).latlng)){
                        cr.addSousedniRoad(myLatLngs.get(k).road); //pokud sousedí, cestu přidáme
                        pouziteLatLngs.add(myLatLngs.get(k));
                    }
                }
                crossRoadss.add(cr); //křižovatku uložíme
            }
        }

        //NAPLNÍME REFERENCE DO CEST
        for(CrossRoad c : crossRoadss){
            for(Road r : c.getSousedniRoads()){
                r.addCrossroad(c);
            }
        }

        return crossRoadss;
    }
    //</editor-fold>


}
