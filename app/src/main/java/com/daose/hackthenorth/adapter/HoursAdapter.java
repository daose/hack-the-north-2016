package com.daose.hackthenorth.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daose.hackthenorth.R;

public class HoursAdapter extends RecyclerView.Adapter<HoursAdapter.ViewHolder> {

    private String[] hours;

    public HoursAdapter(String[] hours){
        this.hours = hours;
    }

    @Override
    public HoursAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HoursAdapter.ViewHolder holder, int position) {
        String day = null;
        switch(position) {
            case 0:
                day = "Mon.";
                break;
            case 1:
                day = "Tues.";
                break;
            case 2:
                day = "Wed.";
                break;
            case 3:
                day = "Thurs.";
                break;
            case 4:
                day = "Fri.";
                break;
            case 5:
                day = "Sat.";
                break;
            case 6:
                day = "Sun.";
                break;
            default:
                break;
        }
        holder.day.setText(day);
        holder.hoursOfOperation.setText(hours[position]);
        Log.d("Test", "hour: " + hours[position]);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView day;
        private TextView hoursOfOperation;

        public ViewHolder(View v){
            super(v);

            day = (TextView) v.findViewById(R.id.day);
            hoursOfOperation = (TextView) v.findViewById(R.id.hours);
        }
    }

    @Override
    public int getItemCount() {
        return hours.length;
    }
}
