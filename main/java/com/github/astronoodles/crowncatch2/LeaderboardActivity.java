package com.github.astronoodles.crowncatch2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_layout);

        RecyclerView recycler = findViewById(R.id.leaderboardRecycler);
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(new LeaderboardRecyclerAdapter(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crowncatchmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity.selectMenuOptions(this, item);
        return true;
    }

    private class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardHolder> {

        private final List<String> usernames = new ArrayList<>(10);
        private final List<String> descs = new ArrayList<>(10);
        private final List<String> pointText = new ArrayList<>(10);
        private final List<Integer> colorVals = new ArrayList<>(10);

        private LeaderboardRecyclerAdapter(Context c){
            SharedPreferences prefs = c.getSharedPreferences(MapActivity2.PREFERENCE_NAME, Context.MODE_PRIVATE);
            usernames.add(prefs.getString(PointsFragment.USERNAME_KEY, "Mike"));
            usernames.addAll(Arrays.asList(c.getResources().getStringArray(R.array.users)));

            descs.add(prefs.getString(PointsFragment.SURNAME_KEY, ""));
            descs.addAll(Arrays.asList(c.getResources().getStringArray(R.array.description)));

            checkUserColorVal(c, prefs);

            pointText.add(String.valueOf(prefs.getInt(MapActivity2.POINT_KEY, 0)));
            pointText.addAll(Arrays.asList(c.getResources().getStringArray(R.array.points)));
        }

        private void checkUserColorVal(Context c, SharedPreferences prefs){
            String userColor = prefs.getString(PointsFragment.COLOR_KEY, "#000");
            int parsedColor;

            try {
                parsedColor = Color.parseColor(userColor);
            } catch(IllegalArgumentException e){
                parsedColor = Color.BLACK;
            }
            colorVals.add(parsedColor);

            for(String fakeColorVals : c.getResources().getStringArray(R.array.favcolor)){
                colorVals.add(Color.parseColor(fakeColorVals));
            }
        }

        @NonNull
        @Override
        public LeaderboardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(LeaderboardActivity.this).inflate(R.layout.leaderboard_card, parent, false);
            return new LeaderboardHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull LeaderboardHolder holder, int position) {
            holder.username.setText(usernames.get(position));
            holder.description.setText(descs.get(position));
            holder.pointCounter.setText(pointText.get(position));
            holder.favColor.setImageDrawable(new ColorDrawable(colorVals.get(position)));
        }

        @Override
        public int getItemCount() {
            return usernames.size();
        }
    }


    private static class LeaderboardHolder extends RecyclerView.ViewHolder {

        private final TextView username;
        private final TextView description;
        private final TextView pointCounter;
        private final ImageView favColor;

        public LeaderboardHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.leaderboard_name);
            description =  itemView.findViewById(R.id.leadershipDesc);
            pointCounter = itemView.findViewById(R.id.points_score);
            favColor = itemView.findViewById(R.id.favColor);
        }
    }

}
