package com.jetfiletransfer.mek.jetfiletransfer.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.securepreferences.SecurePreferences;

import static android.content.Context.MODE_PRIVATE;

public  class SharedPreferencesHelper {
    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    SecurePreferences securePrefs;
    boolean useSecureOne=false;
    public SharedPreferencesHelper(Context context) {
        this.context=context;


    }
    public SharedPreferencesHelper(Context context,boolean useSecureOne) {
        this.context=context;
        this.useSecureOne = useSecureOne;


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
    public boolean checkAppStatus(){
        securePrefs = new SecurePreferences(context);
        editor = securePrefs.edit();
        //Uygulama paralımı bedava mı ona bakılması gerekli
        return securePrefs.getBoolean("full", false);
    }
    public void userBuyProVersion(){
        securePrefs = new SecurePreferences(context);
        editor = securePrefs.edit();
        editor.remove("full");
        editor.putBoolean("full", true);
        editor.apply();
        editor.commit();
    }

}
