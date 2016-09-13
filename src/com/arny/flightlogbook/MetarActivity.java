package com.arny.flightlogbook;
// imports start==========
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
// imports end==========

//==============Activitystart=========================
public class MetarActivity extends AppCompatActivity {
    private static final String TAG = "LOG_TAG";
    private static final String PARSE_LINK = "http://meteocenter.asia/?m=aopa&p=";
    // =============Variables_start================
    Button btnMetar;
    TextView tvMetar;
    EditText edtIcaoCode;
    TextInputLayout edtIcaoCodeLayout;
    String strIcao;
    boolean startParse,finishParse,hasAutotaf;
    List<String> Metarlist;
    ArrayAdapter<String> metaradapter;
    ActionBar actionBar;
    // =============Variable_send================
// ====================onCreatestart=========================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metar);
        // ================Forms Ids start=========================
        tvMetar = (TextView)findViewById(R.id.tvMetar);
        btnMetar = (Button)findViewById(R.id.btnMetar);
        edtIcaoCodeLayout = (TextInputLayout) findViewById(R.id.edtIcaoCodeLayout);
        edtIcaoCode = (EditText) edtIcaoCodeLayout.findViewById(R.id.edtIcaoCode);
        try {
            actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ================Forms Ids end=========================
        // находим список
//        ListView lvMain = (ListView) findViewById(R.id.lvMain);

        // создаем адаптер
       /* metaradapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Metarlist);*/

        // присваиваем адаптер списку
//        lvMain.setAdapter(metaradapter);

        btnMetar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                strIcao = edtIcaoCode.getText().toString();
                if (!strIcao.equals("")) {
                    new LoadTask().execute();
//                    new explodeMetar().execute();
                }
            }
        });
        // ==================onCreateCode start=========================
    }//============onCreate_end====================


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // ProjectsActivity is my 'home' activity
                super. onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class LoadTask extends AsyncTask<String, Integer, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultBuffer = "";
        String resultLines = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnMetar.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            startParse = false;
            hasAutotaf = false;
            finishParse = false;
            try {
                URL url = new URL(PARSE_LINK + strIcao);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer lineBuffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("<PRE>AUTOTAF")) {
                        startParse = true;
                        hasAutotaf = true;
                    }
                    if (!finishParse && startParse) {
//                        Log.i(TAG, "doInBackground line = "+line);
                        if (!line.equals("")) {
                            lineBuffer.append(line);
                        }
                    }
                    if (startParse && line.contains("</PRE>")) {
                        finishParse = true;
                        startParse = false;
                    }
                }
                resultBuffer = lineBuffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "doInBackground Exception = "+e.toString());
            }
            Log.i(TAG, "doInBackground hasAutotaf = "+hasAutotaf);
            return resultBuffer;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String strResult) {
            super.onPostExecute(strResult);
            resultLines=strResult.replaceAll("[\\s]{2,}", " ");
            resultLines=resultLines.replaceAll("<PRE>", "");
            resultLines=resultLines.replaceAll("</PRE>", "");
            resultLines=resultLines.replaceAll("AUTOTAF", "\nAUTOTAF");
            /*Metarlist = new ArrayList<String>(Arrays.asList(resultLines.split("AUTOTAF")));
            metaradapter.notifyDataSetChanged();*/
//            for (MetarItem : Metarlist ) {
//                Log.i(TAG, "LoadTask metar  = " + MetarItem);
//            }
            tvMetar.setText(resultLines);
            btnMetar.setEnabled(true);
        }
    }

   /* private class explodeMetar extends AsyncTask<String, Integer, String> {
        String resultLines = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnMetar.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String strResult) {
            super.onPostExecute(strResult);
            resultLines=strResult.replaceAll("[\\s]{2,}", " ");
            resultLines=resultLines.replaceAll("<PRE>", "");
            resultLines=resultLines.replaceAll("</PRE>", "");
            resultLines=resultLines.replaceAll("AUTOTAF", "\nAUTOTAF");
            metarText = resultLines;

            tvMetar.setText(resultLines);
            btnMetar.setEnabled(true);
        }
    }*/
}// ===================Activity_end==================================
