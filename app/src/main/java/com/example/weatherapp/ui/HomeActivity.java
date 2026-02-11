package com.example.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.weatherapp.R;
import com.example.weatherapp.data.WeatherModel;
import com.example.weatherapp.data.WeatherResponse;
import com.example.weatherapp.utils.RetrofitClient;
import com.example.weatherapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ImageView imgWeatherMain, btnCityList;
    private TextView tvCity, tvMainTemp, tvStatus, tvRange;
    private ViewPager2 viewPagerInfo;
    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Инициализация UI
        imgWeatherMain = findViewById(R.id.imgWeatherMain);
        tvCity = findViewById(R.id.tvCity);
        tvMainTemp = findViewById(R.id.tvMainTemp);
        tvStatus = findViewById(R.id.tvStatus);
        tvRange = findViewById(R.id.tvRange);
        viewPagerInfo = findViewById(R.id.viewPagerInfo);
        btnCityList = findViewById(R.id.btnCityList);

        currentCity = getIntent().getStringExtra("CITY_NAME");
        if (currentCity == null || currentCity.isEmpty()) {
            currentCity = "Moscow";
        }

        getWeatherData(currentCity);

        btnCityList.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CityListActivity.class);
            startActivity(intent);
        });
    }

    private void getWeatherData(String city) {
        RetrofitClient.getApi().getWeather(city, Utils.APIKEY, "metric", "ru")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateUI(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        tvStatus.setText("Ошибка сети");
                    }
                });
    }

    private void updateUI(WeatherResponse data) {
        WeatherResponse.ForecastItem current = data.list.get(0);

        tvCity.setText(data.city.name);
        tvMainTemp.setText(Math.round(current.main.temp) + "°");
        tvStatus.setText(current.weather.get(0).description);
        tvRange.setText("↑ " + Math.round(current.main.temp_max) + "° / ↓ " + Math.round(current.main.temp_min) + "°");

        String iconCode = current.weather.get(0).icon;
        setWeatherTheme(iconCode.contains("n"));
        imgWeatherMain.setImageResource(getWeatherIcon(iconCode));

        // Настройка списков
        setupHourlyList(data.list);
        setupDailyList(data.list);
        setupInfoPager(current, data.city);
    }

    private void setupHourlyList(List<WeatherResponse.ForecastItem> list) {
        List<WeatherModel> data = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("HH:00", Locale.getDefault());
        for (int i = 0; i < 8; i++) {
            WeatherResponse.ForecastItem item = list.get(i);
            data.add(new WeatherModel(format.format(new Date(item.dt * 1000L)),
                    Math.round(item.main.temp) + "°", getWeatherIcon(item.weather.get(0).icon)));
        }
        setupRecyclerView(R.id.rvHourly, data, true);
    }

    private void setupDailyList(List<WeatherResponse.ForecastItem> list) {
        List<WeatherModel> data = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("EE", Locale.getDefault());
        for (WeatherResponse.ForecastItem item : list) {
            if (item.dt_txt.contains("12:00:00")) {
                data.add(new WeatherModel(format.format(new Date(item.dt * 1000L)),
                        Math.round(item.main.temp_max) + "° / " + Math.round(item.main.temp_min) + "°",
                        getWeatherIcon(item.weather.get(0).icon)));
            }
        }
        setupRecyclerView(R.id.rvDaily, data, false);
    }

    private void setupInfoPager(WeatherResponse.ForecastItem current, WeatherResponse.City city) {
        List<InfoPagerAdapter.InfoPage> pages = new ArrayList<>();
        pages.add(new InfoPagerAdapter.InfoPage("Ощущается как", Math.round(current.main.feels_like) + "°"));
        pages.add(new InfoPagerAdapter.InfoPage("Влажность", current.main.humidity + "%"));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String sun = "Восход: " + sdf.format(new Date(city.sunrise * 1000L)) + "\nЗакат: " + sdf.format(new Date(city.sunset * 1000L));
        pages.add(new InfoPagerAdapter.InfoPage("Солнце", sun));

        viewPagerInfo.setAdapter(new InfoPagerAdapter(pages));
    }

    private int getWeatherIcon(String code) {
        switch (code) {
            case "01d": return R.drawable.ic_sun;
            case "01n": return R.drawable.ic_moon;
            case "02d": case "03d": return R.drawable.ic_partly_cloudy;
            case "04d": case "04n": return R.drawable.ic_cloud;
            case "09d": case "10d": return R.drawable.ic_rain;
            case "13d": return R.drawable.ic_snow;
            default: return R.drawable.ic_cloud;
        }
    }

    private void setupRecyclerView(int id, List<WeatherModel> data, boolean isHoriz) {
        RecyclerView rv = findViewById(id);
        rv.setLayoutManager(new LinearLayoutManager(this, isHoriz ? RecyclerView.HORIZONTAL : RecyclerView.VERTICAL, false));
        rv.setAdapter(new WeatherAdapter(data, isHoriz));
        if (!isHoriz) rv.setNestedScrollingEnabled(false);
    }

    private void setWeatherTheme(boolean isNight) {
        View root = findViewById(R.id.mainScrollView);
        root.setBackgroundColor(ContextCompat.getColor(this, isNight ? R.color.night_bg : R.color.day_bg));
    }
}