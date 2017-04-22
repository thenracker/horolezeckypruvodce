package cz.weissar.horolezeckypruvodce.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.weissar.horolezeckypruvodce.R;
import cz.weissar.horolezeckypruvodce.data.EPlacemark;
import cz.weissar.horolezeckypruvodce.data.Placemark;
import cz.weissar.horolezeckypruvodce.data.PlacemarkListAdapter;

/**
 * Created by petrw on 17.10.2015.
 */
public class FragmentSearch extends FragmentE {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "sectionNumber";
    private int viewXML = R.layout.fragment_search;
    private static int icon = R.drawable.ic_search_g;
    private static int iconActive = R.drawable.ic_arrow_down_b; //R.drawable.ic_search_b;
    private static int iconTitle = R.drawable.ic_search_w;
    private static String title = "Hledat";
    //private ArrayAdapter<Placemark> resultAdapter; //spravuje obsah listview
    private ArrayList<Placemark> resultsArray; //je ovládán result adapterem
    private EditText searchedText;
    private ArrayList<Placemark> elements;
    private ListView listView; //obsahuje vyhledane skalni elementy

    private RadioGroup rG;
    private Switch switchCesty;
    private Switch switchSkaly;
    private Switch switchOblasti;
    private Switch switichOrientacniBody;
    private SearchView searchView;

    private Boolean otevrenFiltr = false; //na začátku je filtr zavřen

    private PlacemarkListAdapter myResultAdapter;

    private char sort; //neseřazeno zpočátku

