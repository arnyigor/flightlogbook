package com.arny.flightlogbook.views.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.arny.arnylib.adapters.SimpleBindableAdapter;
import com.arny.arnylib.interfaces.AlertDialogListener;
import com.arny.arnylib.interfaces.InputDialogListener;
import com.arny.arnylib.utils.DroidUtils;
import com.arny.arnylib.utils.Utility;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.adapter.TypeListHolder;
import com.arny.flightlogbook.common.Local;
import com.arny.flightlogbook.models.Type;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import java.util.List;

public class AirplaneTypesActivity extends AppCompatActivity implements TypeListHolder.SimpleActionListener {
	private Button removeall;
	private Context context = this;
	private List<Type> types;
	private SimpleBindableAdapter<Type, TypeListHolder> typeListAdapter;
	private final CompositeDisposable disposable = new CompositeDisposable();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.typelist);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		toolbar.setTitle(R.string.str_airplane_types);
		toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorText));

		Button add = findViewById(R.id.addType);
		removeall = findViewById(R.id.removeallTypes);
		RecyclerView recyclerView = findViewById(R.id.typelistView);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		typeListAdapter = new SimpleBindableAdapter<>(context, R.layout.typeitem, TypeListHolder.class);
		typeListAdapter.setActionListener(this);
		recyclerView.setAdapter(typeListAdapter);
		removeall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete), new AlertDialogListener() {
					@Override
					public void onConfirm() {
						Local.removeAllTypes(context);
						loadList();
						setVisbltyBtnRemAll();
					}
				});
			}
		});

		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				DlgAddType();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disposable.clear();
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

	//str_add_airplane_types
	public void DlgAddType() {
		DroidUtils.simpleInputDialog(context, getString(R.string.str_add_airplane_types), getString(R.string.str_ok), getString(R.string.str_cancel), InputType.TYPE_CLASS_TEXT, new InputDialogListener() {
			@Override
			public void onConfirm(String content) {
				if (!Utility.empty(content)) {
					int id = (int) Local.addType(content, context);
					Type type = Local.getTypeItem(id, context);
					types.add(type);
					typeListAdapter.add(type);
					setVisbltyBtnRemAll();
				} else {
					Toast.makeText(getApplicationContext(), R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onError(String error) {
				Toast.makeText(getApplicationContext(), R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
			}
		});
	}

	public void DlgEdtType(final int pos) {
		final Type type = Local.getTypeItem(types.get(pos).getTypeId(), context);
		DroidUtils.simpleInputDialog(context, getString(R.string.str_edt_airplane_types), "", type.getTypeName(), getString(R.string.str_ok), getString(R.string.str_cancel), InputType.TYPE_CLASS_TEXT, new InputDialogListener() {
			@Override
			public void onConfirm(String content) {
				if (!Utility.empty(content)) {
					Type t = new Type();
					t.setTypeId(type.getTypeId());
					t.setTypeName(content);
					Local.updateType(content, type.getTypeId());
					types.set(pos, t);
					typeListAdapter.set(pos, t);
				} else {
					Toast.makeText(getApplicationContext(), R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onError(String error) {
				Toast.makeText(getApplicationContext(), R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
			}
		});
	}

	public void DlgRemoveType(final int pos) {
		final Type type = Local.getTypeItem(types.get(pos).getTypeId(), context);
		DroidUtils.alertConfirmDialog(context, getString(R.string.str_remove_airplane_types), new AlertDialogListener() {
			@Override
			public void onConfirm() {
				Local.removeType(type.getTypeId(), context);
				types.remove(pos);
				typeListAdapter.removeChild(pos);
				setVisbltyBtnRemAll();
			}
		});
	}

	private void loadList() {
		disposable.add(Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getTypeList(context))).subscribe(types1 -> {
			types = types1;
			typeListAdapter.clear();
			typeListAdapter.addAll(types);
			setVisbltyBtnRemAll();
		}));
	}

	private void setVisbltyBtnRemAll() {
		removeall.setEnabled(types.size() >= 1);
	}

	@Override
	public void OnItemClickListener(int position, Object Item) {

	}

	@Override
	public void OnTypeEdit(int position) {
		DlgEdtType(position);
	}

	@Override
	public void OnTypeDelete(int position) {
		DlgRemoveType(position);
	}
}
