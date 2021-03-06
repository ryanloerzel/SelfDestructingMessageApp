package com.spacecasestudios.messagemonster.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;

import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.ui.FriendsFragment;
import com.spacecasestudios.messagemonster.ui.InboxFragment;

import java.util.Locale;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    protected Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new InboxFragment();
            case 1:
                return new FriendsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return mContext.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return mContext.getString(R.string.title_section2).toUpperCase(l);
            case 2:
                return mContext.getString(R.string.title_section3).toUpperCase(l);
        }
        return null;
    }

    public int getIcon(int position){
        switch(position){
            case 0:
                return R.drawable.ic_inbox;
            case 1:
                return R.drawable.ic_friends;
        }
        return R.drawable.ic_inbox;
    }
}

