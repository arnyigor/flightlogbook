package com.arny.flightlogbook.views.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.arny.arnylib.adapters.SimpleBindableAdapter;
import com.arny.arnylib.interfaces.AlertDialogListener;
import com.arny.arnylib.utils.DroidUtils;
import com.arny.flightlogbook.BuildConfig;
import com.arny.flightlogbook.adapter.FlightListHolder;
import com.arny.flightlogbook.adapter.TypeListHolder;
import com.arny.flightlogbook.common.Local;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.Type;

import java.util.List;

public class AirplaneTypesActivity extends AppCompatActivity {
    private static final String TAG = "LOG_TAG";
    private Button add, removeall;
    private Context context = this;
    private List<Type> types;
    private int dlgPosition = 0;
    private RecyclerView recyclerView;
    private SimpleBindableAdapter<Type, TypeListHolder> typeListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.typelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setTitle(R.string.str_airplane_types);
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.colorText));

        add = (Button) findViewById(R.id.addType);
        removeall = (Button) findViewById(R.id.removeallTypes);

        recyclerView = (RecyclerView) findViewById(R.id.typelistView);
        recyclerView.setLayoutManager( new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        typeListAdapter = new SimpleBindableAdapter<>(context,R.layout.typeitem, TypeListHolder.class);

        removeall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DroidUtils.alertConfirmDialog(context, getString(R.string.str_delete), new AlertDialogListener() {
                    @Override
                    public void onConfirm() {
                        Local.removeAllTypes(context);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreatseOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.add_edit_menu, menu);
//        return true;
//    }

    //str_add_airplane_types
    public void DlgAddType() {
        DroidUtils.in//// TODO: 28.08.2017  
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        final EditText edtTypeInput = new EditText(this);
        final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        edtTypeInput.setLayoutParams(lparams);
        alert.setTitle(getString(R.string.str_add_airplane_types));
        layout.addView(edtTypeInput);
        alert.setView(layout);
        alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!edtTypeInput.getText().toString().equals("") && edtTypeInput.getText().toString().length() > 0) {
                    Local.addType(edtTypeInput.getText().toString(), context);
                    types = Local.getTypeList(context);
                    setVisbltyBtnRemAll();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.show();
    }

    public void DlgEdtType(int pos) {
        dlgPosition = pos;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        final EditText edtTypeInput = new EditText(this);
	    Type type = Local.getTypeItem(types.get(dlgPosition).getTypeId(), context);;
	    edtTypeInput.setText(type.getTypeName());
        final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        edtTypeInput.setLayoutParams(lparams);
        alert.setTitle(getString(R.string.str_edt_airplane_types));
        layout.addView(edtTypeInput);
        alert.setView(layout);
        alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Local.updateType(edtTypeInput.getText().toString(), types.get(dlgPosition).getTypeId());
                types = Local.getTypeList(context);
                setVisbltyBtnRemAll();
            }
        });
        alert.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        types = Local.getTypeList(context);
        typeListAdapter.addAll(types);
        setVisbltyBtnRemAll();
    }
    private void setVisbltyBtnRemAll(){
        if (types.size() < 1) removeall.setEnabled(false);
        else removeall.setEnabled(true);
    }
}
