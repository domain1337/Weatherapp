package com.example.weatherapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cities")
public class CityEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String cityName;
    public String note;

    public CityEntity(String cityName, String note) {
        this.cityName = cityName;
        this.note = note;
    }
}