package com.github.dev.williamg.simplecouchbaseapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CouchBaseApp =>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyCouchBase myCouchBase = new MyCouchBase(this);
        myCouchBase.printValues();

        myCouchBase.close();
    }

}
