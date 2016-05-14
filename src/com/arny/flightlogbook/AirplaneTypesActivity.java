package com.arny.flightlogbook;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

public class AirplaneTypesActivity extends Activity {
    private static final String TAG = "LOG_TAG";
    Button add,removeall;
    Context context = this;
    DatabaseHandler db;
    ListView typeslistView;
    List<DataList> ListTypes;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.typelist);
        db = new DatabaseHandler(this);

        add = (Button)findViewById(R.id.addType);
        removeall = (Button)findViewById(R.id.removeallTypes);
        typeslistView = (ListView)findViewById(R.id.typelistView);

        typeslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "View = "+view);
                Log.i(TAG, "position = "+position);
                Log.i(TAG, "id = "+id);
            }
        });

        removeall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("Delete item?");
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                db.removeAllTypes();
                                ListTypes = db.getTypeList();
                                typeslistView.setAdapter(new TypesAdapter());
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
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
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.typeedit);
                dialog.setTitle("Add Data to Database");
                final EditText typeTitle = (EditText) dialog.findViewById(R.id.edtTypeTitle);
                Button Add = (Button) dialog.findViewById(R.id.btnAddType);
                Add.setText("Add");
                Add.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!typeTitle.getText().toString().equals("") && typeTitle.getText().toString().length() > 0 ){
                            db.addType(typeTitle.getText().toString());
                            ListTypes = db.getTypeList();
                            typeslistView.setAdapter(new TypesAdapter());
                            dialog.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(), R.string.str_alarm_add_airplane_type, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    public class TypesAdapter extends BaseAdapter {

        LayoutInflater mInflater;

        public TypesAdapter() {
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
                convertView = mInflater.inflate(R.layout.typeitem,null);
            }
            final TextView nameText = (TextView) convertView.findViewById(R.id.nameText);
            nameText.setText(getString(R.string.str_airplane_type) + ":" + ListTypes.get(position).getAirplanetypetitle());
            final Button edit = (Button) convertView.findViewById(R.id.edit);
            edit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context);
					dialog.setContentView(R.layout.typeedit);
					dialog.setTitle("Update Data in Database");
					final EditText typeTitle = (EditText) dialog.findViewById(R.id.edtTypeTitle);
					List<DataList> dbl =(db.getTypeItem(ListTypes.get(position).getAirplanetypeid()));
					for (DataList item : dbl) {
                        typeTitle.setText(item.getAirplanetypetitle());
					}
					Button Add = (Button) dialog.findViewById(R.id.btnAddType);
					Add.setText("Update");
					Add.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							db.updateType(typeTitle.getText().toString(),ListTypes.get(position).getAirplanetypeid());
							ListTypes = db.getTypeList();
                            typeslistView.setAdapter(new TypesAdapter());
							dialog.dismiss();
						}
					});
					dialog.show();
                }
            });
            final Button delete = (Button) convertView.findViewById(R.id.delete);
            delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("Delete item?");
                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    db.removeType(ListTypes.get(position).getAirplanetypeid());
                                    notifyDataSetChanged();
									/*ListTypes = db.getDataList();
									listView.setAdapter(new ViewAdapter());*/
                                }
                            })
                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume ");
        ListTypes = db.getTypeList();
        typeslistView.setAdapter(new TypesAdapter());
    }
}
