package com.example.androidbtcontrol;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import BTservice.BTservice;

public class TentActivity extends AppCompatActivity {

    TextView textInfo2;
    BTservice _bTservice;
    Tent _tent;
    //using the well-known SPP UU
    // ID


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _tent = new Tent();
        textInfo2 = (TextView)findViewById(R.id.myView);
        //String str = "TTTTTTTTTTTTTTTTTT";
       // textInfo2.setText(str);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        _bTservice = new BTservice(textInfo2, this);
        Toast.makeText(this,
                "Tent Activity created!",
                Toast.LENGTH_SHORT).show();


    }

    @Override
    protected void onStart() {
        super.onStart();
        _bTservice.startBT();
    }

}
