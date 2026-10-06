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
import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.data.WeatherModel;
import com.example.weatherapp.data.WeatherResponse;
import com.example.weatherapp.utils.RetrofitClient;

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
        tvCity.setText(city);
        showError(getString(R.string.weather_loading));
        if (BuildConfig.WEATHER_API_KEY.isEmpty()) {
            showError(getString(R.string.weather_key_missing));
            return;
        }
        RetrofitClient.getApi().getWeather(city, BuildConfig.WEATHER_API_KEY, "metric", "ru")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful()) {
                            WeatherResponse body = response.body();
                            if (isValidForecast(body)) {
                                updateUI(body);
                            } else {
                                showError(getString(R.string.weather_bad_response));
                            }
                        } else if (response.code() == 404) {
                            showError(getString(R.string.weather_city_not_found));
                        } else if (response.code() == 401 || response.code() == 403) {
                            showError(getString(R.string.weather_key_invalid));
                        } else if (response.code() == 429) {
                            showError(getString(R.string.weather_rate_limit));
                        } else {
                            showError(getString(R.string.weather_api_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        if (!isFinishing() && !isDestroyed()) {
                            showError(getString(R.string.weather_network_error));
                        }
                    }
                });
    }

    private boolean isValidForecast(WeatherResponse data) {
        if (data == null || data.city == null || data.city.name == null
                || data.list == null || data.list.isEmpty()) return false;
        for (WeatherResponse.ForecastItem item : data.list) {
            if (item == null || item.main == null || item.weather == null
                    || item.weather.isEmpty() || item.weather.get(0) == null
                    || item.weather.get(0).icon == null) return false;
        }
        return true;
    }

    private void showError(String message) {
        tvMainTemp.setText("");
        tvStatus.setText(message);
        tvRange.setText("");
        imgWeatherMain.setVisibility(View.INVISIBLE);
        findViewById(R.id.cardHourly).setVisibility(View.GONE);
        findViewById(R.id.cardDaily).setVisibility(View.GONE);
        viewPagerInfo.setVisibility(View.GONE);
    }

    private void updateUI(WeatherResponse data) {
        WeatherResponse.ForecastItem current = data.list.get(0);

        imgWeatherMain.setVisibility(View.VISIBLE);
        findViewById(R.id.cardHourly).setVisibility(View.VISIBLE);
        findViewById(R.id.cardDaily).setVisibility(View.VISIBLE);
        viewPagerInfo.setVisibility(View.VISIBLE);

        tvCity.setText(data.city.name);
        tvMainTemp.setText(Math.round(current.main.temp) + "°");
        tvStatus.setText(current.weather.get(0).description == null ? "" : current.weather.get(0).description);
        tvRange.setText("↑ " + Math.round(current.main.temp_max) + "° / ↓ " + Math.round(current.main.temp_min) + "°");

        String iconCode = current.weather.get(0).icon;
        setWeatherTheme(iconCode.contains("n"));
        imgWeatherMain.setImageResource(getWeatherIcon(iconCode));
        setupHourlyList(data.list);
        setupDailyList(data.list);
        setupInfoPager(current, data.city);
    }

    private void setupHourlyList(List<WeatherResponse.ForecastItem> list) {
        List<WeatherModel> data = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("HH:00", Locale.getDefault());
        for (int i = 0; i < Math.min(8, list.size()); i++) {
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
            if (item.dt_txt != null && item.dt_txt.contains("12:00:00")) {
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
