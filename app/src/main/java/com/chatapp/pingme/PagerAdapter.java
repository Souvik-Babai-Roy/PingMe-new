package com.chatapp.pingme;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    // Number of tabs
    private int tabCount;

    // Constructor
    public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.tabCount = behavior;
    }

    // Return the fragment for each tab
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new chatFragment();
            case 1:
                return new statusFragment();
            case 2:
                return new callFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chat";
            case 1:
                return "Call";
            case 2:
                return "Status";
            default:
                return null;
        }
    }

    // Return the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }
}
