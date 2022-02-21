package com.example.googlemaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import java.util.ArrayList;

public class PageView extends AppCompatActivity {

    ArrayList<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view);

        ViewPager mPager = findViewById(R.id.vpager);
        mPager.setAdapter(new SlidingAdapter(this, urls));
    }
}