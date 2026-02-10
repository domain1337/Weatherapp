package com.example.weatherapp.data;

public class WeatherModel {
    public String timeOrDay;
    public String temp;
    public int iconRes;

    public WeatherModel(String timeOrDay, String temp, int iconRes) {
        this.timeOrDay = timeOrDay;
        this.temp = temp;
        this.iconRes = iconRes;
    }
}
