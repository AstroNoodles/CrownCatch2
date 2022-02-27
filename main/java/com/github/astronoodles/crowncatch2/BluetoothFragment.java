package com.github.astronoodles.crowncatch2;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.astronoodles.crowncatch2.cards.BluetoothRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.github.astronoodles.crowncatch2.MapActivity2.CHANNEL_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothFragment extends Fragment {
    private final List<String> phone_names = new ArrayList<>(5);
    private final List<String> signals = new ArrayList<>(5);
    private final List<String> proximities = new ArrayList<>(5);

    private BluetoothRecyclerAdapter adapter;
    private BluetoothAdapter discovery;
    private TextView finding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bluetooth_layout, container, false);

        RecyclerView rv = v.findViewById(R.id.connections_view);
        Button bluetoothBtn = v.findViewById(R.id.connect_btn);
        ImageButton infoBtn = v.findViewById(R.id.info);

        discovery = BluetoothAdapter.getDefaultAdapter();
        finding = v.findViewById(R.id.empty_alert);
        rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        adapter = new BluetoothRecyclerAdapter(phone_names, signals, proximities);
        rv.setAdapter(adapter);

        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finding.setVisibility(View.VISIBLE);
                if(discovery != null) {
                    System.out.println("test beast");
                    discovery.startDiscovery();
                } else {
                    Toast.makeText(getActivity(), R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                }
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder build = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.troubleshooting_title)
                        .setMessage(getString(R.string.bluetooth_troubleshooting_message))
                        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                build.show();
            }
        });

        return v;
    }

    private NotificationCompat.Builder buildWarningNotification(Context c, String message){

        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(c, 1, i, 0);

        return new NotificationCompat.Builder(c, CHANNEL_ID)
                .setSmallIcon(R.drawable.img_title_fg)
                .setContentTitle(getString(R.string.sdAlert))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            NotificationManagerCompat nmc = NotificationManagerCompat.from(context);

            System.out.println("hello");
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                BluetoothSignal signal = new BluetoothSignal(rssi);
                System.out.println("bluetooth on!");

                if(name != null && !name.equals("")) {
                    phone_names.add(name);
                    signals.add(getString(R.string.test_strength, signal.getStrength()));
                    proximities.add(getString(R.string.test_distance, signal.getOrdinalStrength(getActivity())));
                    finding.setVisibility(View.INVISIBLE);
                    adapter.notifyDataSetChanged();
                }


            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                nmc.notify(2, buildWarningNotification(context, getString(R.string.social_distancing_alert)).build());
                discovery.startDiscovery();
            }
            }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    protected static class BluetoothSignal {
        private final short strength;

        public BluetoothSignal(short strength){
            this.strength = strength;
        }

        @Override
        public String toString() {
            if(-100 <= strength && strength <= -90){
                return String.format("LOW (%d dBm)", strength);
            } else if (-90 < strength && strength <= -80){
                return String.format("MEDIUM (%d Dbm)", strength);
            } else {
                return String.format("HIGH (%d Dbm)", strength);
            }
        }

        public short getStrength(){
            return strength;
        }

        public String getOrdinalStrength(Context c){
            if(-100 <= strength && strength <= -90){
                return c.getString(R.string.low_strength);
            } else if(-90 < strength && strength <= -80){
                return c.getString(R.string.medium_strength);
            } else {
                return c.getString(R.string.high_strength);
            }
        }

    }
}
