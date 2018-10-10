package com.jetfiletransfer.mek.jetfiletransfer.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public  class SharedPreferencesHelper {
    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public SharedPreferencesHelper(Context context) {
        this.context=context;

    }

    public void addHelperView(){
        pref = context.getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("help", false);
        editor.apply();
        editor.commit();
    }
    public void removeHelperView(){
        pref = context.getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.remove("help");
        editor.putBoolean("help", true);
        editor.apply();
        editor.commit();
    }
    public boolean getHelperViewPref(){
        pref = context.getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        return pref.getBoolean("help", true);
    }
}
