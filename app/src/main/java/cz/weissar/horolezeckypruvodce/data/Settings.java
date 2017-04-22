package cz.weissar.horolezeckypruvodce.data;

import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import cz.weissar.horolezeckypruvodce.MainActivity;

/**
 * Created by petrw on 06.02.2016.
 */
public class Settings implements Serializable{

    private CheckBox chckIkonyVZahlavi;
    private CheckBox chckVysokaPresnost;
    private MainActivity mainActivity;
    //TODO next


    public Settings(final MainActivity mainActivity, CheckBox chckIkonyVZahlavi, CheckBox chckVysokaPresnost){
        this.chckIkonyVZahlavi = chckIkonyVZahlavi;
        this.chckVysokaPresnost = chckVysokaPresnost;

        this.mainActivity = mainActivity;

        load(); //proběhne jen na začátku aplikace - aplikace se podle toho nastaví a pak se kdyžtak při změně jen ukládá

        //TODO

        //LISTENERS
        chckIkonyVZahlavi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.showIconsTabLayout(isChecked);
                save();
            }
        });

        chckVysokaPresnost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.getFragmentMap().setPresnostDijkstra(isChecked);
                save();
            }
        });
    }


    private void load(){
        File f = new File(mainActivity.getExternalFilesDir(null)+"/settings.txt");

        if(f.exists()){
            //čteme
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

                boolean b,c;
                int m;

                b = Boolean.parseBoolean(br.readLine().split(":")[1]); //ikony v záhlaví
                c = Boolean.parseBoolean(br.readLine().split(":")[1]); //vysoká přesnost
                m = Integer.parseInt(br.readLine().split(":")[1]); //typ mapy

                br.close();

                //ikony
                chckIkonyVZahlavi.setChecked(b);
                mainActivity.showIconsTabLayout(b);

                //přesnost
                chckVysokaPresnost.setChecked(c);
                mainActivity.getFragmentMap().setPresnostDijkstra(c);

                //mapový podklad
                mainActivity.getFragmentMap().setMapType(m);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e){

            }
        }
        else{
            //asi první spuštění aplikace - vytvoříme ho
            save();
        }
    }
    public void saveBecauseOfMapChange(){
        save();
    }

    private void save(){
        //ukládá veškeré nastavení (kompletní seznam)
        File f = new File(mainActivity.getExternalFilesDir(null)+"/settings.txt");

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));

            bw.write("ikonyVZahlaviTabLayout:" + chckIkonyVZahlavi.isChecked());
            bw.newLine();

            bw.write("vysokaPresnostNavigace:" + chckVysokaPresnost.isChecked());
            bw.newLine();

            bw.write("posledniMapa:" + mainActivity.getFragmentMap().getMapType());

            bw.flush();
            bw.close();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

}
