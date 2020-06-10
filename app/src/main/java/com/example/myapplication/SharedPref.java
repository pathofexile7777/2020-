package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private SharedPreferences sp;
    private Context context;

    public SharedPref(Context context)
    {
        sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
    }
    public void setNightMode(boolean state)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("NightMode",state);
        editor.apply();
    }

    public Boolean loadNightModeState()
    {
        return sp.getBoolean("NightMode",false);
    }

}
