package com.example.sitnik.onetoonechat;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/***adapter to cards in main leyout*/
class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                return new RequestFragment();

            case 1:
                return new GroupsFragment();

            case 2:
                return new FriendsFragment();

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "REQUEST";

            case 1:
                return "GROUPS";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }
    }
}
