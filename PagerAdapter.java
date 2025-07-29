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
                return new callFragment();
            case 2:
                return new statusFragment();
            default:
                return new chatFragment(); // Default to chat fragment
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Calls";
            case 2:
                return "Status";
            default:
                return "Chats";
        }
    }

    // Return the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }
}