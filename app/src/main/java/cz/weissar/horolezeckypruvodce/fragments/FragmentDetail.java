package cz.weissar.horolezeckypruvodce.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import cz.weissar.horolezeckypruvodce.R;
import cz.weissar.horolezeckypruvodce.data.EPlacemark;
import cz.weissar.horolezeckypruvodce.data.Placemark;

/**
 * Created by petrw on 17.10.2015.
 */
public class FragmentDetail extends FragmentE {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "sectionNumber";
    private int viewXML = R.layout.fragment_detail;
    private static int icon = R.drawable.ic_target_g;
    private static int iconActive = R.drawable.ic_target_b;
    private static int iconTitle = R.drawable.ic_target_w;
    private static String title = "Detail";

    //UI elementy pro zobrazení detailu zde
    private TextView txtNadpis;
    private TextView txtTyp; //TODO propojit s obrázkem
    private TextView txtOstatni; //velikost, výška, obtížnost
    private ImageView imageView;

    private Button btnChciVylezt;
    private Button btnVylezeno;
    private Button btnMapa;
    //private ListView lstSeznam;
    private ArrayAdapter<Placemark> lstAdapter; //spravuje obsah listview
    private ArrayList<Placemark> lstArray; //je ovládán result adapterem

    //a samotný rockElement :)
    private Placemark element;
    private ArrayList<Placemark> historyElements;

