package com.example.hasee.myweather;

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

public class personal extends AppCompatActivity {

    private Button setset;
    private EditText set_min;
    private EditText set_hour;

    private SharedPreferences pref1;
    private SharedPreferences.Editor editor1;
    private CheckBox remember_set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

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
    }
}
