package cz.weissar.horolezeckypruvodce;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;

import cz.weissar.horolezeckypruvodce.data.DataManager;
import cz.weissar.horolezeckypruvodce.data.Document;
import cz.weissar.horolezeckypruvodce.data.Placemark;
import cz.weissar.horolezeckypruvodce.data.Settings;
import cz.weissar.horolezeckypruvodce.fragments.FragmentDetail;
import cz.weissar.horolezeckypruvodce.fragments.FragmentMap;
import cz.weissar.horolezeckypruvodce.fragments.FragmentSearch;
import cz.weissar.horolezeckypruvodce.fragments.FragmentStats;
import cz.weissar.horolezeckypruvodce.fragments.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity
        implements FragmentSearch.OnCompleteFragSearchListener, FragmentStats.OnCompleteFragStatsListener {

    //<editor-fold desc="ATRIBUTY">
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private int lastSelectedTab = 0;
    private ArrayList<Integer> historySelectedTabs;

    public enum FRAGMENT{ SEARCH, DETAIL, STATS, MAP };

    private DataManager dataManager;
    private ProgressDialog progressDialog;
    private Settings settings; //nastavení

    private boolean statsFirstTime = true; //při otočení pohledu v appce to padalo TODO wtf
    //</editor-fold>


    //<editor-fold desc="METODY PUBLIC, GETTERS & SETTERS">
    public Fragment getFragmentByType(FRAGMENT fragment){
        switch(fragment){
            default:
            case SEARCH: return mSectionsPagerAdapter.getItem(0);
            case DETAIL: return  mSectionsPagerAdapter.getItem(1);
            case STATS: return  mSectionsPagerAdapter.getItem(2);
            case MAP: return  mSectionsPagerAdapter.getItem(3);
        }
    }
    public int getFragmentIdByType(FRAGMENT fragment){
        switch(fragment){
            default:
            case SEARCH: return 0;
            case DETAIL: return 1;
            case STATS: return 2;
            case MAP: return 3;
        }
    }
    public void switchFragmentView(FRAGMENT fragment){
        //viewPager.setCurrentItem(getFragmentIdByType(fragment));
        tabLayout.getTabAt(getFragmentIdByType(fragment)).select(); //kvůli vybarvování textu voláme takto
    }

    public void switchToFragmentSearch(){switchFragmentView(FRAGMENT.SEARCH);}
    public void switchToFragmentDetail(){switchFragmentView(FRAGMENT.DETAIL);}
    public void switchToFragmentStats(){switchFragmentView(FRAGMENT.STATS);}
    public void switchToFragmentMap(){switchFragmentView(FRAGMENT.MAP);}

    public FragmentSearch getFragmentSearch(){ return (FragmentSearch)getFragmentByType(FRAGMENT.SEARCH); }
    public FragmentDetail getFragmentDetail(){ return (FragmentDetail)getFragmentByType(FRAGMENT.DETAIL); }
    public FragmentStats getFragmentStats(){return (FragmentStats)getFragmentByType(FRAGMENT.STATS); }
    public FragmentMap getFragmentMap(){return (FragmentMap)getFragmentByType(FRAGMENT.MAP); }

    public Document getRoadDocument(){
        return dataManager.getLoadedDocuments().get(2); //dokument s cestami
    }

    //METODY PŘEDÁVÁNÍ PARAMETRŮ DATAMANAŽEROVI NA ZPRACOVÁNÍ
    public void loadLists(ArrayAdapter<Placemark> climbedListAdapter, ArrayAdapter<Placemark> wishListAdapter){
        ArrayList<Placemark> c = new ArrayList<>();
        ArrayList<Placemark> w = new ArrayList<>();

        dataManager.loadStatsData(c,w);

        climbedListAdapter.clear();
        wishListAdapter.clear();
        //TATO PRASEČINA JE TU PROTO, ŽE DATAMANAGER JE V JINÉM VLÁKNĚ A NEMOHL BY DO ADAPTÉRŮ ZAPISOVAT
        //TODO vyřešeno?
        climbedListAdapter.addAll(c);
        wishListAdapter.addAll(w);
    }
    public void saveLists(ArrayList<Placemark> climbedListArray, ArrayList<Placemark> wishListArray){
        dataManager.saveStatsData(climbedListArray, wishListArray);
    }
    //UKLÁDÁNÍ NASTAVENÍ PO VÝBĚRU MAPY
    public void saveSettings(){
        settings.saveBecauseOfMapChange();
    }
    //</editor-fold>

    //<editor-fold desc="METODY PRIVATE">
    private void sendDataToSearchFragment(){

        if(dataManager.dataFileExists()){
            getFragmentSearch().setElements(dataManager.getLoadedPlacemarksWithoutRoads());
        }

    }
    private void sendDataToMapFragment() {

        if(dataManager.dataFileExists())
            getFragmentMap().initDijkstra(getRoadDocument());
    }

    private void sendDataToStatsFragment(){
        if(dataManager.dataFileExists())
            getFragmentStats().loadLists();
    }

    public void fillDataToFragmentsAfterDownload(){
        sendDataToSearchFragment();
        sendDataToStatsFragment();
        sendDataToMapFragment();
    }

    private void downloadData(){
        //načtou se ONLINE data, odešlou do fragmentů

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Stahuji soubor z webu");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                dataManager.loadDataFromWeb(); //download & load ;)

                runOnUiThread(new Runnable() { //třeba upravovat view z UI threadu :)
                    @Override
                    public void run() {
                        fillDataToFragmentsAfterDownload();
                    }
                });

                progressDialog.dismiss();
            }
        }).start();

        //vlákno čeká - bude notifikováno od potomka

    }
    private void loadData(){
        //načtou se OFFLINE data, odešlou do fragmentů
        new Thread(new Runnable() {
            @Override
            public void run() {
                dataManager.loadData();

                sendDataToMapFragment();

                //LOAD DATA SE VOLÁ JEN PŘI INITU
                //METODY NÍŽE SE ZAVOLAJÍ CALLBACKEM
                /*
                sendDataToSearchFragment();

                sendDataToStatsFragment();
                */
            }
        }).start();
    }
    public boolean offlineMapExists(){
        return dataManager.mapFileExists();
    }
    private void downloadMaps(boolean vynuceno){

        if(!dataManager.mapFileExists() || vynuceno){ //vynuceno je pro tvrdé přepsání map
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Stáhnutí offline map");

            builder.setMessage("Soubor s mapou má přibližně 40MB, chcete tento soubor stáhnout nyní?")
                    .setCancelable(false)
                    .setPositiveButton("Stáhnout", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                            final PowerManager.WakeLock mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                    getClass().getName());
                            mWakeLock.acquire(); //zámek obrazovky

                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("Stahuji mapu z webu, to může trvat i několik minut.. ");

                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setProgressNumberFormat(null); //aby nebyly vidět procenta - páč se neaktualizovaly

                            progressDialog.setMax(100); //velikost souboru
                            progressDialog.setIndeterminate(false); //aby se ukazoval progress
                            progressDialog.show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dataManager.downloadZipOfflineMaps(progressDialog);  //stáhne a rozbalí (pokud jsou stažené - jen rozbalí)

                                    progressDialog.dismiss();

                                    mWakeLock.release();
                                }
                            }).start();

                        }
                    })
                    .setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else{
            Toast.makeText(getApplicationContext(), "Mapy jsou již staženy (pokud je chcete i přesto stáhnout, podržte tlačítko stisknuté)", Toast.LENGTH_SHORT).show();
        }
    }

    public void showIconsTabLayout(boolean show){
        mSectionsPagerAdapter.switchIconsForText(show);

        int actualPosition = viewPager.getCurrentItem();

        if (actualPosition == 0) viewPager.setCurrentItem(1,false);

        viewPager.setCurrentItem(0,false); //false dělá okamžitou změnu bez animace
        viewPager.setCurrentItem(1,false);
        viewPager.setCurrentItem(2,false);
        viewPager.setCurrentItem(3,false);
        viewPager.setCurrentItem(actualPosition,false);

        if (actualPosition == 0) historySelectedTabs.remove(historySelectedTabs.size() - 1);

        historySelectedTabs.remove(historySelectedTabs.size() - 1);
        historySelectedTabs.remove(historySelectedTabs.size() - 1);
        historySelectedTabs.remove(historySelectedTabs.size() - 1);
        historySelectedTabs.remove(historySelectedTabs.size() - 1);
        historySelectedTabs.remove(historySelectedTabs.size() - 1);
    }

    //</editor-fold>

    //<editor-fold desc="METODY OVERRIDE">
    //<editor-fold desc="CALLBACK METODY OD FRAGMENTŮ a DATAMANAGERA">
    @Override
    public void onFragSearchComplete() {
        //PO KOMPLETNÍM VYTVOŘENÍ FRAGMENT SEARCH se tímto zavolá callback
        sendDataToSearchFragment();
    }

    @Override
    public void onFragStatsComplete() {
        if(statsFirstTime){ //po otočení obrazovky se metoda vyvolá znovu, což už nepotřebujeme
            sendDataToStatsFragment();
            statsFirstTime = false;
        }

    }
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //<editor-fold desc="INIT toolbar, drawer, levé menu listenery, init progress dialogu">

        // init toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //init drawer
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.getParent().getParent().requestDisallowInterceptTouchEvent(true); //při vysunutém drawer blokujeme content - nefunguje
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle.syncState();

        //odkaz na openstreetmaps?
        findViewById(R.id.txtAppInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, "weissar.petr@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Mobilní horolezecký průvodce");
                intent.putExtra(Intent.EXTRA_TEXT, ""); //email body

                startActivity(Intent.createChooser(intent, "Poslat email vývojáři"));
            }
        });

        //<editor-fold desc="Nastavení aplikace v draweru">
        ((Button)findViewById(R.id.btn_aktualizovat)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadData();
            }
        });
        ((Button)findViewById(R.id.btn_download_map)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMaps(false);
            }
        });
        findViewById(R.id.btn_download_map).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                downloadMaps(true);
                return false;
            }
        });

        //TODO doplnit zbytek tlačítek a označit je dle nastavení aplikace

        //TODO zbytek chcks

        progressDialog = new ProgressDialog(this);

        progressDialog.setCancelable(false);
        //</editor-fold>
        //</editor-fold>

        //<editor-fold desc="INIT sectionsPager (vytvoření fragmentů), tabLayout, historie zobrazených záložek pro tlačítko zpět">

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),MainActivity.this);
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3); //jedna je aktivní, 3 uložené (y)

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //zmenit titulek aplikace
                getSupportActionBar().setTitle(mSectionsPagerAdapter.getPageTitleAndIconLeft(viewPager.getCurrentItem()));

                //zmenit puvodni ikonu
                tabLayout.getTabAt(lastSelectedTab).setText(mSectionsPagerAdapter.getPageTitle(lastSelectedTab));
                //nastavit novou ikonu
                tabLayout.getTabAt(position).setText(mSectionsPagerAdapter.getActivePageTitle(position));

                //zadame tab do historie
                historySelectedTabs.add(new Integer(lastSelectedTab));
                lastSelectedTab = position;
                invalidateOptionsMenu(); //přepsat menu

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });
        //INIT AKTUÁLNÍ ZÁLOŽKA = druhá (tedy ID = 1)
        historySelectedTabs = new ArrayList<>();
        viewPager.setCurrentItem(1, true);
        //nechceme se po tlačítku zpět vracet na Search záložku - proto když by hned někdo zmáčkl zpět, appka se vypne
        historySelectedTabs.clear();
        //</editor-fold>

        //<editor-fold desc="INIT datamanažera, (naplnění dat do fragmentuSearch a init dijkstra)v metodě sendDataTo..">
        dataManager = new DataManager(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(!dataManager.dataFileExists()){
                    Snackbar.make(findViewById(android.R.id.content), "Zatím nebyla stažena data", Snackbar.LENGTH_INDEFINITE).setAction("Stáhnout nyní", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadData();  //download & load ;)
                        }
                    }).show();

                }else{
                    loadData();
                }

            }
        }).start();
        //</editor-fold>

        //<editor-fold desc="SETTINGS vytvoření a vložení checkboxů">
        settings = new Settings(this, (CheckBox)findViewById(R.id.chck_show_icons_or_texts),(CheckBox)findViewById(R.id.chck_high_presnost));
        //</editor-fold>

    }

    @Override
    protected void onPause() {
        super.onPause();
        getFragmentMap().stopUpdatingCamera(); //jinak pořád jede jak čůro
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //VISIBILITA MENU PŘI ZMĚNĚ FRAGMENTU
        menu.findItem(R.id.action_filtr).setVisible(true);
        menu.findItem(R.id.action_navigovat).setVisible(true);
        menu.findItem(R.id.action_zmenaMapy).setVisible(true);
        menu.findItem(R.id.action_vymazatVse).setVisible(true);
        menu.findItem(R.id.action_seradit_abecedne).setVisible(true);
        menu.findItem(R.id.action_seradit_dle_vzdalenosti).setVisible(true);
        menu.findItem(R.id.action_zobrazitCesty).setVisible(true);
        menu.findItem(R.id.action_rotateMap).setVisible(true);
        (menu.findItem(R.id.action_urcitPolohu)).setVisible(true);
        menu.findItem(R.id.action_zobrazit_wishList).setVisible(true);
        menu.findItem(R.id.action_zobrazit_climbedList).setVisible(true);

        //TODO možnosti na smazání seznamu chci vylézt a vylezeno

        //checknutí seřazení
        //TODO přepsat do group?
        if(getFragmentSearch().hasSort() == 'a'){ //alphabetically
            menu.findItem(R.id.action_seradit_abecedne).setChecked(true);
            menu.findItem(R.id.action_seradit_dle_vzdalenosti).setChecked(false);
            menu.findItem(R.id.action_zobrazit_wishList).setChecked(false);
            menu.findItem(R.id.action_zobrazit_climbedList).setChecked(false);
        }
        if(getFragmentSearch().hasSort() == 'n'){ //nearest
            menu.findItem(R.id.action_seradit_abecedne).setChecked(false);
            menu.findItem(R.id.action_seradit_dle_vzdalenosti).setChecked(true);
            menu.findItem(R.id.action_zobrazit_wishList).setChecked(false);
            menu.findItem(R.id.action_zobrazit_climbedList).setChecked(false);
        }
        if(getFragmentSearch().hasSort() == 'w'){ //wish
            menu.findItem(R.id.action_seradit_abecedne).setChecked(false);
            menu.findItem(R.id.action_seradit_dle_vzdalenosti).setChecked(false);
            menu.findItem(R.id.action_zobrazit_wishList).setChecked(true);
            menu.findItem(R.id.action_zobrazit_climbedList).setChecked(false);
        }
        if(getFragmentSearch().hasSort() == 'c'){ //climbed
            menu.findItem(R.id.action_seradit_abecedne).setChecked(false);
            menu.findItem(R.id.action_seradit_dle_vzdalenosti).setChecked(false);
            menu.findItem(R.id.action_zobrazit_wishList).setChecked(false);
            menu.findItem(R.id.action_zobrazit_climbedList).setChecked(true);
        }
        menu.findItem(R.id.action_rotateMap).setChecked(getFragmentMap().isRotate()); //checked pokud rotace mapy


        //0 vyhledávání, 1 detail, 2 statistiky, 3 mapa
        //změna mapy, zobrazit vše a vymazat vše jen u mapy
        if(viewPager.getCurrentItem() != 0){
            (menu.findItem(R.id.action_filtr)).setVisible(false);
            (menu.findItem(R.id.action_vyhledat_vse)).setVisible(false);
            menu.findItem(R.id.action_seradit_abecedne).setVisible(false);
            menu.findItem(R.id.action_seradit_dle_vzdalenosti).setVisible(false);
            menu.findItem(R.id.action_zobrazit_wishList).setVisible(false);
            menu.findItem(R.id.action_zobrazit_climbedList).setVisible(false);
        }
        if(viewPager.getCurrentItem() != 1){
            menu.findItem(R.id.action_navigovat).setVisible(false);
        }
        if(viewPager.getCurrentItem() != 2){
            //(menu.findItem(R.id.action_nacistLocal)).setVisible(false);
            //(menu.findItem(R.id.action_stahnoutMapu)).setVisible(false);
        }
        if(viewPager.getCurrentItem() != 3){
            (menu.findItem(R.id.action_zmenaMapy)).setVisible(false);
            (menu.findItem(R.id.action_vymazatVse)).setVisible(false);
            (menu.findItem(R.id.action_zobrazitCesty)).setVisible(false);
            (menu.findItem(R.id.action_urcitPolohu)).setVisible(false);
            (menu.findItem(R.id.action_rotateMap)).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_filtr){
            getFragmentSearch().showFilter();
        }
        if(id == R.id.action_vyhledat_vse){
            getFragmentSearch().searchForAll();
        }
        if(id == R.id.action_navigovat){
            getFragmentMap().navigaceToPlacemark(getFragmentDetail().getElement());
        }
        if(id == R.id.action_zmenaMapy){
            getFragmentMap().switchMapType();
        }
        if(id == R.id.action_zobrazitCesty){
            getFragmentMap().fillAllWays();
        }
        if(id == R.id.action_rotateMap){
            getFragmentMap().rotateCamera();
        }
        if(id == R.id.action_vymazatVse){
            getFragmentMap().clearMap();
        }
        if(id == R.id.action_urcitPolohu){
            getFragmentMap().setMyLocation();
        }
        if(id == R.id.action_seradit_abecedne){
            getFragmentSearch().sortAlphabetically();
            invalidateOptionsMenu(); //překreslit menu
        }
        if(id == R.id.action_seradit_dle_vzdalenosti){
            getFragmentSearch().sortNearestToFarest();
            invalidateOptionsMenu();
        }
        if(id == R.id.action_zobrazit_wishList){
            getFragmentSearch().sortWished();
            invalidateOptionsMenu();
        }
        if(id == R.id.action_zobrazit_climbedList){
            getFragmentSearch().sortClimbed();
            invalidateOptionsMenu();
        }
        if(id == R.id.action_napoveda){


            if(viewPager.getCurrentItem() == 0){
                Toast.makeText(MainActivity.this, "Ze seznamu objektů dlouze podržte na položce, nebo klepněte na symbol tří teček pro rozšiřující možnosti\n" +
                        "(pokud se Vám seznam nezobrazuje, zvole v levém vysouvacím menu nabídku aktualizovat data)", Toast.LENGTH_LONG).show();
            }
            if(viewPager.getCurrentItem() == 1){
                Toast.makeText(MainActivity.this, "Pokud byla vybrána položka ze seznamu, můžete zde přidávat objekt do seznamu, zobrazit si ho v mapě, nebo" +
                        " si ho na horní liště rovnou zadat do navigace", Toast.LENGTH_LONG).show();
            }
            if(viewPager.getCurrentItem() == 2){
                Toast.makeText(MainActivity.this, "V horní části je možné se přepínat mezi svými seznamy a po kliknutí na objekt můžete vyvolat rozšiřující nabídku funkcí" +
                        "", Toast.LENGTH_LONG).show();
            }
            if(viewPager.getCurrentItem() == 3){
                Toast.makeText(MainActivity.this, "Dlouze podržte v mapě pro vyvolání kruhového výběru objektů - pro rozšíření znovu klepněte do mapy - " +
                        "po výběru objektu můžete spustit navigaci k němu z Vaší polohy pomocí tlačítka vlevo dole\n" +
                        "(mapové podklady přepínáte v menu na horní části obrazovky - pro zobrazení offline map je nutné si podklady stáhnout v levé nabídce menu aplikace)", Toast.LENGTH_LONG).show();
            }

            //TODO nápověda by zvolený fragment
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if(viewPager.getCurrentItem() == 0 && getFragmentSearch().isOtevrenFiltr()){
            getFragmentSearch().showFilter(); //zavřeme filtr
        }
        else if(historySelectedTabs.isEmpty())
            super.onBackPressed();
        else{
            //if dvakrát za sebou detail, změníme ho zpět, ale dříve mu dáme předchozí element
            //viewPager.setCurrentItem(historySelectedTabs.get(historySelectedTabs.size() - 1));
            tabLayout.getTabAt(historySelectedTabs.get(historySelectedTabs.size() - 1)).select(); //vybíráme takto kvůli barvě textu
            //a umazat dvě - protože jsme si tam aktuální připsali :)
            historySelectedTabs.remove(historySelectedTabs.size()-1);
            historySelectedTabs.remove(historySelectedTabs.size()-1);
        }

    }
    //</editor-fold>
}
