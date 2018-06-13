package com.example.hasee.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hasee.myweather.util.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private MyDatabaseHelper dbHelper;

    private Button login;
    private Button register;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText accountEdit;
    private EditText passwordEdit;
    private CheckBox rememberPass;

    private ImageView bingPicImg;
//
    private Fragment leftfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        dbHelper = new MyDatabaseHelper(this, "Users.db", null, 2);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit = (EditText)findViewById(R.id.account);
        passwordEdit = (EditText)findViewById(R.id.passward);
        rememberPass = (CheckBox)findViewById(R.id.remember_pass);


        //



        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        String bingPic = pref.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }


        boolean isRemember = pref.getBoolean("remember_password",false);
        if(isRemember){
            String account = pref.getString("account","");
            String password = pref.getString("password","");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }

        login = (Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                int flag = 0;


                       /* if (account.equals(account1)&&password.equals(passward1)){
                            editor = pref.edit();
                            if(rememberPass.isChecked()){
                                editor.putBoolean("remember_password",true);
                                editor.putString("account",account);
                                editor.putString("password",password);
                            }else{
                                editor.clear();
                            }
                            editor.apply();
                            Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                            replaceFragmentLeft(new FriendList());
                        }else {
                            Toast.makeText(MainActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                        }*/


                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // 查询Book表中所有的数据
                Cursor cursor = db.query("Users", null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        // 遍历Cursor对象，取出数据并打印
                        String account1 = cursor.getString(cursor.getColumnIndex("account"));
                        String passward1 = cursor.getString(cursor.getColumnIndex("passward"));

                        if (account.equals(account1)&&password.equals(passward1)){
                            editor = pref.edit();
                            if(rememberPass.isChecked()){
                                editor.putBoolean("remember_password",true);
                                editor.putString("account",account);
                                editor.putString("password",password);
                            }else{
                                editor.clear();
                            }
                            editor.apply();
                            Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                            flag = 1;
                            //replaceFragmentLeft(new FriendList());
                            go_weather();
                        }
                        if(flag == 0)Toast.makeText(MainActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "book account is " + account1);
                        Log.d("MainActivity", "book passward is " + passward1);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        });

        register = (Button)findViewById(R.id.register);
        register.setOnClickListener(this);
    }

    public void replaceFragmentLeft(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frag,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void replaceFragmentRight(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.right_layout,fragment);
        transaction.commit();
    }



    public void onClick(View v){
        switch (v.getId()){
            case R.id.login:
                replaceFragmentLeft(new Imgwall());
                break;
            case R.id.register:
                replaceFragmentLeft(new Register());
                break;
            default:
                break;
        }
    }

    public void go_weather(){
        Intent intent = new Intent(MainActivity.this,weathermain.class);
        startActivity(intent);
    }

    private  void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager .getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }

}
