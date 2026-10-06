package com.example.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCities();
    }

    private void loadCities() {
        AppDatabase.databaseExecutor.execute(() -> {
            try {
                List<CityEntity> cities = db.cityDao().getAllCities();
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) showCities(cities);
                });
            } catch (RuntimeException e) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed())
                        Toast.makeText(this, "Не удалось загрузить города", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showCities(List<CityEntity> cities) {
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new CityAdapter(cities, new CityAdapter.OnCityClickListener() {
            @Override
            public void onClick(CityEntity city) {
                Intent intent = new Intent(CityListActivity.this, HomeActivity.class);
                intent.putExtra("CITY_NAME", city.cityName);
                startActivity(intent);
            }

            @Override
            public void onDelete(CityEntity city) {
                AppDatabase.databaseExecutor.execute(() -> {
                    try {
                        db.cityDao().deleteCity(city);
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) loadCities();
                        });
                    } catch (RuntimeException e) {
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed())
                                Toast.makeText(CityListActivity.this, "Не удалось удалить город", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }));
    }
}
