package com.example.weatherapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.R;
import com.example.weatherapp.data.CityEntity;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
    private List<CityEntity> list;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onClick(CityEntity city);
        void onDelete(CityEntity city);
    }

    public CityAdapter(List<CityEntity> list, OnCityClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_city, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CityEntity city = list.get(pos);
        h.name.setText(city.cityName);
        h.note.setText(city.note);
        h.itemView.setOnClickListener(v -> listener.onClick(city));
        h.delete.setOnClickListener(v -> listener.onDelete(city));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, note;
        ImageView delete;
        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.cityName);
            note = v.findViewById(R.id.cityNote);
            delete = v.findViewById(R.id.btnDelete);
        }
    }
}