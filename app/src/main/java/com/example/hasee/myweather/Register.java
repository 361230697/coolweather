package com.example.hasee.myweather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends Fragment {

    private MyDatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        MainActivity activity = (MainActivity)getActivity();
        dbHelper = new MyDatabaseHelper(activity, "Users.db", null, 2);
        View view = inflater.inflate(R.layout.register_layout,container,false);

        final EditText register_account = (EditText)view.findViewById(R.id.register_account);
        final EditText register_passward = (EditText)view.findViewById(R.id.register_passward);
        final Button register_submit = (Button)view.findViewById(R.id.register_submit);
        register_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_user = register_account.getText().toString();
                String new_password = register_passward.getText().toString();
                userRegister(new_user,new_password);
                Log.d("MainActivity", "注册中");
            }
        });

        return view;
    }

    public void userRegister(String new_user,String new_password){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // 开始组装第一条数据
        values.put("account", new_user);
        values.put("passward",new_password);

        db.insert("Users", null, values); // 插入第一条数据

        Cursor cursor = db.query("Users", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                String account = cursor.getString(cursor.getColumnIndex("account"));
                String passward = cursor.getString(cursor.getColumnIndex("passward"));


                Log.d("MainActivity", "book account is " + account);
                Log.d("MainActivity", "book passward is " + passward);

            } while (cursor.moveToNext());
            MainActivity mainActivity = (MainActivity)getActivity();
            Toast.makeText(mainActivity,"注册完成",Toast.LENGTH_SHORT).show();
        }
        cursor.close();

      /*
        ContentValues values = new ContentValues();
        // 开始组装第一条数据
        values.put("account", "The Da Vinci Code");
        values.put("passward", "The Da Vinci Code");

        db.insert("User", null, values); // 插入第一条数据
        values.clear();
        // 开始组装第二条数据
        values.put("account", "The Da Vinci Code11");
        values.put("passward", "The Da Vinci Code11");
        db.insert("User", null, values); // 插入第二条数据*/

    }

}
