package com.example.weatherapp.ui;

import android.os.Bundle;
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

    private ImageView imgWeatherMain;
    private TextView tvCity, tvMainTemp, tvStatus, tvRange;
    private ViewPager2 viewPagerInfo;

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

        getWeatherData("Moscow");
    }

    private void getWeatherData(String city) {
        RetrofitClient.getApi().getWeather(city, Utils.APIKEY, "metric", "ru")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateUI(response.body());
                            android.widget.Toast.makeText(HomeActivity.this, "Данные обновлены!", android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            android.widget.Toast.makeText(HomeActivity.this, "Ошибка сервера: " + response.code(), android.widget.Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        android.widget.Toast.makeText(HomeActivity.this, "Ошибка сети: " + t.getMessage(), android.widget.Toast.LENGTH_LONG).show();
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
        boolean isNight = iconCode.contains("n");
        setWeatherTheme(isNight);
        imgWeatherMain.setImageResource(getWeatherIcon(iconCode));

        List<WeatherModel> hourlyData = new ArrayList<>();
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:00", Locale.getDefault());
        for (int i = 0; i < 8; i++) {
            WeatherResponse.ForecastItem item = data.list.get(i);
            String time = hourFormat.format(new Date(item.dt * 1000L));
            hourlyData.add(new WeatherModel(time, Math.round(item.main.temp) + "°", getWeatherIcon(item.weather.get(0).icon)));
        }
        setupRecyclerView(R.id.rvHourly, hourlyData, true);

        List<WeatherModel> dailyData = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EE", Locale.getDefault());
        for (WeatherResponse.ForecastItem item : data.list) {
            if (item.dt_txt.contains("12:00:00")) {
                String day = dayFormat.format(new Date(item.dt * 1000L));
                dailyData.add(new WeatherModel(day, Math.round(item.main.temp_max) + "° / " + Math.round(item.main.temp_min) + "°", getWeatherIcon(item.weather.get(0).icon)));
            }
        }
        setupRecyclerView(R.id.rvDaily, dailyData, false);
        setupInfoPager(current, data.city);
    }

    private void setupInfoPager(WeatherResponse.ForecastItem current, WeatherResponse.City city) {
        List<InfoPagerAdapter.InfoPage> pages = new ArrayList<>();
        pages.add(new InfoPagerAdapter.InfoPage("Ощущается как", Math.round(current.main.feels_like) + "°"));
        pages.add(new InfoPagerAdapter.InfoPage("Вероятность осадков сегодня", current.weather.get(0).description));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String sunInfo = "Восход: " + timeFormat.format(new Date(city.sunrise * 1000L)) + "\nЗакат: " + timeFormat.format(new Date(city.sunset * 1000L));
        pages.add(new InfoPagerAdapter.InfoPage("Солнечный цикл", sunInfo));

        viewPagerInfo.setAdapter(new InfoPagerAdapter(pages));
    }

    private int getWeatherIcon(String iconCode) {
        switch (iconCode) {
            case "01d": return R.drawable.ic_sun;
            case "01n": return R.drawable.ic_moon;
            case "02d": case "03d": return R.drawable.ic_partly_cloudy;
            case "04d": case "04n": return R.drawable.ic_cloud;
            case "09d": case "10d": return R.drawable.ic_rain;
            case "13d": return R.drawable.ic_snow;
            default: return R.drawable.ic_cloud;
        }
    }

    private void setupRecyclerView(int id, List<WeatherModel> data, boolean isHorizontal) {
        RecyclerView rv = findViewById(id);
        rv.setLayoutManager(new LinearLayoutManager(this, isHorizontal ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(new WeatherAdapter(data, isHorizontal));
        if (!isHorizontal) rv.setNestedScrollingEnabled(false);
    }

    private void setWeatherTheme(boolean isNight) {
        android.view.View root = findViewById(R.id.mainScrollView);
        root.setBackgroundColor(ContextCompat.getColor(this, isNight ? R.color.night_bg : R.color.day_bg));
    }
}