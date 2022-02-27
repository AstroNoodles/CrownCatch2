package com.github.astronoodles.crowncatch2;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass.
 */
public class PointsFragment extends Fragment {
    public static final String USERNAME_KEY = "CrownUsername";
    public static final String COLOR_KEY = "CrownColorUser";
    public static final String SURNAME_KEY = "CrownSurname";
    public static final String LEADERBOARD_AVAILABLE_KEY = "LeaderboardAvailable";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.point_fragment, container, false);

        final SharedPreferences prefs = getActivity().getSharedPreferences(MapActivity2.PREFERENCE_NAME, Context.MODE_PRIVATE);
        int points = prefs.getInt(MapActivity2.POINT_KEY, 0);
        String pointsTitle = getString(R.string.points_title, points);

        TextView pointsTitleView = view.findViewById(R.id.points_title_view);
        pointsTitleView.setText(pointsTitle);

        Button registerLeaderboard = view.findViewById(R.id.registerLeaderboard);

        registerLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeadershipDialog(view, prefs);
            }
        });

        return view;
    }

    private void openLeadershipDialog(final View v, final SharedPreferences prefs) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.register_dialog); // todo - change this to a new dialog layout
        dialog.setTitle(R.string.register_dialog_title);

        final EditText username = dialog.findViewById(R.id.usernameText);
        final EditText favColor = dialog.findViewById(R.id.colorText);
        final EditText surname = dialog.findViewById(R.id.descText);
        final CoordinatorLayout coordinator = v.findViewById(R.id.coordinator);

        Button submitLeaderboard = dialog.findViewById(R.id.submitLeaderboard);
        submitLeaderboard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(USERNAME_KEY, username.getText().toString());
                editor.putString(COLOR_KEY, favColor.getText().toString());
                editor.putString(SURNAME_KEY, surname.getText().toString());
                editor.putBoolean(LEADERBOARD_AVAILABLE_KEY, true);
                editor.commit();

                dialog.dismiss();
                Snackbar.make(coordinator, R.string.leaderboard_thanks, Snackbar.LENGTH_LONG).show();
            }
        });
        dialog.show();

    }
}
