package cz.weissar.horolezeckypruvodce.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cz.weissar.horolezeckypruvodce.R;

/**
 * Created by petrw on 29.10.2015.
 */
public class DataManager {

    private Context context;
    private File[] files;
    private ArrayList<Document> documents;

    //<editor-fold desc="KONSTANTY url a názvy souborů">
    private String[] urls = new String[]{
            "https://www.google.com/maps/d/u/0/kml?mid=zhIxwmfDd9HE.kf9MGn3OfViQ&forcekml=1",
            "https://www.google.com/maps/d/u/0/kml?mid=zhIxwmfDd9HE.kZkT4wNhxX6I&forcekml=1",
            "https://www.google.com/maps/d/u/0/kml?mid=zGv3nm4J8ZQs.ki0W8m2abvY4&forcekml=1"


    };
    private String[] names = new String[]{
            "V Horách",
            "Slavenské skály",
            "Cesty"
    };
    //</editor-fold>

    public DataManager(Context context){

        this.context = context;
        //check if file exists
        files = new File[]{
                new File(context.getExternalFilesDir(null)+"/"+names[0]+".kml"),
                new File(context.getExternalFilesDir(null)+"/"+names[1]+".kml"),
                new File(context.getExternalFilesDir(null)+"/"+names[2]+".kml")
        };
        documents = new ArrayList<>();

        //settings = loadOptions(); //TODO
        //saveSettingsObjectly();
    }

    //<editor-fold desc="GETTERY A SETTERY">
    public boolean dataFileExists(){
        //TODO - přepsat files na
        for(File f : files){
            if(!f.exists())return false; //alespoň jeden neexistuje - chyba!
        }
        return true;
    }
    public boolean mapFileExists(){
        return new File(context.getExternalFilesDir(null)+"/Landscape").isDirectory();
        //return new File(context.getExternalFilesDir(null)+"/mapa.zip").exists();
    }


    //metody pro vraceni dokumentů, složek, placemarks
    public ArrayList<Document> getLoadedDocuments(){
        return documents;
    }

    public ArrayList<Placemark> getAllLoadedPlacemarks(){
        ArrayList<Placemark> placemarks = new ArrayList<>();
        for(Document d : documents)
            for(Folder f : d.getFolders())
                for(Placemark p : f.getPlacemarks())
                    placemarks.add(p);
        return placemarks;
    }
    public ArrayList<Placemark> getLoadedPlacemarksWithoutRoads(){
        //jen dva první dokumenty
        ArrayList<Placemark> placemarks = new ArrayList<>();
        for(Folder f : documents.get(0).getFolders())
            for(Placemark p : f.getPlacemarks())
                placemarks.add(p);
        for(Folder f : documents.get(1).getFolders())
            for(Placemark p : f.getPlacemarks())
                placemarks.add(p);
        return placemarks;
    }

    public Placemark getPlacemarkByPopis(String[] vyrazivo){

        for(Placemark p : getAllLoadedPlacemarks()){
            if(p.getType() != null && p.getName() != null)
                //if(p.getType().toString() == vyrazivo[0])
                if(p.getName().toString().contains(vyrazivo[1]))
                    if((p.getCoordinates().get(0).latitude + "").contains(vyrazivo[2]))
                        if((p.getCoordinates().get(0).longitude + "").contains(vyrazivo[3]))
                            return p;
        }

        return null;
    }
    //</editor-fold>

    //<editor-fold desc="NAČÍTÁNÍ & UKLÁDÁNÍ NASTAVENÍ APLIKACE">
    //METODA UKLÁDAJÍCÍ NASTAVENÍ APLIKACE

    //TODO WORKING?
    @Deprecated
    public void saveSettingsObjectly(){
        try {
            File f = new File(context.getExternalFilesDir(null)+"/settings.data");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));