    public FragmentDetail() {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, 2);
        setArguments(args);

    }

    private double distanceBetweenLatLng(LatLng l1, LatLng l2){

        double latitude1 = l1.latitude;
        double longitude1 = l1.longitude;
        double latitude2 = l2.latitude;
        double longitude2 = l2.longitude;

        return 93220 * Math.acos(Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude2 - longitude1));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO přidat metodu mrnkiDoPaměti telefonu a najdi poslední prohlížený objekt
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(viewXML, container, false);

        System.out.println("Začátek načítání view fragment Detail");

        //<editor-fold defaultstate="collapsed" desc="Nastavení listu a naplnění proměnných (INIT)">

        //<editor-fold desc="nastaveni listu a naplnění proměnných">
        lstArray = new ArrayList<>();
        lstAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_activated_1, lstArray);
        ((ListView)rootView.findViewById(R.id.lstSeznam)).setAdapter(lstAdapter);

        historyElements = new ArrayList<>();

        txtNadpis = (TextView)rootView.findViewById(R.id.txtNadpis);
        txtTyp = (TextView)rootView.findViewById(R.id.txtTyp);
        txtOstatni = (TextView)rootView.findViewById(R.id.txtOstatni);
        imageView = (ImageView) rootView.findViewById(R.id.imageView);

        btnChciVylezt = (Button)rootView.findViewById(R.id.btnChciVylezt);
        btnVylezeno = (Button)rootView.findViewById(R.id.btnVylezeno);
        btnMapa = (Button)rootView.findViewById(R.id.btnMapa);
        //</editor-fold>

        //nastavení list click
        ((ListView)rootView.findViewById(R.id.lstSeznam)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                Snackbar.make(rootView, "Chcete zobrazit položku?", Snackbar.LENGTH_LONG).setAction("Zobrazit", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setElement((Placemark) parent.getItemAtPosition(position));
                    }
                }).show();
            }
        });

        ((ListView)rootView.findViewById(R.id.lstSeznam)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                setElement((Placemark) parent.getItemAtPosition(position));
                return true; //zavibrujeme
            }
        });

        //nastaveni buttonů
        btnChciVylezt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(element != null){
                    getMainActivity().switchToFragmentStats();
                    getMainActivity().getFragmentStats().switchToWishList();
                    getMainActivity().getFragmentStats().addToWishList(element);

                    getMainActivity().getFragmentSearch().searchForAll(); //refresh, aby se objevilo znaménko
                    setElement(element); //refresh detailu
                }
                else{
                    Toast.makeText(getContext(), "Není vybrán objekt k přidání", Toast.LENGTH_SHORT).show();
                }
            }

        });
        btnVylezeno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(element != null){
                    getMainActivity().switchToFragmentStats();
                    getMainActivity().getFragmentStats().switchToClimbedList();
                    getMainActivity().getFragmentStats().addToClimbedList(element);

                    getMainActivity().getFragmentSearch().searchForAll(); //refresh, aby se objevilo znaménko
                    setElement(element); //refresh detailu
                }
                else{
                    Toast.makeText(getContext(), "Není vybrán objekt k přidání", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(element != null){
                    getMainActivity().getFragmentMap().putPlacemarkOnMap(element);
                    //getMainActivity().switchToFragmentMap(); //v fragment map
                }
                else{
                    Toast.makeText(getContext(), "Není vybrán objekt k zobrazení", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //</editor-fold>

        System.out.println("Konec načítání view fragment Detail");

        return rootView;
    }
    public int getIcon(){
        return icon;
    }
    public int getIconActive(){
        return iconActive;
    }
    public int getIconTitle() {return iconTitle; }
    public String getTitle(){
        return title;
    }

    public Placemark getElement(){
        return element;
    }

    public void setElement(Placemark element){

        historyElements.add(element);

        this.element = element;
        txtNadpis.setText(element.getName());
        txtTyp.setText(element.getFolder().getDocument().getName() + " \\ " + element.getFolder().getName() + " \\ " + element.getStringType());

        //blokace tlačítek (povolí se, pokud se bude jednat o skálu)
        btnMapa.setEnabled(true); //zpočátku je false - pak už nejde nevybrat objekt //TODO určitě?
        btnChciVylezt.setEnabled(false);
        btnVylezeno.setEnabled(false);

        if(element.getType() == EPlacemark.POINT){
            LatLng aktualniPozice = null;//getMainActivity().getFragmentMap().getUserLatLng(); TODO
            if(aktualniPozice != null){
                txtOstatni.setText("Vzdušnou čarou: " + distanceBetweenLatLng(element.getCoordinates().get(0),aktualniPozice) + " metrů");
            }
            else{
                txtOstatni.setText(element.getCoordinates().get(0).toString());
            }

            if(!element.getFolder().getName().contains("Orient")){
                //pokud se nejedná o orientační body, nebo tam už náhodou nejsou
                btnVylezeno.setEnabled(!getMainActivity().getFragmentStats().isInClimbed(element));
                btnChciVylezt.setEnabled(!getMainActivity().getFragmentStats().isInWished(element));
            }

        }
        if(element.getType() == EPlacemark.LINESTRING){
            double vzdalenost = 0;
            for(LatLng l : element.getCoordinates()){
                if(element.getCoordinates().size() > (element.getCoordinates().indexOf(l)+1)){
                    vzdalenost += distanceBetweenLatLng(l, element.getCoordinates().get(element.getCoordinates().indexOf(l)+1));
                }
            }
            txtOstatni.setText("Délka: "+vzdalenost+" metrů");
        }
        if(element.getType() == EPlacemark.POLYGON){
            double vzdalenost = 0;
            for(LatLng l : element.getCoordinates()){
                vzdalenost += distanceBetweenLatLng(l,
                        (element.getCoordinates().get((element.getCoordinates().indexOf(l)+1) % element.getCoordinates().size())));
            }
            txtOstatni.setText("Obvod: ?? metrů");
        }
        imageView.setImageResource(element.getIconId());

        findRelevantAndFillThem(); //TODO
        //a přesuneme jakoby pohled na to samé (tím zjistíme, že se uvnitř zmenil objekt)
        //((MainActivity)getActivity()).switchFragmentView(MainActivity.FRAGMENT.DETAIL);
    }

    public void setPreviousElement(){
        //pokud jsou v historii záložek za sebou dva detaily, zavolá se tato metoda
        if(!historyElements.isEmpty()){

            if(historyElements.size()>1){
                setElement(historyElements.get(historyElements.size() - 2));
            }
            if(historyElements.size()>0){
                setElement(historyElements.get(0));
            }
            historyElements.remove(historyElements.size()-1);
        }
        else{
            Toast.makeText(getContext(), "Není dřívější prohlížený objekt", Toast.LENGTH_SHORT);
        }


    }

    private void findRelevantAndFillThem(){
        lstAdapter.clear();
        for(Placemark p : element.getFolder().getPlacemarks()){
            if(p != element)
                lstAdapter.add(p); //TODO seřadit dle blízkosti??
        }
    }
}

