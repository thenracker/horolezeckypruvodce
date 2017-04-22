package cz.weissar.horolezeckypruvodce.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import java.util.ArrayList;

/**
 * Created by petrw on 17.10.2015.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    //ikonky tabu
    private Context context;
    private ArrayList<FragmentE> fragments = new ArrayList<>();
    private boolean iconsVsTexts;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        this.iconsVsTexts = true;
        fragments.add(new FragmentSearch());
        fragments.add(new FragmentDetail());
        fragments.add(new FragmentStats());
        fragments.add(new FragmentMap());
    }

    public void switchIconsForText(){
        iconsVsTexts = !iconsVsTexts;
    }
    public void switchIconsForText(boolean wannaIcons){
        iconsVsTexts = wannaIcons;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        if(iconsVsTexts){ //ikona
            Drawable image = context.getResources().getDrawable(fragments.get(position).getIcon());
            //image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
            image.setBounds(0,0,46,46); //46 46 protože ikony jsou 92x92 - což / 2 dává 46
            SpannableString sb = new SpannableString(" ");// + fragments.get(position).getTitle().toUpperCase());
            ImageSpan iS = new ImageSpan(image, ImageSpan.ALIGN_BASELINE);
            sb.setSpan(iS,0,sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
        else{ //text
            return fragments.get(position).getTitle().toUpperCase(); //return new SpannableString(fragments.get(position).getTitle().toUpperCase());
        }
    }

    public CharSequence getActivePageTitle(int position){

        if(iconsVsTexts){ //ikona
            Drawable image = context.getResources().getDrawable(fragments.get(position).getIconActive());
            //image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
            image.setBounds(0,0,46,46);
            SpannableString sb = new SpannableString(" ");// + fragments.get(position).getTitle().toUpperCase());
            ImageSpan iS = new ImageSpan(image, ImageSpan.ALIGN_BASELINE);
            sb.setSpan(iS,0,sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
        else{ //text
            return fragments.get(position).getTitle().toUpperCase(); //new SpannableString(fragments.get(position).getTitle().toUpperCase());
        }
    }
    //nadpis aplikace
    public CharSequence getPageTitleAndIconLeft(int position){
        //return fragments.get(position).getTitle();
        Drawable image = context.getResources().getDrawable(fragments.get(position).getIconTitle());
        //image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
        image.setBounds(0,0,46,46);
        SpannableString sb = new SpannableString("   " + fragments.get(position).getTitle().toUpperCase());
        ImageSpan iS = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(iS,0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

}
