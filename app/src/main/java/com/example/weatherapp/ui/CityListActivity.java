package com.example.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.R;
import com.example.weatherapp.data.AppDatabase;
import com.example.weatherapp.data.CityEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class CityListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        db = AppDatabase.getInstance(this);
        rv = findViewById(R.id.rvCities);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        fab.setOnClickListener(v -> startActivity(new Intent(this, AddCityActivity.class)));

        loadCities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCities(); // Обновляем список при возврате
    }

    private void loadCities() {
        List<CityEntity> cities = db.cityDao().getAllCities();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new CityAdapter(cities, new CityAdapter.OnCityClickListener() {
            @Override
            public void onClick(CityEntity city) {
                // ПЕРЕХОД НА ГЛАВНЫЙ ЭКРАН С ВЫБРАННЫМ ГОРОДОМ
                Intent intent = new Intent(CityListActivity.this, HomeActivity.class);
                intent.putExtra("CITY_NAME", city.cityName);
                startActivity(intent);
            }

            @Override
            public void onDelete(CityEntity city) {
                db.cityDao().deleteCity(city);
                loadCities(); // Перезагрузить список
            }
        }));
    }
}