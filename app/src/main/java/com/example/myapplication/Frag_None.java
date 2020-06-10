package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

public class Frag_None extends Fragment {

    private View view;
    public ActionBar actionBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.frag3,container,false);
        findViewById();

        return view;
    }


    private void findViewById() {
        actionBar =  ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("x");
    }
}
