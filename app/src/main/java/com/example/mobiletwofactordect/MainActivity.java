package com.example.mobiletwofactordect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mobiletwofactordect.R;

public class MainActivity extends AppCompatActivity {

    private TextView titleTextView;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");
            updateTextView(title, body);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Přiřazení odkazu na TextView
        titleTextView = findViewById(R.id.titleTextView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrace BroadcastReceiveru pro příjem notifikace
        IntentFilter filter = new IntentFilter("com.example.NOTIFICATION_DATA");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Odhlášení BroadcastReceiveru
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void updateTextView(String title, String body) {
        // Nastavení dat do TextView
        titleTextView.setText("Title: " + title + "\nBody: " + body);
    }
}
