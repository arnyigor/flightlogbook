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
import android.view.*;

import android.widget.Button;
import android.widget.Toast;
import com.arny.arnylib.adapters.SimpleBindableAdapter;
import com.arny.arnylib.interfaces.InputDialogListener;
import com.arny.arnylib.utils.DroidUtils;
import com.arny.arnylib.utils.Utility;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.adapter.TypeListHolder;
import com.arny.flightlogbook.data.Local;
import com.arny.flightlogbook.data.models.AircraftType;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import java.util.List;

public class TypeListFragment extends Fragment implements TypeListHolder.SimpleActionListener, View.OnClickListener {
	private Context context;
	private List<AircraftType> aircraftTypes;
	private SimpleBindableAdapter<AircraftType, TypeListHolder> typeListAdapter;
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
		typeListAdapter = new SimpleBindableAdapter<>(context, R.layout.typeitem, TypeListHolder.class);
		typeListAdapter.setActionListener(this);
		recyclerView.setAdapter(typeListAdapter);
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
	public void onItemClick(int position, Object Item) {

	}

	@Override
	public void OnTypeEdit(int position) {
		DlgEdtType(position);
	}

	@Override
	public void OnTypeDelete(int position) {
		DlgRemoveType(position);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.removeallTypes:
				DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete), () -> {
					Local.removeAllTypes(context);
					loadList();
					setVisbltyBtnRemAll();
				});
				break;
			case R.id.addType:
				DlgAddType();
				break;
		}
	}

	//str_add_airplane_types
	public void DlgAddType() {
		DroidUtils.simpleInputDialog(context, getString(R.string.str_add_airplane_types), getString(R.string.str_ok), getString(R.string.str_cancel), InputType.TYPE_CLASS_TEXT, new InputDialogListener() {
			@Override
			public void onConfirm(String content) {
				if (!Utility.empty(content)) {
					int id = (int) Local.addType(content, context);
					AircraftType aircraftType = Local.getTypeItem(id, context);
					aircraftTypes.add(aircraftType);
					typeListAdapter.add(aircraftType);
					setVisbltyBtnRemAll();
				} else {
					Toast.makeText(context, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onError(String error) {
				Toast.makeText(context, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
			}
		});
	}

	public void DlgEdtType(final int pos) {
		final AircraftType aircraftType = Local.getTypeItem(aircraftTypes.get(pos).getTypeId(), context);
		DroidUtils.simpleInputDialog(context, getString(R.string.str_edt_airplane_types), "", aircraftType.getTypeName(), getString(R.string.str_ok), getString(R.string.str_cancel), InputType.TYPE_CLASS_TEXT, new InputDialogListener() {
			@Override
			public void onConfirm(String content) {
				if (!Utility.empty(content)) {
					AircraftType t = new AircraftType();
					t.setTypeId(aircraftType.getTypeId());
					t.setTypeName(content);
					Local.updateType(content, aircraftType.getTypeId());
					aircraftTypes.set(pos, t);
					typeListAdapter.set(pos, t);
				} else {
					Toast.makeText(context, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onError(String error) {
				Toast.makeText(context, R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
			}
		});
	}

	public void DlgRemoveType(final int pos) {
		final AircraftType aircraftType = Local.getTypeItem(aircraftTypes.get(pos).getTypeId(), context);
		DroidUtils.alertConfirmDialog(context, getString(R.string.str_remove_airplane_types), () -> {
			Local.removeType(aircraftType.getTypeId(), context);
			aircraftTypes.remove(pos);
			typeListAdapter.removeChild(pos);
			setVisbltyBtnRemAll();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		loadList();
	}

	private void loadList() {
		disposable.add(Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getTypeList(context))).subscribe(types1 -> {
			aircraftTypes = types1;
			typeListAdapter.clear();
			typeListAdapter.addAll(aircraftTypes);
			setVisbltyBtnRemAll();
		}));
	}

	private void setVisbltyBtnRemAll() {
		if (aircraftTypes != null) {
			removeall.setVisibility(aircraftTypes.size() >= 1 ? View.VISIBLE : View.GONE);
		}
	}
}