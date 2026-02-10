package com.example.weatherapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.R;
import java.util.List;

public class InfoPagerAdapter extends RecyclerView.Adapter<InfoPagerAdapter.ViewHolder> {

    private List<InfoPage> pages;

    public InfoPagerAdapter(List<InfoPage> pages) { this.pages = pages; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_info_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InfoPage page = pages.get(position);
        holder.title.setText(page.title);
        holder.value.setText(page.value);
    }

    @Override
    public int getItemCount() { return pages.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, value;
        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.infoTitle);
            value = v.findViewById(R.id.infoValue);
        }
    }

    public static class InfoPage {
        String title, value;
        public InfoPage(String title, String value) { this.title = title; this.value = value; }
    }
}