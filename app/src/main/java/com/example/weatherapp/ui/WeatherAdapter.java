package com.example.weatherapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.R;
import com.example.weatherapp.data.WeatherModel;
import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private List<WeatherModel> list;
    private boolean isHorizontal;

    public WeatherAdapter(List<WeatherModel> list, boolean isHorizontal) {
        this.list = list;
        this.isHorizontal = isHorizontal;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isHorizontal ? R.layout.item_hourly : R.layout.item_daily;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherModel item = list.get(position);
        holder.time.setText(item.timeOrDay);
        holder.temp.setText(item.temp);
        holder.icon.setImageResource(item.iconRes);
    }

    @Override
    public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView time, temp;
        ImageView icon;
        ViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.itemDate);
            temp = itemView.findViewById(R.id.itemTemp);
            icon = itemView.findViewById(R.id.itemIcon);
        }
    }
}