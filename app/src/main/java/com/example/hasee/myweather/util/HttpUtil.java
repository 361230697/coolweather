package com.example.hasee.myweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {//传入地址和回调
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}

