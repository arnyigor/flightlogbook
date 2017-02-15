package com.arny.flightlogbook.views.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.arny.flightlogbook.models.DataList;
import com.arny.flightlogbook.models.DatabaseHandler;
import com.arny.flightlogbook.R;

import java.util.List;

public class AirplaneTypesActivity extends AppCompatActivity {
    private static final String TAG = "LOG_TAG";
    private Button add, removeall;
    private Context context = this;
    private DatabaseHandler db;
    private ListView typeslistView;
    private List<DataList> ListTypes;
    private int dlgPosition = 0;


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
        db = new DatabaseHandler(this);

        add = (Button) findViewById(R.id.addType);
        removeall = (Button) findViewById(R.id.removeallTypes);
        typeslistView = (ListView) findViewById(R.id.typelistView);

        typeslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "View = " + view);
                Log.i(TAG, "position = " + position);
                Log.i(TAG, "id = " + id);
            }
        });

        removeall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle(getString(R.string.str_delete) + "?");
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.removeAllTypes();
                                ListTypes = db.getTypeList();
                                typeslistView.setAdapter(new TypesAdapter());
                                setVisbltyBtnRemAll();
                            }
                        })
                        .setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
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

    public void DlgAddType() {
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
                    db.addType(edtTypeInput.getText().toString());
                    ListTypes = db.getTypeList();
                    typeslistView.setAdapter(new TypesAdapter());
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
        List<DataList> dbl = (db.getTypeItem(ListTypes.get(dlgPosition).getAirplanetypeid()));
        for (DataList item : dbl) {
            edtTypeInput.setText(item.getAirplanetypetitle());
        }
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
                db.updateType(edtTypeInput.getText().toString(), ListTypes.get(dlgPosition).getAirplanetypeid());
                ListTypes = db.getTypeList();
                typeslistView.setAdapter(new TypesAdapter());
                setVisbltyBtnRemAll();
            }
        });
        alert.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume ");
        setVisbltyBtnRemAll();
        typeslistView.setAdapter(new TypesAdapter());
    }
    private void setVisbltyBtnRemAll(){
        ListTypes = db.getTypeList();
        if (ListTypes.size() < 1) removeall.setEnabled(false);
        else removeall.setEnabled(true);
    }
    public class TypesAdapter extends BaseAdapter {

        LayoutInflater mInflater;

        TypesAdapter() {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return db.getTypeCount();
        }

        @Override
        public Object getItem(int position) {
            return db.getTypeItem(ListTypes.get(position).getAirplanetypeid());
        }

        @Override
        public long getItemId(int position) {
            return ListTypes.get(position).getAirplanetypeid();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.typeitem, null);
            }
            final TextView typeText = (TextView) convertView.findViewById(R.id.nameText);
            typeText.setText(getString(R.string.str_airplane_type) + ":" + ListTypes.get(position).getAirplanetypetitle());
            final ImageButton edit = (ImageButton) convertView.findViewById(R.id.edit);
            edit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DlgEdtType(position);
                }
            });
            final ImageButton delete = (ImageButton) convertView.findViewById(R.id.delete);
            delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle(getString(R.string.str_delete) + "?");
                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    db.removeType(ListTypes.get(position).getAirplanetypeid());
                                    notifyDataSetChanged();
									/*ListTypes = db.getDataList();
									listView.setAdapter(new ViewAdapter());*/
                                    setVisbltyBtnRemAll();
                                }
                            })
                            .setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
	        /*convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "getId: " + ListTypes.get(position).getAirplanetypeid());
                }
            });*/
            return convertView;
        }
    }
}
