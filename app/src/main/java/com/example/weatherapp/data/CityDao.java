package com.example.weatherapp.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CityDao {
    @Query("SELECT * FROM cities")
    List<CityEntity> getAllCities();

    @Insert
    void insertCity(CityEntity city);

    @Update
    void updateCity(CityEntity city);

    @Delete
    void deleteCity(CityEntity city);
}