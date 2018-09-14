package com.arny.flightlogbook.presenter.types;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.arny.flightlogbook.R;
import com.arny.flightlogbook.adapter.FlightTypesAdapter;
import com.arny.flightlogbook.data.Local;
import com.arny.flightlogbook.data.models.AircraftType;
import com.arny.flightlogbook.utils.Utility;
import com.arny.flightlogbook.utils.dialogs.ConfirmDialogListener;
import com.arny.flightlogbook.utils.dialogs.DialogsKt;
import com.arny.flightlogbook.utils.dialogs.InputDialogListener;

import io.reactivex.disposables.CompositeDisposable;

public class TypeListFragment extends Fragment implements View.OnClickListener {
	private Context context;
	private FlightTypesAdapter adapter;
	private Button removeall;
	private final CompositeDisposable disposable = new CompositeDisposable();

	public TypeListFragment() {
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.context = context;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_type_list, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disposable.clear();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Button add = view.findViewById(R.id.addType);
		removeall = view.findViewById(R.id.removeallTypes);
		RecyclerView recyclerView = view.findViewById(R.id.typelistView);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		adapter = new FlightTypesAdapter(context, new FlightTypesAdapter.FlightTypesListener() {
			@Override
			public void onTypeEdit(int position) {
				DlgEdtType(position);
			}

			@Override
			public void onTypeDelete(int position) {
				DlgRemoveType(position);
			}

			@Override
			public void onHolderItemClick(View view, int position, Object item) {

			}
		});
		recyclerView.setAdapter(adapter);
		removeall.setOnClickListener(this);
		add.setOnClickListener(this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.removeallTypes:
//				DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete), () -> {
//					Local.removeAllTypes(context);
//					loadList();
//					setVisbltyBtnRemAll();
//				});
				break;
			case R.id.addType:
				DlgAddType();
				break;
		}
	}

	//str_add_airplane_types
	public void DlgAddType() {
		DialogsKt.inputDialog(context, getString(R.string.str_add_airplane_types), "", "", getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, new InputDialogListener() {
			@Override
			public void onConfirm(String content) {
				if (!Utility.empty(content)) {
					int id = (int) Local.addType(content, context);
					AircraftType aircraftType = Local.getTypeItem(id, context);
					adapter.add(aircraftType);
					setVisbltyBtnRemAll();
				} else {
					Toast.makeText(context, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancel() {

			}

		});
	}

	public void DlgEdtType(final int pos) {
		final AircraftType aircraftType = Local.getTypeItem(adapter.getItem(pos).getTypeId(), context);
		DialogsKt.inputDialog(context, getString(R.string.str_edt_airplane_types), "", aircraftType.getTypeName(), getString(R.string.str_ok), getString(R.string.str_cancel), false, InputType.TYPE_CLASS_TEXT, new InputDialogListener() {
			@Override
			public void onConfirm(String content) {
				if (!Utility.empty(content)) {
					AircraftType t = new AircraftType();
					t.setTypeId(aircraftType.getTypeId());
					t.setTypeName(content);
					Local.updateType(content, aircraftType.getTypeId());
					adapter.add(pos, t);
				} else {
					Toast.makeText(context, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancel() {

			}
		});
	}

	public void DlgRemoveType(int pos) {
		final AircraftType aircraftType = Local.getTypeItem(adapter.getItem(pos).getTypeId(), context);
		DialogsKt.confirmDialog(context, "", null, "Да", "Нет", false, new ConfirmDialogListener() {
			@Override
			public void onConfirm() {
				Local.removeType(aircraftType.getTypeId(), context);
				adapter.remove(pos);
				setVisbltyBtnRemAll();
			}

			@Override
			public void onCancel() {

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void loadList() {
//		disposable.add(Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getTypeList(context))).subscribe(types1 -> {
//			aircraftTypes = types1;
//			adapter.clear();
//			adapter.addAll(aircraftTypes);
//			setVisbltyBtnRemAll();
//		}));
	}

	private void setVisbltyBtnRemAll() {
		removeall.setVisibility(adapter.getItems().size() >= 1 ? View.VISIBLE : View.GONE);
	}
}
