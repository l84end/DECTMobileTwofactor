package com.example.mobiletwofactordect;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Console;

public class SetupServer extends AppCompatActivity {
    public static final String PREFS_NAME = "prefFile";
    public static final String IP_ADDRESS_KEY = "ipAddress";

    private String ipServerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_server);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        ipServerAddress = prefs.getString(IP_ADDRESS_KEY, "");

        Button setIPButton = findViewById(R.id.setIP);
        EditText setIPAddEditText = findViewById(R.id.setIPAdd);

        setIPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = setIPAddEditText.getText().toString();
                try {
                    setIpServerAddress(ipAddress);
                    Toast.makeText(SetupServer.this, "IP adresa nastavena: ", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(IP_ADDRESS_KEY, ipAddress);
                    editor.apply();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(SetupServer.this, "Neplatná adresa IP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button setMainActivity = findViewById(R.id.setMainActivityButton);
        setMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public String getIpServerAddress() {
        return ipServerAddress;
    }

    public void setIpServerAddress(String ipServerAddress) throws IllegalArgumentException {
        if (isValidIPAddress(ipServerAddress)) {
            this.ipServerAddress = ipServerAddress;
        } else {
            throw new IllegalArgumentException("Neplatná adresa IP: " + ipServerAddress);
        }
    }

    private boolean isValidIPAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