    public FragmentSearch() {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, 1);
        setArguments(args);
    }

    //<editor-fold desc="INTERFACE A METODA NA VYVOLÁNÍ CALLBACK NA MAINACTIVITY">
    public static interface OnCompleteFragSearchListener {
        public abstract void onFragSearchComplete();
    }
    private OnCompleteFragSearchListener mListener;
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mListener = (OnCompleteFragSearchListener)activity;
    }
    //</editor-fold>

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(viewXML, container, false);

        System.out.println("Začátek načítání view fragment Search");

        //<editor-fold defaultstate="collapsed" desc="nastaveni Naplneni parametru a ListView">

        //vysledky list view
        resultsArray = new ArrayList<>();
        listView = (ListView)rootView.findViewById(R.id.listView);

        //custom listAdapter here
        myResultAdapter = new PlacemarkListAdapter(getContext(), R.layout.mylistview, resultsArray, getMainActivity());
        listView.setAdapter(myResultAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ziskat element
                Placemark item = (Placemark) parent.getItemAtPosition(position);
                //naplnit fragment
                getMainActivity().getFragmentDetail().setElement(item);
                //presunout view
                getMainActivity().switchToFragmentDetail();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                //získaný placemark
                final Placemark item = (Placemark) parent.getItemAtPosition(position);

                final int typ; //0 = skála, 1 = orientační bod, 2 = zbytek

                CharSequence options[];
                if(item.getType() == EPlacemark.POINT && !item.getFolder().getName().contains("Orient")){ //skalní věž
                    options = new CharSequence[]{"Zobrazit detail", "Zobrazit v mapě", "Zahájit navigaci",
                            "Přidat do seznamu přání",
                            "Přidat do seznamu vylezených", "Nic"};
                    typ = 0;

                }else if(item.getType() == EPlacemark.POINT){ //orientační bod
                    options = new CharSequence[]{"Zobrazit detail", "Zobrazit v mapě", "Zahájit navigaci", "Nic"};
                    typ = 1;
                }
                else{ //oblast nebo cesta
                    options = new CharSequence[]{"Zobrazit detail", "Zobrazit v mapě", "Nic"};
                    typ = 2;
                }

                //POPUP menu? TODO
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Co chcete udělat?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) { //pro všechny stejné
                            getMainActivity().getFragmentDetail().setElement(item);
                            getMainActivity().switchToFragmentDetail();

                        } else if (which == 1) { //pro všechny stejné
                            getMainActivity().getFragmentMap().putPlacemarkOnMap(item);

                        } else if (which == 2 && typ != 2) { //navigace u POINTU
                            getMainActivity().getFragmentMap().navigaceToPlacemark(item);

                        } else if(which == 3 && typ == 0){ //wish
                            if(!getMainActivity().getFragmentStats().isInWished(item))
                                getMainActivity().getFragmentStats().addToWishList(item);
                            else Toast.makeText(getContext(), "Tato položka již v seznamu je", Toast.LENGTH_SHORT).show();

                        } else if(which == 4 && typ == 0){ //climbed
                            if(!getMainActivity().getFragmentStats().isInClimbed(item))
                                getMainActivity().getFragmentStats().addToClimbedList(item);
                            else Toast.makeText(getContext(), "Tato položka již v seznamu je", Toast.LENGTH_SHORT).show();

                        }
                        else{ //ani jedno z předchozích logicky dismiss
                            dialog.dismiss();
                        }
                        //renačtení seznamu - když by bylo přidáno do seznamu, tak se překreslí řádek se symbolem
                        listActualization();

                    }
                }).show();

                return true;
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="nastaveni Switches a listenery">

        final SearchView sV = (SearchView)rootView.findViewById(R.id.searchView);
        sV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForData(query.isEmpty());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchForData(false);
                return false; //TODO POKUD si uživatel zvolí vyhledávat po stisknutí znaku
            }
        });
        sV.setFocusable(false); //nebude chytat focus po každý akci co se stane?? TODO funguje to? ale asi jo :)


        CompoundButton.OnCheckedChangeListener onChckListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                searchForData(false); //HLEDÁME I KDYŽ NENÍ TEXT //ale tady musi fungovat fireTableDataChange :/
            }
        };

        ((Switch)rootView.findViewById(R.id.switchOblasti)).setOnCheckedChangeListener(onChckListener);
        ((Switch)rootView.findViewById(R.id.switchSkaly)).setOnCheckedChangeListener(onChckListener);
        ((Switch)rootView.findViewById(R.id.switchCesty)).setOnCheckedChangeListener(onChckListener);
        ((Switch)rootView.findViewById(R.id.switichOrientacniBody)).setOnCheckedChangeListener(onChckListener);
        //</editor-fold>

        rG = (RadioGroup)rootView.findViewById(R.id.switchesGroup);
        switchCesty = (Switch)rootView.findViewById(R.id.switchCesty);
        switchSkaly = (Switch)rootView.findViewById(R.id.switchSkaly);
        switchOblasti = (Switch)rootView.findViewById(R.id.switchOblasti);
        switichOrientacniBody = (Switch)rootView.findViewById(R.id.switichOrientacniBody);
        searchView = (SearchView)rootView.findViewById(R.id.searchView);

        //a zavoláme na MainActivity že jsme hotovi - mainActivita
        mListener.onFragSearchComplete();
        //</editor-fold>

        System.out.println("Konec načítání view fragment Search");

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //myResultAdapter.notifyDataSetChanged();
        //TODO po otočení se ztrácí data
        //getMainActivity().fillElementsInSearchFragment();
        //myResultAdapter.addAll(elements);
    }

    //FUNCTIONS
    public void showFilter() { //objevování a mizení filtrování pomocí Animace.. :D FTW
        if(!otevrenFiltr){
            Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.filter_rise_up);
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    rG.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            rG.startAnimation(a);
            otevrenFiltr = true;
        }
        else{
            Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.filter_calm_down);
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    rG.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rG.startAnimation(a);
            otevrenFiltr = false;
        }
    }

    public Boolean isOtevrenFiltr() {
        return otevrenFiltr;
    }
    public char hasSort(){ return sort; } //pro označování checked v menu

    public void searchForAll(){
        if(elements != null) //pojistky na vyhledavani
            if(!elements.isEmpty())
                if(myResultAdapter != null){
                    myResultAdapter.clear();
                    myResultAdapter.addAll(elements);
                }
    }

    public void listActualization(){
        myResultAdapter.notifyDataSetChanged(); //TODO zabírá?
        //searchForData(false); //hledáme tak jako tak
    }

    private void searchForData(boolean prazdne){

        if(!prazdne){ //NECHCEME HLEDAT KDYŽ UŽIVATEL NIC NEZADAL - LEDA BY TO VYBRAL V MENU

            boolean c = switchCesty.isChecked();
            boolean s = switchSkaly.isChecked();
            boolean o = switchOblasti.isChecked();
            boolean r = switichOrientacniBody.isChecked();

            if((!c || !s || !c || !r) && !otevrenFiltr){
                Toast t = Toast.makeText(getContext(),"Je zapnut filtr",Toast.LENGTH_SHORT);
                t.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFilter();
                    }
                });
                t.show();
            }

            myResultAdapter.clear();

            String searchedText = searchView.getQuery().toString();

            if(elements != null) //pojistky na vyhledavani
                if(!elements.isEmpty())
                    for(Placemark e : elements){
                        if (e.getName().toLowerCase().contains(searchedText.toLowerCase())){
                            if(e.getFolder().getDocument().getName() != "Cesty Polické stěny") //Nechceme hledat v LINE x ;) TODO dát do ryti - už je ošéflé, že se do fragmentu posílají dokumenty bez roadů
                                switch (e.getType()){
                                    case LINESTRING: if(c) myResultAdapter.add(e); break;
                                    case POINT:  if(s) if(!e.getFolder().getName().contains("rient")) myResultAdapter.add(e);  //bez Orientačních
                                        if(r) if(e.getFolder().getName().contains("rient")) myResultAdapter.add(e); break; //s Orientačními
                                    case POLYGON: if(o) myResultAdapter.add(e); break;
                                }
                            else
                                myResultAdapter.add(e);

                        }
                    }
        }

    }
    public void setElements(ArrayList<Placemark> elements){
        this.elements = elements;
        searchForAll();
    }

    public Placemark getPlacemarkByLatLng(LatLng l){
        for(Placemark p : elements)
            if(p.getType() == EPlacemark.POINT)
                if(p.getCoordinates().get(0).latitude == l.latitude)
                    if(p.getCoordinates().get(0).longitude == l.longitude)
                        return p;
        return null;
    }

    public void sortAlphabetically(){
        if(!elements.isEmpty()){
            Comparator<Placemark> comp = new Comparator<Placemark>() {
                @Override
                public int compare(Placemark lhs, Placemark rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            };
            //myResultAdapter.sort(comp);
            Collections.sort(elements, comp);
            searchForData(false);
        }
        this.sort = 'a'; //alphabetically
    }

    public void sortNearestToFarest(){
        if(getMainActivity().getFragmentMap().getUserLatLng() != null){
            if(!elements.isEmpty()){
                Comparator<Placemark> comp = new Comparator<Placemark>() {
                    @Override
                    public int compare(Placemark lhs, Placemark rhs) {

                        return Double.compare(distanceBetweenLatLng(lhs.getCoordinates().get(0),getMainActivity().getFragmentMap().getUserLatLng()),
                                distanceBetweenLatLng(rhs.getCoordinates().get(0),getMainActivity().getFragmentMap().getUserLatLng()));

                    }
                };
                Collections.sort(elements, comp);
                searchForData(false);
            }
        }
        else{
            Toast.makeText(getContext(), "Nelze najít vaši polohu", Toast.LENGTH_SHORT).show();
        }
        this.sort = 'n'; //nearest
    }

    public void sortWished(){
        myResultAdapter.clear();
        for(Placemark p : elements){
            if(getMainActivity().getFragmentStats().isInWished(p)) myResultAdapter.add(p);

        }
        this.sort = 'w'; //alphabetically
    }
    public void sortClimbed(){
        myResultAdapter.clear();
        for(Placemark p : elements){
            if(getMainActivity().getFragmentStats().isInClimbed(p)) myResultAdapter.add(p);
        }
        this.sort = 'c'; //alphabetically
    }

    public ArrayList<Placemark> getNearestRocks(LatLng point, Double radius){
        ArrayList<Placemark> results = new ArrayList<>();

        if(elements != null)
            for(Placemark p : elements){
                if(p.getType() == EPlacemark.POINT){
                    if(distanceBetweenLatLng(p.getCoordinates().get(0), point) < radius){
                        results.add(p);
                    }
                }
            }

        return results;
    }

    private double distanceBetweenLatLng(LatLng l1, LatLng l2){

        double latitude1 = l1.latitude;
        double longitude1 = l1.longitude;
        double latitude2 = l2.latitude;
        double longitude2 = l2.longitude;

        return 6371 * Math.acos(Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude2 - longitude1));

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
    } //.toUpperCase()


}

