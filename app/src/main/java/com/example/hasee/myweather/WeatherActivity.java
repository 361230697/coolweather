package com.example.hasee.myweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hasee.myweather.gson.Forecast;
import com.example.hasee.myweather.gson.Tranweather;
import com.example.hasee.myweather.gson.Weather;
import com.example.hasee.myweather.service.AutoUpdateService;
import com.example.hasee.myweather.util.*;



import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    public SwipeRefreshLayout swipeRefresh;

    private ScrollView weatherLayout;

    private Button navButton;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    private String mWeatherId;

    //
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor3;

    private String cityId;
    private String cityweatherId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        // 初始化各控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        //
        titleCity.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bitmap bitmap;
                bitmap = captureScreen(WeatherActivity.this);
                shareImg("分享","分享","分享",Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null)));
                return true;
            }
        });

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
            editor3.putString("location","");
            editor3.apply();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);

        cityId = getIntent().getStringExtra("city_Id");
//        Log.d("WeatherActivity", "666mylog:cityid"+cityId);


        if (weatherString != null&&cityId==null) {
            // 有缓存时直接解析天气数据
            Log.d("MainActivity", "mylog应该是为null: "+cityId);
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);

        } else {
            if(cityId!=null){
                //weatherLayout.setVisibility(View.INVISIBLE);
                //requestWeather1(cityId);
                Log.d("MainActivity", "mylog应该是长沙: "+cityId);
                Log.d("MainAcyivity","mylogjson开始");
                requestWeather1(cityId);
                Log.d("MainActivity","mylogjson成功");

                cityweatherId = sharedPreferences.getString("location",null);
                Log.d("MainActivity","这是本地地址"+cityweatherId);
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(cityweatherId);

                //weatherLayout.setVisibility(View.INVISIBLE);
                //requestWeather1(cityId);
            }else {
                // 无缓存时去服务器查询天气
                mWeatherId = getIntent().getStringExtra("weather_id");
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });//下拉刷新
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //
        navButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                prefer();
                return true;
            }
        });

        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=08228685e5f345d3a701d6912246bfef";//bc0418b57b2d4918819d3974ac1285d9
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    public void requestWeather1(final String cityId) {
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + cityId + "&key=08228685e5f345d3a701d6912246bfef";//bc0418b57b2d4918819d3974ac1285d9
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Tranweather tranweather = Utility.mtranweather(responseText );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tranweather != null && "ok".equals(tranweather.status)) {
                            Log.d("MainActivity", "mylog:JSON这回成功了！");
                            Log.d("MainActivity", "mylog:"+tranweather.basic.weatherId+tranweather.basic.cityName);
                            editor3.putString("location",tranweather.basic.weatherId);
                            editor3.apply();
                            Log.d("MainActivity","mylog保存本地"+sharedPreferences.getString("location",null));
                        }else {
                            Log.d("MainActivity", "mylog:JSON这回失败了！");
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("获取失败","获取失败");
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {//展示天气信息
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void prefer()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this,android.R.style.Theme_Holo_Light_Dialog);
        //builder.setIcon(R.drawable.ic_launcher);
        //    指定下拉列表的显示数据
        final String[] choose = {sharedPreferences.getString("prefer1_name",""),
                sharedPreferences.getString("prefer2_name",""),
                sharedPreferences.getString("prefer3_name","")};
        //    设置一个下拉的列表选择项
        builder.setItems(choose, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              if(choose[which].equals(sharedPreferences.getString("prefer1_name",""))){
                  if(choose[which].equals(""))Toast.makeText(WeatherActivity.this,"暂无关注",Toast.LENGTH_SHORT).show();
                  else {
                      WeatherActivity.this.drawerLayout.closeDrawers();
                      WeatherActivity.this.swipeRefresh.setRefreshing(true);
                      WeatherActivity.this.requestWeather(sharedPreferences.getString("prefer1",""));
                  }
              }
                else if(choose[which].equals(sharedPreferences.getString("prefer2_name",""))){
                    if(choose[which].equals(""))Toast.makeText(WeatherActivity.this,"暂无关注",Toast.LENGTH_SHORT).show();
                    else {
                        WeatherActivity.this.drawerLayout.closeDrawers();
                        WeatherActivity.this.swipeRefresh.setRefreshing(true);
                        WeatherActivity.this.requestWeather(sharedPreferences.getString("prefer2",""));
                    }
                }
                else  if(choose[which].equals(sharedPreferences.getString("prefer3_name",""))){
                  if(choose[which].equals(""))Toast.makeText(WeatherActivity.this,"暂无关注",Toast.LENGTH_SHORT).show();
                  else {
                      WeatherActivity.this.drawerLayout.closeDrawers();
                      WeatherActivity.this.swipeRefresh.setRefreshing(true);
                      WeatherActivity.this.requestWeather(sharedPreferences.getString("prefer3",""));
                  }
              }


            }
        });
        builder.show();
    }

    //
    private void shareImg(String dlgTitle, String subject, String content,
                          Uri uri) {
//        if (uri == null) {
//            return;
//        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }

    public Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    public static Bitmap captureScreen(Activity activity) {

        activity.getWindow().getDecorView().setDrawingCacheEnabled(true);

        Bitmap bmp=activity.getWindow().getDecorView().getDrawingCache();

        return bmp;

    }

}

