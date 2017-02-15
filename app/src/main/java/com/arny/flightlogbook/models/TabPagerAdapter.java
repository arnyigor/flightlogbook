package com.arny.flightlogbook.models;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.arny.flightlogbook.views.fragments.FlightList;
import com.arny.flightlogbook.views.fragments.StatisticFragment;

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new FlightList();
            case 1:
                return new StatisticFragment();
        }
        return null;

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title=" ";
        switch (position){
            case 0:
                title="Fragment one";
                break;
            case 1:
                title="Fragment two";
                break;
            case 2:
                title="Fragment splash";
                break;
        }

        return title;
    }

    }