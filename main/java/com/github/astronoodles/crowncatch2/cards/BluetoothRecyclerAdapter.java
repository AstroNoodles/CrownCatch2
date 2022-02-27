package com.github.astronoodles.crowncatch2.cards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.astronoodles.crowncatch2.R;

import java.util.List;

public class BluetoothRecyclerAdapter extends RecyclerView.Adapter<BluetoothRecyclerAdapter.BluetoothViewHolder> {
    private final List<String> phone_names;
    private final List<String> signal_strength;
    private final List<String> proximity;

    public BluetoothRecyclerAdapter(List<String> phone_names, List<String> signal_strength,
                                    List<String> proximity){
        this.phone_names = phone_names;
        this.signal_strength = signal_strength;
        this.proximity = proximity;
    }

    @NonNull
    @Override
    public BluetoothViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).
                inflate(R.layout.bluetooth_recycler_layout, parent, false);
        return new BluetoothViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothViewHolder holder, int position) {
        holder.phone_name.setText(phone_names.get(position));
        holder.signal_strength.setText(signal_strength.get(position));
        holder.proximity.setText(proximity.get(position));
    }

    @Override
    public int getItemCount() {
        // The lists should all be the same size
        System.out.println("The size of the phone names list is " + phone_names.size());
        return phone_names.size();
    }


    public static class BluetoothViewHolder extends RecyclerView.ViewHolder {

        public TextView phone_name;
        public TextView signal_strength;
        public TextView proximity;

        public BluetoothViewHolder(@NonNull View itemView) {
            super(itemView);

            phone_name = itemView.findViewById(R.id.phone_name);
            signal_strength = itemView.findViewById(R.id.signal_strength);
            proximity = itemView.findViewById(R.id.proximity);
        }
    }

}
