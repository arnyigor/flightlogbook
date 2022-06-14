package com.arny.core.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    // TODO попробовать использовать в качестве кастомных параметров
    public static Map<String, Object> jsonToMap(@Nullable JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<>();

        if (json != JSONObject.NULL && json != null) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    /**
     * Remove the file extension from a filename, that may include a path.
     * <p>
     * e.g. /path/to/myfile.jpg -> /path/to/myfile
     */
    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(".");
        int lastDirSeparator = filename.lastIndexOf("/");
        if (lastDirSeparator > extensionPos) {
            return -1;
        }
        return extensionPos;
    }

    public static ArrayList<String> sortDates(ArrayList<String> dates, final String format) {
        Collections.sort(dates, new Comparator<String>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

            @Override
            public int compare(String o1, String o2) {
                int result = -1;
                try {
                    Date parse = sdf.parse(o1);
                    Date parse1 = sdf.parse(o2);
                    result = parse != null ? parse.compareTo(parse1) : 0;
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                return result;
            }
        });
        return dates;
    }

    public static String match(String where, String pattern, int groupnum) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(where);
        while (m.find()) {
            if (!m.group(groupnum).equals("")) {
                return m.group(groupnum);
            }
        }
        return null;
    }

    public static boolean empty(Object obj) {
        return UtilsKt.empty(obj);
    }

    public static String readAssetFile(Context context, String folder, String fileName) {
        InputStream input;
        try {
            input = context.getAssets().open(folder + "/" + fileName);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> getJsonObjectKeys(Gson gson, String result) {
        ArrayList<String> keys = new ArrayList<>();
        try {
            Iterator<String> keysToCopyIterator =
                    new JSONObject(gson.fromJson(result, JsonElement.class).toString()).keys();
            while (keysToCopyIterator.hasNext()) {
                keys.add((String) keysToCopyIterator.next());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return keys;
    }

    public static ArrayList<String> getJsonArrayKeys(JSONObject object) {
        ArrayList<String> keys = new ArrayList<>();
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        return keys;
    }

    public static String getJsonObjVal(String result, String key) {
        Gson gson = new Gson();
        ArrayList<String> keys = Utility.getJsonObjectKeys(gson, result);
        JsonObject jsonObject = gson.fromJson(result, JsonElement.class).getAsJsonObject();
        if (keys.contains(key)) {
            return jsonObject.get(key).toString();
        }
        return "";
    }

    public static void setJsonParam(JSONObject params, String col, Object val) {
        try {
            params.put(col, val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static <V, T> ArrayList<T> getValuesFromMap(HashMap<V, T> hashMap) {
        ArrayList<T> list = new ArrayList<>();
        for (Map.Entry<V, T> entry : hashMap.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }

    public static <V, T> ArrayList<V> getKeysFromMap(HashMap<V, T> map) {
        ArrayList<V> list = new ArrayList<>();
        for (Map.Entry<V, T> entry : map.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

    public static String dumpCursor(Cursor cursor) {
        return DatabaseUtils.dumpCursorToString(cursor);
    }

    public static String dumpBundle(@Nullable Bundle bundle) {
        StringBuilder stringBuilder = new StringBuilder();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                if (value != null) {
                    stringBuilder.append(String.format(" class(%s) %s->%s", value.getClass().getSimpleName(), key, value.toString()));
                }
            }
            return stringBuilder.toString();
        }
        return null;
    }

    public static String dumpIntent(@Nullable Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    if (value != null) {
                        stringBuilder.append(String.format("class(%s) %s->'%s'", value.getClass().getName(), key, value.toString()));
                    }
                }
                return stringBuilder.toString();
            }
        }
        return null;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return !(networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable());
        }
        return false;
    }
}
