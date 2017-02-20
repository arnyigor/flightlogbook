package com.arny.flightlogbook.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.DropboxClientFactory;
import com.arny.flightlogbook.models.Functions;
import com.arny.flightlogbook.models.GetCurrentAccountTask;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxSyncFragment extends Fragment {
    private static final String DROPBOX_STR_TOKEN = "access-token";
    private Context contex;
    private Button login_button,btnSync;
    private TextView email_text,name_text,type_text;

    public DropboxSyncFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dropbox_sync, container, false);
        contex = container.getContext();
        login_button = (Button) rootView.findViewById(R.id.btnDpxLogin);
        btnSync = (Button) rootView.findViewById(R.id.btnSync);
        email_text = (TextView) rootView.findViewById(R.id.tvDpxEmail);
        name_text = (TextView) rootView.findViewById(R.id.tvDpxName);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Auth.startOAuth2Authentication(contex, "rzzuhol6y1aibdx");
            }
        });

        String accessToken = Functions.getPrefs(contex).getString(DROPBOX_STR_TOKEN, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                Functions.getPrefs(contex).edit().putString(DROPBOX_STR_TOKEN, accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (hasToken()) {
            login_button.setVisibility(View.GONE);
            email_text.setVisibility(View.VISIBLE);
            name_text.setVisibility(View.VISIBLE);
            btnSync.setVisibility(View.VISIBLE);
        } else {
            login_button.setVisibility(View.VISIBLE);
            email_text.setVisibility(View.GONE);
            name_text.setVisibility(View.GONE);
            btnSync.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(DropboxSyncFragment.class.getSimpleName(), "onActivityResult: requestCode = " + requestCode);
        Log.i(DropboxSyncFragment.class.getSimpleName(), "onActivityResult: resultCode = " + resultCode);
        Log.i(DropboxSyncFragment.class.getSimpleName(), "onActivityResult: data = " + data.getExtras().toString());
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean hasToken() {
        String accessToken = Functions.getPrefs(contex).getString(DROPBOX_STR_TOKEN, null);
        return accessToken != null;
    }

    private void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                email_text.setText(result.getEmail());
                name_text.setText(result.getName().getDisplayName());
            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
//        PicassoClient.init(contex, DropboxClientFactory.getClient());
        loadData();
    }


}
