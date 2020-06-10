package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class Frag_Setting extends PreferenceFragmentCompat {
    private View view;
    public ActionBar actionBar;
    private Preference setting_unit, setting_highBg, setting_lowBg, setting_goal, reset;
    public SwitchPreferenceCompat low_alarm_switch, high_alarm_switch, dark_switch;
    private AlertDialog.Builder unit_builder, highBg_builder, lowBg_builder, goal_builder, reset_builder;
    private NumberPicker numberPicker;

    public boolean unit;
    public int high_bg, low_bg, goal_bg;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.settings);

        unit =false;
        high_bg=-1;
        low_bg=-1;
        goal_bg=-1;

        findViewById();

        unit_builder = new AlertDialog.Builder(getActivity(),R.style.Dialog);
        reset_builder = new AlertDialog.Builder(getActivity(),R.style.Dialog);
        highBg_builder = new AlertDialog.Builder(getActivity(),R.style.Dialog);
        lowBg_builder = new AlertDialog.Builder(getActivity(),R.style.Dialog);
        goal_builder = new AlertDialog.Builder(getActivity(),R.style.Dialog);

        setting_unit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                String[] items = new String[]{"md/gl", "mmol/l"};
                unit_builder.setTitle("단위 설정");
                unit_builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                unit_builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0)
                            unit = false;
                        else
                            unit = true;
                        update();
                        Toast.makeText(getActivity(), "단위 설정이 완료되었습니다", Toast.LENGTH_SHORT).show();
                    }
                });
                unit_builder.setNegativeButton("취소", null);
                unit_builder.show();

                return false;
            }
        });

        setting_highBg.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                numberPicker = new NumberPicker(getActivity());
                numberPicker.setMaxValue(300);
                numberPicker.setMinValue(50);

                highBg_builder.setTitle("고혈당 값 설정");
                highBg_builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        high_bg = numberPicker.getValue();
                        Toast.makeText(getActivity(), "고혈당 값이 설정되었습니다", Toast.LENGTH_SHORT).show();
                        update();
                    }
                });
                highBg_builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                highBg_builder.setView(numberPicker);
                highBg_builder.show();

                return false;
            }
        });


        setting_lowBg.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                numberPicker = new NumberPicker(getActivity());
                numberPicker.setMaxValue(200);
                numberPicker.setMinValue(0);

                lowBg_builder.setTitle("저혈당 값 설정");
                lowBg_builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        low_bg = numberPicker.getValue();
                        Toast.makeText(getActivity(), "저혈당 값이 설정되었습니다", Toast.LENGTH_SHORT).show();
                        update();
                    }
                });
                lowBg_builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                lowBg_builder.setView(numberPicker);
                lowBg_builder.show();

                return false;
            }
        });


        setting_goal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                numberPicker = new NumberPicker(getActivity());
                numberPicker.setMaxValue(250);
                numberPicker.setMinValue(50);

                goal_builder.setTitle("목표 혈당 값 설정");
                goal_builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        valueChangeListener.onValueChange(numberPicker, numberPicker.getValue(),numberPicker.getValue());
                        goal_bg = numberPicker.getValue();
                        Toast.makeText(getActivity(), "목표 혈당 값이 설정되었습니다", Toast.LENGTH_SHORT).show();
                        update();
                    }
                });
                goal_builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        valueChangeListener.onValueChange(numberPicker, numberPicker.getValue(),numberPicker.getValue());
                    }
                });
                goal_builder.setView(numberPicker);
                goal_builder.show();

                return false;
            }
        });
//
//        low_alarm_switch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
//        {
//            @Override
//            public boolean onPreferenceClick(Preference preference)
//            {
//                return false;
//            }
//        });

        low_alarm_switch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange (Preference preference, Object newValue)
            {
                MainActivity main= (MainActivity)getActivity();

                if (low_alarm_switch.isChecked()){
                    low_alarm_switch.setChecked(false);
                    main.hide();
                }
                else {
                    low_alarm_switch.setChecked(true);
                    main.show();
                }
                return false;
            }
        });
//
//        high_alarm_switch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
//        {
//            @Override
//            public boolean onPreferenceClick(Preference preference)
//            {
//                return false;
//            }
//        });

        high_alarm_switch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange (Preference preference, Object newValue)
            {
                MainActivity main= (MainActivity)getActivity();

                if (high_alarm_switch.isChecked()){
                    high_alarm_switch.setChecked(false);
                    main.hide();
                }
                else{
                    high_alarm_switch.setChecked(true);
                    main.show();
                }

                return false;
            }
        });

        dark_switch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange (Preference preference, Object newValue)
            {
                MainActivity main= (MainActivity)getActivity();

                if (dark_switch.isChecked()){
                    dark_switch.setChecked(false);
                    main.sp.setNightMode(false);
                    restartApp();
                }
                else{
                    dark_switch.setChecked(true);
                    main.sp.setNightMode(true);
                    restartApp();
                }
                return false;
            }
        });


        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                reset_builder.setTitle("초기화");
                reset_builder.setMessage("모든 데이터를 삭제하시겠습니까?");
                reset_builder.setPositiveButton("삭제", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //
                        Toast.makeText(getActivity(), "모든 데이터가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                    }
                });
                reset_builder.setNegativeButton("취소", null);
                reset_builder.show();

                return false;
            }
        });

    }

    private void findViewById() {
        actionBar =  ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("설정");

        setting_unit= findPreference("set_unit");
        setting_highBg =findPreference("set_high_bg");
        setting_lowBg =findPreference("set_low_bg");
        setting_goal = findPreference("ideal_bg");
        reset = findPreference("reset");
        low_alarm_switch =(SwitchPreferenceCompat) findPreference("alarm_low_bg");
        high_alarm_switch=(SwitchPreferenceCompat) findPreference("alarm_high_bg");
        dark_switch=(SwitchPreferenceCompat)findPreference("theme");
    }

    public void update()
    {
            MainActivity main = (MainActivity) getActivity();
            main.bundle.putInt("highBg", high_bg);
            main.bundle.putInt("lowBg", low_bg);
            main.bundle.putInt("goal", goal_bg);
            main.bundle.putBoolean("unit", unit);
    }

    @Override
    public  void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).showOptionMenu(false);
    }

    public void restartApp()
    {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