            //out.writeObject(settings);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Deprecated
    public void loadSettingsObjectly(){
        try {
            File f = new File(context.getExternalFilesDir(null)+"/settings.data");

            if(f.exists()){

                ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));

                in.close();
            }else{
                //Zatím nebylo uloženo nastavneí
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //</editor-fold>

    //<editor-fold desc="NAČÍTÁNÍ & UKLÁDÁNÍ STATISTIK">
    //METODA NAČÍTAJÍCÍ PLACEMARKY UŽIVATELE OBOU SEZNAMŮ
    public void loadStatsData(ArrayList<Placemark> climbedList, ArrayList<Placemark> wishList){
        try {
            //climbedList
            if(new File(context.getExternalFilesDir(null)+"/climbedList.txt").exists()){
                File file = new File(context.getExternalFilesDir(null)+"/climbedList.txt"); //pokud neexistuje, bude vytvořen
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                String s;
                while((s = br.readLine()) != null){
                    String[] vyrazivo = s.split(";");
                    climbedList.add(getPlacemarkByPopis(vyrazivo));
                }
                br.close();

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Zatím nebyl vytvořen seznam vylezených", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //wish
            if(new File(context.getExternalFilesDir(null)+"/wishList.txt").exists()){
                File file = new File(context.getExternalFilesDir(null)+"/wishList.txt"); //pokud neexistuje, bude vytvořen
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                String s;
                while((s = br.readLine()) != null){
                    String[] vyrazivo = s.split(";");
                    wishList.add(getPlacemarkByPopis(vyrazivo));
                }
                br.close();

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Zatím nebyl vytvořen seznam přání", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //METODA UKLÁDAJÍCÍ OBA DVA SEZNAMY UŽIVATELE
    public void saveStatsData(ArrayList<Placemark> climbedListArray, ArrayList<Placemark> wishListArray){
        try {
            //climbedList
            File file = new File(context.getExternalFilesDir(null)+"/climbedList.txt"); //pokud neexistuje, bude vytvořen
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            for(Placemark p : climbedListArray){
                bw.write(p.getType().toString()+";"+p.getName()+";"+p.getCoordinates().get(0).latitude+";"+p.getCoordinates().get(0).longitude);
                bw.newLine();
            }

            bw.flush();
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //wishList
            File file = new File(context.getExternalFilesDir(null)+"/wishList.txt"); //pokud neexistuje, bude vytvořen
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            for(Placemark p : wishListArray){
                bw.write(p.getType().toString()+";"+p.getName().toString()+";"+p.getCoordinates().get(0).latitude+";"+p.getCoordinates().get(0).longitude+";");
                bw.newLine();
            }

            bw.flush();
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //</editor-fold>

    //<editor-fold desc="NAČÍTÁNÍ PLACEMARK DAT ZE SOUBORU VČETNĚ PODPŮRNÝCH METOD K PARSOVÁNÍ">
    public void loadData(){

        System.out.println("Mažu původní arrays (pokud vůbec existují? - aktuální počet dokumentů: "+documents.size());
        /*
        if(documents != null)
            if(!documents.isEmpty())
                for(Document d : documents)
                    d.getFolders().clear();
        */
        documents = new ArrayList<>();

        System.out.println("Načítám data z lokálu... ");


        for(int i = 0; i < names.length; i++){
            File f = new File(context.getExternalFilesDir(null)+"/"+names[i]+".kml"); //projdeme soubory - pokud existují? - naplníme do objektů documents

            if(f.exists()){

                documents.add(new Document(names[i],urls[i]));

                try {
                    BufferedReader reader;// = new BufferedReader(new InputStreamReader(inputStream));
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

                    XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(reader);
                    parser.nextTag();

                    while(parser.next() != XmlPullParser.END_DOCUMENT){

                        if(parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("Folder")){
                            Document d = documents.get(documents.size() - 1); //poslední dokument
                            d.addFolder(readFolder(parser, d));
                        }

                    }


                    System.out.println("Načtena všechna data v dokumentu "+ names[i]);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Folder readFolder(XmlPullParser parser, Document d) throws IOException, XmlPullParserException {
        String name = "";
        Folder f = new Folder(d, "Zatím bez názvu");

        String tag = ""; //slouží k vyskočení z elementu

        parser.next();
        while(!tag.equals("Folder")){
            if(parser.getEventType() == XmlPullParser.START_TAG){
                String pName = parser.getName().trim().toLowerCase();
                if(pName.equals("name")){
                    name = parser.nextText();
                    f.setName(name);
                }

                if(pName.equals("placemark")){
                    f.addPlacemark(readPlacemark(parser, f));
                }
            }
            if(parser.getEventType() == XmlPullParser.END_TAG){
                tag = parser.getName();
            }
            parser.next();
        }

        return f;
    }

    private Placemark readPlacemark (XmlPullParser parser, Folder f) throws IOException, XmlPullParserException {

        String name = "";
        String coordinates = "";
        String confirmed = "";
        String description = "";
        EPlacemark type = EPlacemark.POINT; //předpokládejme bod

        String tag = ""; //slouží k vyskočení z elementu

        parser.next();
        while(!tag.equals("Placemark")){
            if(parser.getEventType() == XmlPullParser.START_TAG){
                String pName = parser.getName().trim().toLowerCase();
                switch(pName){
                    case "name": parser.next(); name = parser.getText(); break;
                    case "coordinates": parser.next(); coordinates = parser.getText(); break;
                    case "data":
                        if (parser.getAttributeValue(0).equals("confirmed")) {
                            parser.next(); parser.next(); parser.next();
                            confirmed = parser.getText();
                        }
                        else if (parser.getAttributeValue(0).equals("description")) {
                            parser.next(); parser.next(); parser.next();
                            description = parser.getText();
                        }
                        break;
                    case "point": type = EPlacemark.POINT; break;
                    case "linestring": type = EPlacemark.LINESTRING; break;
                    case "polygon": type = EPlacemark.POLYGON; break;
                    default: break;
                }
            }
            if(parser.getEventType() == XmlPullParser.END_TAG){
                tag = parser.getName();
            }
            parser.next();
        }
        //id změnit?? asi nebude třeba

        Placemark placemark = new Placemark(f,name);
        placemark.setDescription(description);
        placemark.setType(type);
        placemark.addCoordinates(coordinates);
        placemark.setConfirmed(confirmed.equals("1"));

        switch (type){
            case LINESTRING: placemark.setIconId(R.drawable.ic_linestring_b); break;
            case POLYGON: placemark.setIconId(R.drawable.ic_polygon_b); break;
            case POINT:
                if(f.getName().contains("Skál"))placemark.setIconId(R.drawable.ic_marker_b);
                else if(f.getName().contains("Orient"))placemark.setIconId(R.drawable.ic_sign_b);
                else placemark.setIconId(R.drawable.ic_chalkbag_b);
                break;
        }

        return placemark;
    }
    //</editor-fold>

    //<editor-fold desc="STÁHNUTÍ PLACEMARKS Z WEBU">
    public void loadDataFromWeb(){
        try {
            System.out.println("Čtení z webu");

            for(int i = 0; i < urls.length; i++){
                URL u = new URL(urls[i]);
                //URLConnection uConn = u.openConnection();
                //uConn.setConnectTimeout(4000);
                //uConn.setReadTimeout(4000);
                //InputStream inputStream = uConn.getInputStream();

                InputStream inputStream = u.openStream();


                File file = new File(context.getExternalFilesDir(null)+"/"+names[i]+".kml"); //pokud neexistuje, bude vytvořen

                System.out.println("Zapisuji do zařízení soubor "+names[i]+".kml");
                OutputStream outputStream = new FileOutputStream(file);

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                inputStream.close();
                outputStream.close();

                System.out.println("Úspěšně zapsáno...");

            }

            //zapsáno do souborů
            //a rovnou čteme
            loadData();

            //mListener.onDataDownload(); //po download Callback metoda na načtení dat do fragmentů

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //TODO zatím nefunguje
    //<editor-fold desc="STÁHNUTÍ MAPY Z WEBU VČETNĚ ROZBALENÍ ZIP SOUBORU">

    public void downloadZipOfflineMaps(ProgressDialog pd){
        //if(!new File(context.getExternalFilesDir(null)+"/mapa.zip").exists())
        try {
            System.out.println("Mapa není v zařízení..");
            System.out.println("Připravuji stahování..");


            URL u = new URL("https://www.dropbox.com/s/00f0azgl86ql3v4/mapa.zip?raw=1");

            InputStream inputStream = u.openStream();

            System.out.println(inputStream);

            inputStream = u.openStream();
            System.out.println(inputStream);
            //TODO nestáhne se celá -_-

            File file = new File(context.getExternalFilesDir(null)+"/mapa.zip"); //pokud neexistuje, bude vytvořen

            System.out.println("Zapisuji do zařízení soubor mapa.zip");
            OutputStream outputStream = new FileOutputStream(file);


            byte[] buf = new byte[1024];
            long total = 0;
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                total += len;
                pd.setProgress((int) (total * 100 / 45985792)); //velikost souboru dle sout = 45983195
                //System.out.println(total);
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();

            outputStream.close();
            inputStream.close();

            System.out.println("Úspěšně zapsáno...");

            //zapsáno do souborů
            //a rovnou čteme
            unpackZip(context.getExternalFilesDir(null) + "/", "mapa.zip");

            file.delete(); // a smažeme soubor, když už máme složku

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        else{
            unpackZip(context.getExternalFilesDir(null) + "/", "mapa.zip");
        }*/
    }

    private boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("Nepovedlo se rozbalit mapu -_-");
            return false;
        }

        return true;
    }
    //</editor-fold>
}
