package com.example.mobiletwofactordect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class SetupServer {
    private String ipServerAddress;
    private EditText ipTextSet;
    private Button setMainPage;
    private SetupServer setupServer;

    public String getIpServerAddress() {
        return ipServerAddress;
    }

    public void setIpServerAddress(String ipServerAddress) throws IllegalArgumentException {
        // Kontrola, zda je předaná adresa IP ve správném formátu
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
