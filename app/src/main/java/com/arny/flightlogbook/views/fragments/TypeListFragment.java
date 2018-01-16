package com.arny.flightlogbook.views.fragments;

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
import com.arny.arnylib.adapters.BindableViewHolder;
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

import java.util.List;

public class TypeListFragment extends Fragment implements TypeListHolder.SimpleActionListener, View.OnClickListener {
	private Context context;
	private List<Type> types;
	private SimpleBindableAdapter<Type, TypeListHolder> typeListAdapter;
	private Button removeall;

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
					Type type = Local.getTypeItem(id, context);
					types.add(type);
					typeListAdapter.add(type);
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
		final Type type = Local.getTypeItem(types.get(pos).getTypeId(), context);
		DroidUtils.alertConfirmDialog(context, getString(R.string.str_remove_airplane_types), () -> {
			Local.removeType(type.getTypeId(), context);
			types.remove(pos);
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
		Utility.mainThreadObservable(Observable.fromCallable(() -> Local.getTypeList(context))).subscribe(types1 -> {
			types = types1;
			typeListAdapter.clear();
			typeListAdapter.addAll(types);
			setVisbltyBtnRemAll();
		});
	}

	private void setVisbltyBtnRemAll() {
		if (types != null) {
			removeall.setEnabled(types.size() >= 1);
		}
	}
}
