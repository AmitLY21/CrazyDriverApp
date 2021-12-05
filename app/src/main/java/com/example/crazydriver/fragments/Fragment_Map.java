package com.example.crazydriver.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.crazydriver.callbacks.CallBack_Map;
import com.example.crazydriver.R;

public class Fragment_Map extends Fragment{

    private AppCompatActivity activity;

    private CallBack_Map callBack_map;

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setCallBack_map(CallBack_Map callBack_map) {
        this.callBack_map = callBack_map;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }

}
