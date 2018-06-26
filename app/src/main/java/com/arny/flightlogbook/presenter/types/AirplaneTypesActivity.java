package com.arny.flightlogbook.presenter.types;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.arny.arnylib.utils.Utility;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.data.models.Flight;
import com.arny.flightlogbook.data.source.MainRepository;
import io.reactivex.functions.BiFunction;

public class AirplaneTypesActivity extends AppCompatActivity {
	private final MainRepository repository = new  MainRepository();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_airplane_types);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		toolbar.setTitle(R.string.str_airplane_types);
		toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorText));
		TypeListFragment fragment = new TypeListFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				super.onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
