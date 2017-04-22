package cz.weissar.horolezeckypruvodce.fragments;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.weissar.horolezeckypruvodce.MainActivity;
import cz.weissar.horolezeckypruvodce.R;
import cz.weissar.horolezeckypruvodce.data.Document;
import cz.weissar.horolezeckypruvodce.data.EPlacemark;
import cz.weissar.horolezeckypruvodce.data.Folder;
import cz.weissar.horolezeckypruvodce.data.Placemark;
import cz.weissar.horolezeckypruvodce.dijkstra.CrossRoad;
import cz.weissar.horolezeckypruvodce.dijkstra.DajkstrManager;

/**
 * Created by petrw on 17.10.2015.
 */
public class FragmentMap extends FragmentE { // implements OnMapReadyCallback{//implements GestureDetector.OnGestureListener{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "sectionNumber";
    private int viewXML = R.layout.fragment_map;
    private static int icon = R.drawable.ic_map_g;
    private static int iconActive = R.drawable.ic_map_b;
    private static int iconTitle = R.drawable.ic_map_w;
    private static String title = "Mapa";

    private Boolean maBytVysokaPresnost; //využíváno pro nastavení hodnoty dijkstra po úvodním nastavení

    //<editor-fold desc="ATRIBUTY">
    private MapView mapView;
    private GoogleMap map;
    private LatLng longClickLatLng;
    private DajkstrManager dajkstrManager;

    private List<Polyline> polylines;
    private List<Polygon> polygons;
    private List<Marker> markers;
    private List<Circle> circles;

    private Marker selectedMarker; //pro spouštění navigace
    private FloatingActionButton fabnavigace;
    private FloatingActionButton fabSetMyPositoin; //určení vlastní polohy

    private TileOverlayOptions tileOverlayOptions;
    private TileOverlay tileOverlay;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private boolean rotate;
    private float[] mGravity; //seznamy
    private float[] mGeomagnetic;
    private float lastBearing;

    int typMapy = 0; //využijeme default hodnoty

    private Location lastKnownLocation;
    private long lastLocationTime;
    //</editor-fold>

    public FragmentMap() {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, 4);
        setArguments(args);

