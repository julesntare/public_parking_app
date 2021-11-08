package com.example.parkinapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private final List<CarData> carList;
    private final Context context;
    CarAdapter(List<CarData> carList, Context context) {
        this.carList = carList;
        this.context = context;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_car_list, parent, false);
        return new CarViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CarViewHolder holder, final int position) {
        final CarData car = carList.get(position);

        String payText = car.getPay() + ".00 Rwf";

        holder.textViewPlate.setText(car.getPlate());
        holder.textViewPhone.setText(car.getPhone());
        holder.textViewTime.setText(car.getTime());
        holder.textViewTimeOut.setText(car.getTimeout());
        holder.textViewNames.setText(car.getNames());
        holder.textViewHours.setText(car.getHours_used());
        holder.textViewPay.setText(payText);
        holder.textPostPaid.setText(car.getCarDetails());
        final User user = SharedPrefManager.getInstance(context).getUser();
        Intent intent = new Intent(context, ProceedPayment.class);
        intent.putExtra("park_id", car.getId());
        intent.putExtra("plate", car.getPlate());
        intent.putExtra("cash", car.getPay());
        intent.putExtra("tel", car.getPhone());
        intent.putExtra("user", user.getId());
        intent.putExtra("total_hours", car.getTotal_time());
        holder.removeButton.setOnClickListener(view -> context.startActivity(intent));
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }


    public static class CarViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewPlate;
        private final TextView textViewPhone;
        private final TextView textViewTime;
        private final TextView textViewTimeOut;
        private final TextView textViewNames;
        private final TextView textViewHours;
        private final TextView textViewPay;
        private final TextView textPostPaid;
        private final Button removeButton;

        public CarViewHolder(View itemView) {
            super(itemView);
            textViewPlate = itemView.findViewById(R.id.textViewPlate);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewTimeOut = itemView.findViewById(R.id.textViewTimeOut);
            textViewNames = itemView.findViewById(R.id.textServedName);
            textViewHours = itemView.findViewById(R.id.textHours);
            textViewPay = itemView.findViewById(R.id.textPay);
            textPostPaid = itemView.findViewById(R.id.textPostPaid);
            removeButton = itemView.findViewById(R.id.remove_car_id);
        }
    }

}
