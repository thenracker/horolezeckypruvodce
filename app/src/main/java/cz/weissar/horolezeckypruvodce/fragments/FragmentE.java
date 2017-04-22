package cz.weissar.horolezeckypruvodce.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.weissar.horolezeckypruvodce.MainActivity;
import cz.weissar.horolezeckypruvodce.R;

/**
 * Created by petrw on 17.10.2015.
 */
public class FragmentE extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static int icon = R.drawable.ic_climber_b;
    private static int iconActive = R.drawable.ic_climber_b;
    private static int iconTitle = R.drawable.ic_climber_w;
    private static String title = "TAB";

    private static CharSequence imgIcon;
    private static CharSequence imgIconActive;
    private static CharSequence imgIconTitle;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    /*
    public FragmentE newInstance(int sectionNumber) {
        FragmentE fragment = new FragmentE();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
*/

    public FragmentE() {
        //FragmentE fragment = new FragmentE();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, 0);
        setArguments(args);
        //vytvořit ikony
    }

    protected MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        return rootView;
    }
    public int getIcon(){
        return icon;
    }
    public int getIconActive(){
        return iconActive;
    }
    public int getIconTitle(){ return iconTitle; }
    public String getTitle(){
        return title;
    }

    public static CharSequence getImgIcon() {
        return imgIcon;
    }
    public static void setImgIcon(CharSequence imgIcon) {
        imgIcon = imgIcon;
    }

    public static CharSequence getImgIconActive() {
        return imgIconActive;
    }

    public static CharSequence getImgIconTitle() {
        return imgIconTitle;
    }
    protected void makeImgIcons(){

        //TODO na hovno a nefunkční - zatím :(
        Drawable image = getActivity().getBaseContext().getResources().getDrawable(getIcon());
        image.setBounds(0,0,46,46); //46 46 protože ikony jsou 92x92 - což / 2 dává 46
        SpannableString sb = new SpannableString("");// + fragments.get(position).getTitle().toUpperCase());
        ImageSpan iS = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(iS,0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        imgIcon = sb;

        image = getActivity().getBaseContext().getResources().getDrawable(getIconActive());
        image.setBounds(0,0,46,46); //46 46 protože ikony jsou 92x92 - což / 2 dává 46
        sb = new SpannableString("");// + fragments.get(position).getTitle().toUpperCase());
        iS = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(iS,0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        imgIconActive = sb;

        image = getActivity().getBaseContext().getResources().getDrawable(getIconTitle());
        image.setBounds(0,0,46,46); //46 46 protože ikony jsou 92x92 - což / 2 dává 46
        sb = new SpannableString("");// + fragments.get(position).getTitle().toUpperCase());
        iS = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(iS,0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        imgIconTitle = sb;
    }
}

