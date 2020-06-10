package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

public class Frag_Chart extends Fragment {
    private View view;

    private TextView tv_highest, tv_lowest, tv_avg, tv_goal;
    private Button bt_week, bt_month, bt_month3;
    public ActionBar actionBar;

    private int goal=-1;
    private boolean unit;
    private User User;

    private String str_unit;
    private int listsize;

    private final int oneday = 288;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

//        ((MainActivity)getActivity()).showOptionMenu(false);

        view = inflater.inflate(R.layout.frag2,container,false);

        if(getArguments()!=null){
            User = getArguments().getParcelable("userlist");
            goal = getArguments().getInt("goal",-1);
            unit = getArguments().getBoolean("unit");
        }

        listsize = User.getDATA().size();

        findViewById();
        setTextView();

        return view;
    }

    private void findViewById() {
        tv_highest = view.findViewById(R.id.highestBG);
        tv_lowest = view.findViewById(R.id.lowestBG);
        tv_avg = view.findViewById(R.id.avgBG);
        tv_goal = view.findViewById(R.id.goalBG);

        actionBar =  ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("차트보기");
    }


    @Override
    public  void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).showOptionMenu(false);
    }

    private void setTextView(){
        if(goal!=-1)
            tv_goal.setText("목표 혈당은 "+goal+"입니다");
        else
            tv_goal.setText("목표 혈당을 설정하고 관리하세요!");


        if(!unit)
            str_unit = "mg/dl";
        else
            str_unit = "mmol/l";

        float[] today =getToday();

        String str =String.format("%.2f ",today[0]);
        tv_highest.setText(str+str_unit);
        str = String.format("%.2f ",today[1]);
        tv_lowest.setText(str+str_unit);
        str = String.format("%.2f ", today[2]);
        tv_avg.setText(str+str_unit);
    }


    private float[] getToday(){
        int size = getsize();

        float max=-1;
        float min=999;
        float sum=0;
        float avg;

        for(int i=size-1; i>=listsize-size; i--){
            float bg = User.getDATA().get(i).glucose;
            if(bg > max)
                max = bg;
            if(bg<min)
                min = bg;
            sum+=bg;
        }

        avg = sum/size;

        float[] list = {max, min, avg};
        return list;
    }

    private int getsize(){
        int size;
        if(listsize<oneday)
            size = listsize;
        else
            size = oneday;

        return size;
    }
}

