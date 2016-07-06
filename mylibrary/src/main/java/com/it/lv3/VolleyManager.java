package com.it.lv3;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyManager {
    private RequestQueue mRequestQueue;
    public RequestQueue getmRequestQueue() {
        return mRequestQueue;
    }
    private VolleyManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }
    private static VolleyManager instance;
    public static VolleyManager getInstance(Context context) {
        if (instance == null)
            instance = new VolleyManager(context);
        return instance;
    }
    public void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }
    public void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }
}
