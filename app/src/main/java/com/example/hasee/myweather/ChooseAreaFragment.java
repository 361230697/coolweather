package com.example.hasee.myweather;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.support.v7.app.AlertDialog;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hasee.myweather.data.*;
import com.example.hasee.myweather.util.*;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    private Button personal;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor3;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false);

        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        personal = (Button)view.findViewById(R.id.pserver);

        //
        sharedPreferences = getActivity().getSharedPreferences("prefer", MODE_PRIVATE);
        editor3 = sharedPreferences.edit();
        File file = new File("/data/data/com.example.hasee.myweather/shared_prefs/prefer.xml");
        if(!file.exists()){
            Log.d("MainActivity","mylog:"+file.exists());
            editor3.putString("prefer1", "");
            editor3.putString("prefer2", "");
            editor3.putString("prefer3", "");
            editor3.apply();
        }




        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//点击事件，判断省市县三级跳转
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {//进入天气详情的活动
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof weathermain) {//instanceof判断对象属于哪个类，如果在刚登陆后情况下点击，进入WeatherActivity
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);//传递点击信息
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {//在滑动列表下点击，只会刷新页面
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        //
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentLevel == LEVEL_COUNTY)
                {String weatherId = countyList.get(position).getWeatherId();
                String county = countyList.get(position).getCountyName();
                guanzhu1(parent,view,position,id,weatherId,county);}
                return true;
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });

        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), personal.class);
                startActivity(intent);
            }
        });

        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        titleText.setText("中国");//不同级别设置标题
        backButton.setVisibility(View.GONE);//省级是不允许返回的，严谨
        provinceList = DataSupport.findAll(Province.class);//显示表
        if (provinceList.size() > 0) {//在数据库中提取数据
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {//否则通过网络请求
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {//传入地址
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {//失败保留
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /*private void guanzhu(String guanzhu){
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(getContext());
                dialogBuilder.setTitle("关注功能");
               // dialogBuilder.setMessage("请选择：");
                dialogBuilder.setCancelable(false);
                dialogBuilder.setPositiveButton(guanzhu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("Mainactivity","长按");
                    }
                });
                dialogBuilder.show();
    }*/

    private void guanzhu1(AdapterView<?> parent, View view, int position, long id, final String weatherId, final String county)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Holo_Light_Dialog);
        //builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("关注功能");
        //    指定下拉列表的显示数据
        final String[] choose = {"关注", "取消关注", "取消"};
        //    设置一个下拉的列表选择项
        builder.setItems(choose, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("MainActivity", "mylog:" + weatherId+county);


                if (choose[which].equals("关注")) {
                    if(sharedPreferences.getString("prefer1", "").equals(weatherId) ||
                            sharedPreferences.getString("prefer2", "").equals(weatherId) ||
                            sharedPreferences.getString("prefer3", "").equals(weatherId)){
                        Toast.makeText(getActivity(), "请勿重复关注", Toast.LENGTH_SHORT).show();
                    }
                    else if (!sharedPreferences.getString("prefer1", "").equals("") &&
                            !sharedPreferences.getString("prefer2", "").equals("") &&
                            !sharedPreferences.getString("prefer3", "").equals(""))
                        Toast.makeText(getActivity(), "抱歉，关注已达上限", Toast.LENGTH_SHORT).show();
                    else if (!sharedPreferences.getString("prefer1", "").equals("")) {
                        if (!sharedPreferences.getString("prefer2", "").equals("")) {
                            editor3.putString("prefer3", weatherId);
                            Toast.makeText(getActivity(), "关注成功", Toast.LENGTH_SHORT).show();
                        } else {
                            editor3.putString("prefer2", weatherId);
                            Toast.makeText(getActivity(), "关注成功", Toast.LENGTH_SHORT).show();
                        }
                    } else if(!sharedPreferences.getString("prefer1","").equals(weatherId)){
                        editor3.putString("prefer1", weatherId);
                        Toast.makeText(getActivity(), "关注成功", Toast.LENGTH_SHORT).show();
                    }
                    editor3.apply();
                }else if(choose[which].equals("取消关注")){
                    if(sharedPreferences.getString("prefer1","").equals(weatherId)){
                        editor3.putString("prefer1", "");
                    }
                    else if(sharedPreferences.getString("prefer2","").equals(weatherId)){
                        editor3.putString("prefer2", "");
                    }
                    else if(sharedPreferences.getString("prefer2","").equals(weatherId)){
                        editor3.putString("prefer3", "");
                    }else {
                        Toast.makeText(getActivity(), "您并没有关注~", Toast.LENGTH_SHORT).show();
                    }
                    editor3.apply();
                }
            }
        });
        builder.show();
    }

}

