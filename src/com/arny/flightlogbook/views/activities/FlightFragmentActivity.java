package com.arny.flightlogbook.views.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.views.fragments.FlightList;

public class FlightFragmentActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        if (findViewById(R.id.container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            FlightList firstFragment = new FlightList();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, firstFragment)
                    .commit();
        }
    }
}