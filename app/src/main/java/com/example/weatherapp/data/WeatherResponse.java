package com.example.weatherapp.data;

import java.util.List;

public class WeatherResponse {
    public List<ForecastItem> list;
    public City city;

    public class ForecastItem {
        public long dt;
        public Main main;
        public List<Weather> weather;
        public String dt_txt;
    }

    public class Main {
        public double temp;
        public double temp_min;
        public double temp_max;
        public double feels_like;
        public int humidity;
    }

    public class Weather {
        public String description;
        public String icon;
    }

    public class City {
        public String name;
        public long sunrise;
        public long sunset;
    }
}