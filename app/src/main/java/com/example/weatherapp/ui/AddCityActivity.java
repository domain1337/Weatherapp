package com.example.weatherapp.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherapp.R;
import com.example.weatherapp.data.AppDatabase;
import com.example.weatherapp.data.CityEntity;

public class AddCityActivity extends AppCompatActivity {

    private static final String[] POPULAR_CITIES = {
            "Moscow", "London", "Paris", "Berlin", "New York",
            "Tokyo", "Dubai", "Rome", "Madrid", "Istanbul",
            "Saint Petersburg", "Kyiv", "Minsk", "Astana"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        AutoCompleteTextView etCity = findViewById(R.id.etCityName);
        EditText etNote = findViewById(R.id.etNote);
        Button btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, POPULAR_CITIES);
        etCity.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            String name = etCity.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (!name.isEmpty()) {
                AppDatabase db = AppDatabase.getInstance(this);
                db.cityDao().insertCity(new CityEntity(name, note));
                Toast.makeText(this, "Город сохранен!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
            }
        });
    }
}