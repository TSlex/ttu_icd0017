package com.tslex.radio;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class WebReqHandler {

    @SuppressLint("StaticFieldLeak")
    private static WebReqHandler instance;
    private static String TAG = WebReqHandler.class.getSimpleName();

    public static WebReqHandler getInstance(Context context) {
        if (instance == null) {
            instance = new WebReqHandler(context);
        }
        return instance;
    }

    private WebReqHandler(Context context) {
        this.context = context;
    }

    private Context context;

    private RequestQueue requestQueue = null;

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setTag(TAG);
        requestQueue.add(request);
    }
}
