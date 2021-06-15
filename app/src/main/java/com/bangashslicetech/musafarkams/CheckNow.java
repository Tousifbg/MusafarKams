package com.bangashslicetech.musafarkams;

import android.content.Context;
import android.net.ConnectivityManager;

public class CheckNow {
    Context context;

    public CheckNow(Context context) {
        this.context = context;
    }


    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
