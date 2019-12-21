package com.arny.data.remote.dropbox;


import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

import okhttp3.OkHttpClient;

public class DropboxClientFactory {
    private static DbxClientV2 sDbxClient;

    public static void init(String accessToken) {
        if (sDbxClient == null) {
            OkHttpClient client = OkHttp3Requestor.defaultOkHttpClient();
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("examples-v2-demo")
                    .withHttpRequestor(new OkHttp3Requestor(client))
                    .build();

            sDbxClient = new DbxClientV2(requestConfig, accessToken);
        }
    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            return null;
        }
        return sDbxClient;
    }
}
