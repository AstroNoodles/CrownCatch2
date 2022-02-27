package com.github.astronoodles.crowncatch2.cards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.recyclerview.widget.RecyclerView;
import com.github.astronoodles.crowncatch2.R;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.PlaceViewHolder> {

    private final List<String> restNames;
    private final List<Float> restaurantRatings;
    private final List<Boolean> restaurantTakeOutOpen;
    private final List<Boolean> restaurantDineInOpen;
    private final List<Float> distancesAway;

    public RestaurantAdapter(List<String> restNames, List<Float> restaurantRatings, List<Float> distancesAway,
                             List<Boolean> restaurantTakeOutOpen, List<Boolean> restaurantDineInOpen) {
        this.restNames = restNames;
        this.restaurantRatings = restaurantRatings;
        this.distancesAway = distancesAway;
        this.restaurantTakeOutOpen = restaurantTakeOutOpen;
        this.restaurantDineInOpen = restaurantDineInOpen;

        // This check could be applied to all of the lists
        if((restaurantRatings.size() != restaurantTakeOutOpen.size()) &&
                (restaurantDineInOpen.size() != distancesAway.size())){
            System.err.println("The lists are not the same size. Check your error stacktrace!");
        }
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_card, parent, false);
        return new PlaceViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.restName.setText(restNames.get(position));
        holder.ratingBar.setRating(restaurantRatings.get(position));
        holder.takeOutAvailable.setChecked(restaurantTakeOutOpen.get(position));
        holder.dineInAvailable.setChecked(restaurantDineInOpen.get(position));

        // TODO - Check if the distances away are in miles or meters. Me thinks they are in meters.
        // That is why I am dividing by 1609. 1 mile = 1609 meters.
        holder.restDistAway.setText(String.format("%.2f miles away.", distancesAway.get(position) / 1609));
    }

    @Override
    public int getItemCount() {
        return restaurantRatings.size();
    }


    protected static class PlaceViewHolder extends RecyclerView.ViewHolder {

        protected final AppCompatRatingBar ratingBar;
        protected final AppCompatCheckBox takeOutAvailable;
        protected final AppCompatCheckBox dineInAvailable;
        protected final TextView restDistAway;
        protected final TextView restName;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            restName = itemView.findViewById(R.id.restaurant_name);
            ratingBar = itemView.findViewById(R.id.restaurant_rating);
            takeOutAvailable = itemView.findViewById(R.id.takeOutMark);
            dineInAvailable = itemView.findViewById(R.id.dineInMark);
            restDistAway = itemView.findViewById(R.id.dist_away);
        }
    }
}