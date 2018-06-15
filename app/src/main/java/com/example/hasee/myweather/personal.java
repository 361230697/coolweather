package com.example.hasee.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class personal extends AppCompatActivity {

    private Button setset;
    private EditText set_min;
    private EditText set_hour;
    private Button myprefer1;
    private Button myprefer2;
    private Button myprefer3;

    private SharedPreferences pref1;
    private SharedPreferences.Editor editor1;
    private CheckBox remember_set;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        sharedPreferences = getSharedPreferences("prefer", MODE_PRIVATE);
        editor3 = sharedPreferences.edit();
        File file = new File("/data/data/com.example.hasee.myweather/shared_prefs/prefer.xml");
        if(!file.exists()){
            Log.d("MainActivity","mylog:"+file.exists());
            editor3.putString("prefer1", "");
            editor3.putString("prefer2", "");
            editor3.putString("prefer3", "");
            editor3.putString("prefer1_name", "");
            editor3.putString("prefer2_name", "");
            editor3.putString("prefer3_name", "");
            editor3.apply();
        }

        pref1 = PreferenceManager.getDefaultSharedPreferences(this);
        set_hour=(EditText)findViewById(R.id.set_hour);
        set_min=(EditText)findViewById(R.id.set_min);
        remember_set=(CheckBox)findViewById(R.id.remember_set);
        boolean set_remember = pref1.getBoolean("remember_set",false);
        if (set_remember) {
            String sethour = pref1.getString("sethour", " 0");
            String setmin = pref1.getString("setmin", "0");
            set_min.setText(setmin);
            set_hour.setText(sethour);
            remember_set.setChecked(true);
        }





        setset = (Button)findViewById(R.id.setset);
        setset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String setmin = set_min.getText().toString();
                String sethour = set_hour.getText().toString();
                editor1 = pref1.edit();
                SharedPreferences.Editor editor2 = getSharedPreferences("data",MODE_PRIVATE).edit();
                if(remember_set.isChecked()) {
                    editor1.putString("setmin", setmin);
                    editor1.putString("sethour", sethour);
                    editor1.putBoolean("remember_set",true);
                    editor2.putString("setmin", setmin);
                    editor2.putString("sethour", sethour);
                    editor2.putBoolean("remember_set",true);
                    if(set_hour.getText().toString().equals("")){editor1.putString("sethour", "0");editor2.putString("sethour", "0");
                        Log.d("MainActivity", "onStartCommand1");}
                    if(set_min.getText().toString().equals("")){editor1.putString("setmin", "0");editor2.putString("setmin", "0");
                        Log.d("MainActivity", "onStartCommand2");}
                    editor2.apply();
                    Toast.makeText(getBaseContext(),"保存成功",Toast.LENGTH_SHORT).show();
                }else{
                    editor2.putString("setmin","0");
                    editor2.putString("sethour", "0");
                    editor2.putBoolean("remember_set",false);
                    editor2.apply();
                    editor1.clear();
                }
                editor1.apply();
            }
        });

        myprefer1 = (Button)findViewById(R.id.myprefer1);
        myprefer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(personal.this);
                SharedPreferences.Editor editor2 = prefs.edit();
                editor2.putString("weather",null);
                editor2.apply();
                Intent intent = new Intent(personal.this,WeatherActivity.class);
                intent.putExtra("weather_id", sharedPreferences.getString("prefer1",null));//传递点击信息
                startActivity(intent);
            }
        });
        myprefer2 = (Button)findViewById(R.id.myprefer2);
        myprefer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(personal.this);
                SharedPreferences.Editor editor2 = prefs.edit();
                editor2.putString("weather",null);
                editor2.apply();
                Intent intent = new Intent(personal.this,WeatherActivity.class);
                intent.putExtra("weather_id", sharedPreferences.getString("prefer2",null));//传递点击信息
                startActivity(intent);
            }
        });
        myprefer3 = (Button)findViewById(R.id.myprefer3);
        myprefer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(personal.this);
                SharedPreferences.Editor editor2 = prefs.edit();
                editor2.putString("weather",null);
                editor2.apply();
                Intent intent = new Intent(personal.this,WeatherActivity.class);
                intent.putExtra("weather_id", sharedPreferences.getString("prefer3",null));//传递点击信息
                startActivity(intent);
            }
        });

        myprefer1.setText(sharedPreferences.getString("prefer1_name",null));
        myprefer2.setText(sharedPreferences.getString("prefer2_name",null));
        myprefer3.setText(sharedPreferences.getString("prefer3_name",null));

    }
}
