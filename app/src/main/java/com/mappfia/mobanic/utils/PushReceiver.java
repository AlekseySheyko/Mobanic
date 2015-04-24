package com.mappfia.mobanic.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PushReceiver", "Push received - Global");
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putBoolean("update", true).apply();
    }
}
