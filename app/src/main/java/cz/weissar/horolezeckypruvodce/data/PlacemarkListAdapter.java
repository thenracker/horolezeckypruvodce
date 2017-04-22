package cz.weissar.horolezeckypruvodce.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cz.weissar.horolezeckypruvodce.MainActivity;
import cz.weissar.horolezeckypruvodce.R;

/**
 * Created by petrw on 14.11.2015.
 */
public class PlacemarkListAdapter extends ArrayAdapter<Placemark> {

    private final Context context;
    private final int resource;
    private final List<Placemark> placemarks;

    private MainActivity mainActivity;

    public PlacemarkListAdapter(Context context, int resource, List<Placemark> objects, MainActivity mainActivity) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.placemarks = objects;
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.mylistview, null, true);

        TextView txtName = (TextView) rowView.findViewById(R.id.name);
        TextView txtFolder = (TextView) rowView.findViewById(R.id.folder);
        ImageView imgView = (ImageView) rowView.findViewById(R.id.icon);

        txtName.setText(placemarks.get(position).getName());
        txtFolder.setText(placemarks.get(position).getFolder().getDocument().getName() + "\\" + placemarks.get(position).getFolder().getName());
        imgView.setImageResource(placemarks.get(position).getIconId());

        ImageView imgClimbedOrWished = (ImageView) rowView.findViewById(R.id.iconClimbedORWished);
        //IKONKY ZDA-LI JE OBJEKT V SEZNAMU
        if(getMainActivity().getFragmentStats().isInWished(getItem(position)))
            imgClimbedOrWished.setImageResource(R.drawable.ic_done_black);

        if(getMainActivity().getFragmentStats().isInClimbed(getItem(position)))
            imgClimbedOrWished.setImageResource(R.drawable.ic_done_all_black);


        ImageView imgMore = (ImageView) rowView.findViewById(R.id.more);
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Placemark item = getItem(position);

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
                        //kedyby se změnil stav objektu, tak se překreslí ikona v řádku
                        getMainActivity().getFragmentSearch().listActualization();
                    }
                }).show();

            }
        });

        return rowView;
    }

    private MainActivity getMainActivity(){
        return this.mainActivity;
    }

    @Override
    public Placemark getItem(int position) {
        return super.getItem(position);
    }
}