        polygons = new ArrayList<>();
        polylines = new ArrayList<>();
        markers = new ArrayList<>();
        circles = new ArrayList<>();
    }


    public void initDijkstra(Document d){ //dostane RoadDocument, vytáhne z něj placemarky a v new Dajkstr.. se z toho vytvoří Roads a CrossRoads
        //CESTY
        ArrayList<Placemark> placemarks = new ArrayList<>();
        if(d != null)
            for(Folder folder : d.getFolders())
                for(Placemark p : folder.getPlacemarks())
                    placemarks.add(p);
        System.out.println(placemarks.size() + " = počet cest");
        dajkstrManager = new DajkstrManager(placemarks);
        if(maBytVysokaPresnost != null)
            dajkstrManager.setVysokaPresnost(maBytVysokaPresnost);
    }

    //<editor-fold desc="FUNKCE">
    public void setPresnostDijkstra(boolean b){
        if(dajkstrManager == null)
            maBytVysokaPresnost = b;
        else
            dajkstrManager.setVysokaPresnost(b);
    }

    private void navigateToMarker(Marker m){
        if(map.isMyLocationEnabled() || lastKnownLocation != null){ //&& m != null){

            Location l = map.getMyLocation();
            if(l == null){
                if(lastKnownLocation != null){
                    l = lastKnownLocation;
                    Toast.makeText(getContext(), "Navigace vychází z Vaší vybrané polohy (nemusí být přesná)", Toast.LENGTH_SHORT).show();
                }
            }

            if(l != null){
                clearMap();
                longClickLatLng = null; //aby po kliknutí nedošlo k smazání cesty

                //final protože se s nim pracuje v dalším vlákně
                final LatLng myLoc = new LatLng(l.getLatitude(), l.getLongitude());
                final Marker marker = m;
                //final ProgressDialog progressDialog = ProgressDialog.show(getMainActivity(), "", "Vyhledávám trasu...", true);

                //vyhledání cesty
                ArrayList<LatLng> path = dajkstrManager.shortestPathBetweenLatLngs(myLoc, marker.getPosition());
                //progressDialog.dismiss();

                //zobrazit marker s přepočítanou vzdáleností
                double delka = getCompleteDistance(path);

                MarkerOptions mo = new MarkerOptions();
                mo.position(marker.getPosition());
                mo.title(marker.getTitle());
                //mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_blue));
                mo.snippet("Pěšky: " + String.format(" %,.2f", delka) + " metrů");
                Marker mm = map.addMarker(mo);
                markers.add(mm);
                mm.showInfoWindow();

                //vykreslení hotové cesty
                //barva accentu, ale na půl průhledná
                int accent = getResources().getColor(R.color.colorAccent);
                int acc = Color.argb(125, Color.red(accent), Color.green(accent), Color.blue(accent));

                polylines.add(map.addPolyline(new PolylineOptions().color(acc).zIndex(3.0f).add(path.get(0)).add(path.get(1))));

                PolylineOptions pl = new PolylineOptions().color(accent).zIndex(3.0f);
                for(int i = 1; i < path.size()-1; i++){
                    pl.add(path.get(i));
                }
                polylines.add(map.addPolyline(pl));

                polylines.add(map.addPolyline(new PolylineOptions().color(acc).zIndex(3.0f).add(path.get(path.size()-2)).add(path.get(path.size()-1))));

                if(lastLocationTime != 0)
                    Toast.makeText(getContext(), "Poslední změřená poloha před: "+((System.currentTimeMillis()-lastLocationTime)/1000)+" vteřinami", Toast.LENGTH_SHORT).show();
                //TODO DO VLÁKNA!!!!

            }else{
                //není vidno kde jsme
                Toast.makeText(getContext(), "Nepodařilo se určit Vaši polohu, můžete ji určit sami pomoci nabídky v menu", Toast.LENGTH_SHORT).show();
            }
        }else{
            //není dostupná funkce vyhledání polohy
            Toast.makeText(getContext(), "Není aktivováno vyhledání Vaší polohy, můžete polohu určit manuálně pomocí možnosti v nabídce menu", Toast.LENGTH_LONG).show();
        }
    }

    private double getCompleteDistance(ArrayList<LatLng> al){
        double complet = 0.0;
        for(int i = 0; i < al.size()-1; i++){
            complet += 14.6*distanceBetweenLatLng(al.get(i),al.get(i+1));
        }
        return complet;
    }

    private double distanceBetweenLatLng(LatLng l1, LatLng l2){

        double latitude1 = l1.latitude;
        double longitude1 = l1.longitude;
        double latitude2 = l2.latitude;
        double longitude2 = l2.longitude;

        return 6371 * Math.acos(Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude2 - longitude1));

    }
    public int getMapType(){
        return (typMapy % 3);
    }
    public void setMapType(int type){
        //my číslo uložíme o jedničku menší a zavoláme switchMapType
        typMapy = (type);
    }

    private void initMapType(){
        switch(typMapy % 3){
            default:
                //TODO - smazat jen tileOverlay
            case 0: clearTileOverlay(); map.setMapType(GoogleMap.MAP_TYPE_TERRAIN); break; //Clear - aby smazal tileprovidera
            case 1: map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); break;
            case 2: if(getMainActivity().offlineMapExists())loadCustomMap(); else { Toast.makeText(getContext(), "Nebyly staženy offline mapy", Toast.LENGTH_SHORT).show(); switchMapType(); }break;
        }
    }

    public void switchMapType(){
        ++typMapy;
        switch(typMapy % 3){
            default:
                //TODO - smazat jen tileOverlay
            case 0: clearTileOverlay(); map.setMapType(GoogleMap.MAP_TYPE_TERRAIN); break; //Clear - aby smazal tileprovidera
            case 1: map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); break;
            case 2: if(getMainActivity().offlineMapExists())loadCustomMap(); else { Toast.makeText(getContext(), "Nebyly staženy offline mapy", Toast.LENGTH_SHORT).show(); switchMapType(); }break;
        }
        getMainActivity().saveSettings();
    }
    private String getTileFilename(int x, int y, int zoom){
        if(zoom > 18 || zoom < 11 || (x==0 && y==0 && zoom==0)) return "map-0.png";

        return "Landscape/"+zoom+"/"+x+"/"+y+".png";
    }
    public void loadCustomMap(){
        //https://www.openstreetmap.org/#map=14/50.5036/16.2736
        //TODO postahovat podklady a označit :)

        map.setMapType(GoogleMap.MAP_TYPE_NONE);

        if(tileOverlayOptions == null){
            TileProvider tileProvider = new TileProvider() {
                @Override
                public Tile getTile(int x, int y, int zoom) {

                    int TILE_WIDTH = 256;
                    int TILE_HEIGHT = 256;
                    int BUFFER_SIZE = 50 * 1024; //první číslo udává počet kB - logicky ;)
                    //obrázky mají od 40kB níž.. takže je třeba velkej bafr :(

                    int width = TILE_WIDTH; //width of the image in pixels
                    int height = TILE_HEIGHT; //height of the image in pixels
                    byte[] data = new byte[BUFFER_SIZE]; //a byte array containing the image data

                    try {

                        String fileName = getTileFilename(x, y, zoom);

                        InputStream is;
                        if(!Arrays.asList(getActivity().getExternalFilesDir("Landscape/"+zoom+"/"+x).list()).contains(y+".png")){
                            System.out.println("Chybí soubor: "+"Landscape/" + zoom + "/" + x + "/" + y + ".png");
                            fileName = getTileFilename(0,0,0); //vrátí jméno obrázku "není k dispozici"
                            //aby mi tam furt neskákal error ;)
                            is = getActivity().getAssets().open(fileName);
                        }else{
                            is = new FileInputStream(getActivity().getExternalFilesDir("Landscape/" + zoom + "/" + x + "/" + y + ".png"));
                        }

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                        int nRead;
                        while((nRead = is.read(data, 0, BUFFER_SIZE)) != -1){
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                    }

                    Tile tile = new Tile(width,height,data);

                    return tile;
                }
            };
            tileOverlayOptions = new TileOverlayOptions().tileProvider(tileProvider).zIndex(0);
        }
        tileOverlay = map.addTileOverlay(tileOverlayOptions);


    }
    public void navigaceToPlacemark(Placemark re){
        if(re != null && re.getType() == EPlacemark.POINT){
            getMainActivity().switchToFragmentMap();

            //clearMap();

            MarkerOptions m = new MarkerOptions()
                    .title(re.getName())
                    .snippet(re.getFolder().getName() + ": " + re.getStringType())
                    .position(re.getCoordinates().get(0));

            Marker mm = map.addMarker(m);
            markers.add(mm);

            mapView.getMap().animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mm.getPosition(), 13)));

            navigateToMarker(mm);

        }else{
            Toast.makeText(getContext(),"K tomuto objektu nelze navigovat", Toast.LENGTH_SHORT).show();
        }
    }

    public void setMyLocation(){ //TODO .. last known location nefunguje, páč jak se jednou zjistí gps, pořád se ukazuje poslední zjištěná
        //tedy TODO ten listener co zapisuje gps při změně je k hovnu
        clearMap();

        fabSetMyPositoin.startAnimation(AnimationUtils
                .loadAnimation(getContext(), R.anim.fade_in_transparent));
        fabSetMyPositoin.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), "Posuňte střed mapy na místo, kde se nacházíte a stiskněte tlačítko dole uprostřed", Toast.LENGTH_LONG).show();

        fabSetMyPositoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastKnownLocation = new Location("User choice");

                lastKnownLocation.setLatitude(map.getCameraPosition().target.latitude);
                lastKnownLocation.setLongitude(map.getCameraPosition().target.longitude);
                Toast.makeText(getContext(), "Poloha úspěšně nastavena, nyní bude od tohoto místa navigováno", Toast.LENGTH_SHORT).show();

                fabSetMyPositoin.startAnimation(AnimationUtils
                        .loadAnimation(getContext(), R.anim.fade_out_transparent));
            }
        });
    }

    public void putPlacemarkOnMap(Placemark re){
        putPlacemarkOnMap(re, true);
    }

    public void putPlacemarkOnMap(Placemark re, boolean clearMap){
        putPlacemarkOnMap(re, clearMap, R.color.colorAccent);
    }

    public void putPlacemarkOnMap(Placemark re, boolean clearMap, int color){

        if(clearMap) clearMap();

        getMainActivity().switchToFragmentMap();

        if(re.getType() == EPlacemark.POLYGON){
            PolygonOptions p = new PolygonOptions().fillColor(0x44000000).strokeColor(Color.BLACK)
                    .geodesic(true).strokeWidth(3.5f).zIndex(2.0f);
            for(LatLng l : re.getCoordinates())
                p.add(l);
            polygons.add(map.addPolygon(p)); //udržujeme si v seznamu

        }

        if(re.getType() == EPlacemark.LINESTRING){
            PolylineOptions p = new PolylineOptions().color(color).zIndex(3.0f);
            for(LatLng l : re.getCoordinates())
                p.add(l);
            polylines.add(map.addPolyline(p));
        }

        if(re.getType() == EPlacemark.POINT){
            MarkerOptions m = new MarkerOptions()
                    .title(re.getName())
                    .position(re.getCoordinates().get(0));

            if(map.getMyLocation() != null){
                LatLng me = new LatLng(map.getMyLocation().getLatitude(),map.getMyLocation().getLongitude());
                m.snippet("Vzduchem: " + String.format(" %,.2f", 14.6*distanceBetweenLatLng(m.getPosition(),me)) + " metrů");

            }else{
                m.snippet(re.getFolder().getName());
            }
            markers.add(map.addMarker(m));
            markers.get(markers.size()-1).showInfoWindow();

            selectedMarker = markers.get(markers.size()-1); //pro funkci navigace je třeba toto naplnit
            fabnavigace.startAnimation(AnimationUtils
                    .loadAnimation(getContext(), R.anim.fade_in_transparent));
            fabnavigace.setVisibility(View.VISIBLE);
        }

        LatLng coordinates = (re).getCoordinates().get((re.getCoordinates().size()-re.getCoordinates().size()%2)/2);
        mapView.getMap().animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(coordinates, 13)));
    }

    public void clearMap(){

        fabSetMyPositoin.setVisibility(View.GONE); //křehké

        if(fabnavigace.getVisibility() != View.GONE){
            fabnavigace
                    .startAnimation(AnimationUtils
                            .loadAnimation(getContext(), R.anim.fade_out_transparent));
            fabnavigace.setVisibility(View.GONE);
        }

        if((typMapy % 3) == 2){
            for(Marker m : markers) m.remove();
            for(Polyline p : polylines) p.remove();
            for(Polygon p : polygons) p.remove();
            for(Circle c : circles) c.remove();
            markers.clear(); polylines.clear(); polygons.clear(); circles.clear();
        }else{
            map.clear();
        }

    }

    private void clearTileOverlay(){
        if(tileOverlay != null)tileOverlay.remove();
    }

    public LatLng getUserLatLng(){
        if(map.isMyLocationEnabled()){
            if(map.getMyLocation() != null)
                return new LatLng(map.getMyLocation().getLatitude(),map.getMyLocation().getLongitude());
        }
        if(lastKnownLocation != null){
            return new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
        }
        return null;
    }

    public void fillAllWays(){
        clearMap();

        Document roadDocument = getMainActivity().getRoadDocument();
        if(roadDocument != null){
            for(Folder f : roadDocument.getFolders())
                for(Placemark p : f.getPlacemarks()){
                    if(p.getType() == EPlacemark.LINESTRING){
                        PolylineOptions pp = new PolylineOptions().color(0x44aa2200).zIndex(3.0f);
                        for(LatLng l : p.getCoordinates())
                            pp.add(l);
                        polylines.add(map.addPolyline(pp));
                    }
                }
        }
    }

    public void fillNearestPointPlacemarks(LatLng point, Double radius){
        for(Placemark p : getMainActivity().getFragmentSearch().getNearestRocks(point, radius)){
            markers.add(map.addMarker(new MarkerOptions().position(p.getCoordinates().get(0)).title(p.getName())
                    .snippet(p.getFolder().getName() + ": " + p.getConfirmed())));
        }
    }

    private void updateCamera(float bearing) {



        CameraPosition oldPos = map.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();

        CameraUpdate camUp = CameraUpdateFactory.newCameraPosition(pos);

        if(Math.abs(bearing - lastBearing)>5)
            map.moveCamera(camUp);
        else
            map.animateCamera(camUp);

    }
    public void rotateCamera(){
        if(!rotate){
            startUpdatingCamera();
        }
        else{
            stopUpdatingCamera();
        }
    }

    public void startUpdatingCamera(){
        if(mSensorManager != null && !rotate){ //TODO zbytečná podmínka??

            rotate = true;

            Sensor a = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor m = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            if(a != null && m != null){
                mSensorManager.registerListener(mSensorEventListener, a, SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(mSensorEventListener, m, SensorManager.SENSOR_DELAY_NORMAL);

                Toast.makeText(getContext(), "Režim otáčení mapy dle pohledu", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(), "Vaše zařízení tuto funkci nepodporuje", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void stopUpdatingCamera(){
        mSensorManager.unregisterListener(mSensorEventListener);

        rotate = false;

        CameraPosition oldPos = map.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(0).build();

        CameraUpdate camUp = CameraUpdateFactory.newCameraPosition(pos);

        map.animateCamera(camUp);

    }

    public boolean isRotate(){
        return rotate;
    }

    private void showRoadsAndCrossRoads(){
        fillAllWays();
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });

        for(CrossRoad c : dajkstrManager.getCrossRoads()){
            map.addMarker(new MarkerOptions().title(c.toString()).position(c.getPribliznaPozice())).setSnippet(c.getSousedniRoads().size()+"");
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return false;
                }
            });
        }
    }
    //</editor-fold>

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(viewXML, container, false);

        System.out.println("Začátek načítání view fragment Map");

        //<editor-fold defaultstate="collapsed" desc="nastaveni FloatingButtons">

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getMainActivity().switchFragmentView(MainActivity.FRAGMENT.STATS);
                getMainActivity().onBackPressed();
                rootView.findViewById(R.id.fab).startAnimation(AnimationUtils.loadAnimation(rootView.getContext(), R.anim.scale_pop));
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getMainActivity().switchFragmentView(MainActivity.FRAGMENT.SEARCH);
                rootView.findViewById(R.id.fab).startAnimation(AnimationUtils.loadAnimation(rootView.getContext(), R.anim.scale_pop));
                return true;
            }
        });

        fabnavigace = (FloatingActionButton) rootView.findViewById(R.id.fabnavigace);
        /*
        fabnavigace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(selectedMarker != null){
                    for(CrossRoad c : dajkstrManager.shortestPathPublic(new LatLng(map.getMyLocation().getLatitude(),map.getMyLocation().getLongitude()),selectedMarker.getPosition())){
                        map.addMarker(new MarkerOptions().position(c.getPribliznaPozice()).title(c.toString()).snippet("Počet cest připojených: "+c.getSousedniRoads().size()));
                    }
                }
                return false;
            }
        });*/
        fabnavigace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //showRoadsAndCrossRoads();

                try{
                    if(selectedMarker != null){
                        navigateToMarker(selectedMarker);
                    }
                }catch(Exception e){

                    Toast.makeText(getContext(), "Nepodařilo se vyhledat trasu", Toast.LENGTH_SHORT).show();
                }

            }
        });

        fabSetMyPositoin = (FloatingActionButton) rootView.findViewById(R.id.fabSetMyPosition);

        //</editor-fold>

        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        //mapView.onResume(); //needed to get the map to display immediately

        //<editor-fold desc="MapsInitializer - zakomentovaný - je třeba??">
        try{
            //TODO - je to třeba? O.o
            //MapsInitializer.initialize(getContext());
            //System.out.println("Inicializováno");
        }catch (Exception e){
            e.printStackTrace();
        }
        //</editor-fold>

        map = mapView.getMap();

        //zobrazit broumovský výběřek
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.52192460123418, 16.290493682026863), 13f));

        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);

        map.getUiSettings().setMapToolbarEnabled(false);

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //pokud je open street mapa
                //pokud je zoom < 11 nebo > 18 (není podpora open street mapy pro přiblížení)
                //pokud je pokud je zoom nezaokrouhlený - vypadá to jako sraní na open street mapě
                //pokud máme nějaké natočení a tilt
                if (typMapy % 3 == 2 && (cameraPosition.zoom != Math.round(cameraPosition.zoom) || cameraPosition.zoom < 11 || cameraPosition.zoom > 18))
                    mapView.getMap().animateCamera(
                            CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().
                                    target(cameraPosition.target).
                                    tilt(cameraPosition.tilt).
                                    bearing(cameraPosition.bearing).
                                    zoom(Math.max(Math.min(18, (float) Math.round(cameraPosition.zoom)), 11)).
                                    build()));
            }
        });
        map.setMyLocationEnabled(true);

        //SETTINGS NAHRÁLO NASTAVENÍ, TAK HO INITNEME BEZ UKLÁDÁNÍ
        initMapType();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (longClickLatLng != null) {
                    clearMap();
                    Double radius = Math.abs((distanceBetweenLatLng(latLng, longClickLatLng)));
                    circles.add(map.addCircle(new CircleOptions().center(longClickLatLng).
                            strokeColor(0x22ffffff).radius(new Double(radius * 18)).fillColor(0x44ffffff).zIndex(4f)));
                    fillNearestPointPlacemarks(longClickLatLng, radius);
                } else {
                    //Toast.makeText(getContext(), "Nejprve dlouze podržte na mapě", Toast.LENGTH_SHORT).show();
                }
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                clearMap();
                mapView.getMap().animateCamera(
                        CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().
                                target(latLng).
                                tilt(map.getCameraPosition().tilt).
                                bearing(map.getCameraPosition().bearing).
                                zoom(map.getCameraPosition().zoom).
                                build()));

                longClickLatLng = latLng;
                Double radius = 31.0d;
                radius = 400.0d / map.getCameraPosition().zoom;
                circles.add(map.addCircle(new CircleOptions().center(latLng).
                        strokeColor(0x22ffffff).radius(new Double(radius * 18)).fillColor(0x44ffffff).zIndex(4.0f)));
                fillNearestPointPlacemarks(latLng, radius);
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                //naplníme si po kliknutí placemark
                selectedMarker = marker; //lze

                if (!marker.getSnippet().toString().contains("Pěšky")) {
                    fabnavigace.startAnimation(AnimationUtils
                            .loadAnimation(rootView.getContext(), R.anim.fade_in_transparent));
                    fabnavigace.setVisibility(View.VISIBLE);
                }

                Placemark p = getMainActivity().getFragmentSearch().getPlacemarkByLatLng(marker.getPosition());

                if (map.getMyLocation() != null && !marker.getSnippet().contains("Pěšky")) { //pokud již není marker vytvořený s přesnou hodnotou při navigaci, změníme popisek
                    LatLng me = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
                    marker.setSnippet("Vzduchem: " + String.format(" %,.2f", 14.6 * distanceBetweenLatLng(marker.getPosition(), me)) + " metrů");
                } else {
                    if (!marker.getSnippet().contains("Pěšky"))
                        marker.setSnippet(p.getFolder().getName());
                }

                getMainActivity().getFragmentDetail().setElement(p);
                return false; //pokud nechceme baloon - return true
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Placemark p = getMainActivity().getFragmentSearch().getPlacemarkByLatLng(marker.getPosition());
                getMainActivity().getFragmentDetail().setElement(p);
                getMainActivity().switchToFragmentDetail();
            }
        });

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                lastLocationTime = System.currentTimeMillis();
            }
        });

        //NASTAVENÍ ROTACE DLE MAGNETOMETRU
        //LISTENERS //TODO přesunout do jiného vlákna? zbytečné ne?

        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    mGravity = event.values.clone();
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    mGeomagnetic = event.values.clone();

                if (mGravity != null && mGeomagnetic != null) {
                    float R[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);

                        float bearing = (float) Math.toDegrees(orientation[0]);

                        //System.out.println(bearing);

                        if(Math.abs(lastBearing - bearing) > 5.0d){ //jen pokud se opravdu posunulo

                            updateCamera(bearing);

                            lastBearing = bearing;
                        }
                    }
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        //SENSOR MANAGER PRO REGISTRACI A UNREGISTRACI
        mSensorManager = (SensorManager) getMainActivity().getSystemService(Context.SENSOR_SERVICE);

        //východzí hodnota - neotáčíme
        rotate = false;

        System.out.println("Konec načítání view fragment Map");

        return rootView;
    }

    public int getIcon() {
        return icon;
    }

    public int getIconActive(){
        return iconActive;
    }
    public int getIconTitle() {return iconTitle; }
    public String getTitle(){
        return title;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}