package cz.weissar.horolezeckypruvodce.data;

/**
 * Created by petrw on 31.10.2015.
 */
public interface IElement {

    public String getName();
    public void setName(String name);
    public EElement getElementType();
    //a asi na interface sere pes.. neni to nejspis treba

    /*
    Popis kml souboru:
    ELEMENTY:
    DOCUMENT = OBLAST (v podstatě celý KML soubor)
        obsahuje:
        "name" - to jest název hor (skalní oblasti)
    FOLDER = SEZNAM ELEMENTŮ STEJNÉHO TYPU - tedy FOLDER má getName, který tvoří skupinu
        obsahuje:
        "Skály"
        "Cesty"
        "Žlutá turistická"
        "Oblasti"
        "Orientační body"
        "Vrstva bez názvu"
    PLACEMARK = JEDEN KONKRÉTNÍ OBJEKT NA MAPĚ
        obsahuje:
        POINT - coordinates 3 desetinná čísla oddělená "," && obsahuje <Data name='confirmed'><Value>1 a <Data name='description'><Value>
        LINESTRING - coordinates skupina 3 čísel oddělené "," a coordinates odděleno " "
        POLYGON - coordinates skupina 3 čísel oddělené "," a coordinates odděleno " "

    Hierarchie:
    DOCUMENT 1<---* FOLDER 1<---* PLACEMARK <--generalize-- POINT / LINESTRING / POLYGON

    Model tříd:
    DOCUMENT    attr(String name, String description, ArrayList<FOLDER> folders)
    FOLDER      attr(String name, ArrayList<PLACEMARK> placemarks)
    PLACEMARK   attr(String name, String description, String[] extendedData, EPlacemark typ, LatLng[] coordinates)
        POINT       attr(typ = EPlacemark.POINT,
        LINESTRING  attr(typ = EPlacemark.LINESRING,
        POLYGON     attr(typ = EPlacemark.POLYGON,

    EPLACEMARK = enum TYP PLACEMARK
    IELEMENT = obsahuje
    DATAMANAGER = má pro každý Dokument objekt s podobjekty.. jak to šlape
     */
}
