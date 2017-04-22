package cz.weissar.horolezeckypruvodce.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cz.weissar.horolezeckypruvodce.R;
import cz.weissar.horolezeckypruvodce.data.EPlacemark;
import cz.weissar.horolezeckypruvodce.data.Placemark;

/**
 * Created by petrw on 17.10.2015.
 */
public class FragmentStats extends FragmentE {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "sectionNumber";
    private int viewXML = R.layout.fragment_stats;
    private static int icon = R.drawable.ic_check_g;
    private static int iconActive = R.drawable.ic_check_b;
    private static int iconTitle = R.drawable.ic_check_w;
    private static String title = "Statistiky";

    private ArrayAdapter<Placemark> climbedListAdapter; //spravuje obsah listview
    private ArrayList<Placemark> climbedListArray; //je ovládán result adapterem
    private ArrayAdapter<Placemark> wishListAdapter; //spravuje obsah listview
    private ArrayList<Placemark> wishListArray; //je ovládán result adapterem
    private ListView listView;
    private Button wishListButton;
    private Button climbedListButton;
    private FloatingActionButton f;

    public FragmentStats() {

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, 3);
        setArguments(args);

        climbedListArray = new ArrayList<>();
        wishListArray = new ArrayList<>();

    }

    //<editor-fold desc="INTERFACE A METODA NA VYVOLÁNÍ CALLBACK NA MAINACTIVITY">
    public static interface OnCompleteFragStatsListener {
        public abstract void onFragStatsComplete();
    }
    private OnCompleteFragStatsListener mListener;
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mListener = (OnCompleteFragStatsListener)activity;
    }
    //</editor-fold>

    //TODO metoda ukládání do souboru
    public void saveLists(){

        getMainActivity().saveLists(climbedListArray, wishListArray);
    }
    public void loadLists(){
        //getMainActivity().loadLists(climbedListAdapter, wishListAdapter);
        //neposíláme adaptéry (jelikož občas ještě nejsou vytvořeny) - proto posíláme seznamy
        //TODO je to v pořádku? - můžeme si díky callbacku dovolit poslat adaptéry?
        getMainActivity().loadLists(climbedListAdapter, wishListAdapter);
        //getMainActivity().loadLists(climbedListArray, wishListArray);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(viewXML, container, false);

        System.out.println("Začátek načítání view fragment Stats");

        climbedListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_activated_1, climbedListArray);
        wishListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_activated_1, wishListArray);

        listView = (ListView)rootView.findViewById(R.id.listView);
        listView.setAdapter(wishListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {

                CharSequence options[] = new CharSequence[]{"Zobrazit detail","Smazat","Přesunout","Zahájit navigaci","Zrušit"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Co chcete udělat?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //naplnit fragment vybraným elementem
                            getMainActivity().getFragmentDetail().setElement((Placemark) parent.getItemAtPosition(position));
                            //presunout view
                            getMainActivity().switchToFragmentDetail();
                        } else if (which == 1) {
                            if (listView.getAdapter() == wishListAdapter)
                                wishListAdapter.remove((Placemark) parent.getItemAtPosition(position));
                            if (listView.getAdapter() == climbedListAdapter)
                                climbedListAdapter.remove((Placemark) parent.getItemAtPosition(position));

                            Toast.makeText(getContext(), "Položka smazána", Toast.LENGTH_SHORT).show();
                            saveLists();
                        } else if (which == 2) {
                            switchPlacemark((Placemark) listView.getAdapter().getItem(position));
                            switchAdapters(); //přehodíme pohled na druhý seznam
                            Toast.makeText(getContext(), "Přehozeno do druhého seznamu", Toast.LENGTH_SHORT).show();
                            saveLists();
                        } else if (which == 3) {
                            getMainActivity().getFragmentMap().navigaceToPlacemark((Placemark) listView.getAdapter().getItem(position));
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).show();

            }
        });

        listView.setLongClickable(false);

        wishListButton = ((Button)rootView.findViewById(R.id.btnWishList));
        climbedListButton = ((Button)rootView.findViewById(R.id.btnClimbedList));

        //nastavení přepínání adapterů dle toggle buttonů
        wishListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToWishList();
            }
        });

        climbedListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToClimbedList();
            }
        });
        f = (FloatingActionButton)rootView.findViewById(R.id.fabswitchlist);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listView.getAdapter() == wishListAdapter){
                    switchToClimbedList();
                }
                else{
                    switchToWishList();
                }

            }
        });

        //a pošleme callbackMainActivitě
        mListener.onFragStatsComplete();

        System.out.println("Konec načítání view fragment Stats");

        return rootView;
    }


    public void switchToWishList(){
        if(listView.getAdapter() != wishListAdapter)
            f.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate_inside_360));

        listView.setAdapter(wishListAdapter);
        wishListButton.setBackgroundColor(0x00ffffff); //Transparent
        climbedListButton.setBackgroundColor(getResources().getColor(R.color.button_material_light));
        wishListButton.setTextColor(getResources().getColor(R.color.colorAccent));
        climbedListButton.setTextColor(getResources().getColor(R.color.common_signin_btn_dark_text_disabled));
    }

    public void switchToClimbedList(){
        if(listView.getAdapter() != climbedListAdapter)
            f.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate_inside_180));

        listView.setAdapter(climbedListAdapter);
        wishListButton.setBackgroundColor(getResources().getColor(R.color.button_material_light));
        climbedListButton.setBackgroundColor(0x00ffffff); //Transparent
        wishListButton.setTextColor(getResources().getColor(R.color.common_signin_btn_dark_text_disabled));
        climbedListButton.setTextColor(getResources().getColor(R.color.colorAccent));
    }
    private void switchAdapters(){
        if(listView.getAdapter() == climbedListAdapter){
            switchToWishList();
        }else{
            switchToClimbedList();
        }
    }

    private void switchPlacemark(Placemark re){
        if(wishListArray.contains(re)){
            wishListAdapter.remove(re);
            climbedListAdapter.add(re);
        }else if(climbedListArray.contains(re)){
            climbedListAdapter.remove(re);
            wishListAdapter.add(re);
        }else{
            Toast.makeText(getContext(), "Nastala chyba", Toast.LENGTH_SHORT).show();
        }
    }

    public void addToWishList(Placemark re){
        if(re.getType() == EPlacemark.POINT && !re.getFolder().getName().contains("Orient")){

            getMainActivity().switchToFragmentStats();
            getMainActivity().getFragmentStats().switchToWishList();

            if(!wishListArray.contains(re)){
                if(climbedListArray.contains(re)){
                    climbedListAdapter.remove(re); //nemůže být zároveň v obojím
                    wishListAdapter.add(re);
                    saveLists();
                    Toast.makeText(getContext(), "Přesunuto ze seznamu vylezených", Toast.LENGTH_SHORT).show();
                }else{
                    wishListAdapter.add(re);
                    saveLists();
                    Toast.makeText(getContext(), "Přidáno do seznamu přání", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(getContext(), "Tuto položku již v seznamu máte", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext(), "Tento objekt nelze přidat do seznamu", Toast.LENGTH_SHORT).show();
        }
    }
    public void addToClimbedList(Placemark re){
        if(re.getType() == EPlacemark.POINT && !re.getFolder().getName().contains("Orient")){

            getMainActivity().switchToFragmentStats();
            getMainActivity().getFragmentStats().switchToClimbedList();

            if(!climbedListArray.contains(re)){
                if(wishListArray.contains(re)){ //nemůže být zároveň v obojím
                    wishListAdapter.remove(re);
                    climbedListAdapter.add(re);
                    saveLists();
                    Toast.makeText(getContext(), "Přesunuto ze seznamu přání", Toast.LENGTH_SHORT).show();
                }else{
                    climbedListAdapter.add(re);
                    saveLists();
                    Toast.makeText(getContext(), "Přidáno do seznamu vylezených", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(getContext(), "Tuto položku již v seznamu máte", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext(), "Tento objekt nelze přidat do seznamu", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isInClimbed(Placemark re){
        return climbedListArray.contains(re);
    }
    public boolean isInWished(Placemark re){
        return wishListArray.contains(re);
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

}

