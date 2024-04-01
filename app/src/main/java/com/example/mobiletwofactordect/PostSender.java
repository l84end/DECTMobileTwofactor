package com.example.mobiletwofactordect;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostSender {
    public void sendPostRequest() {
        new SendPostTask().execute();
    }

        private static class SendPostTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Definice URL adresy
                URL url = new URL("https://192.168.1.124:8443/index.php/apps/twofactormobile/api/1.0/foo");

                // Otevření spojení
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                // Nastavení hlaviček požadavku
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                // Povolení odesílání dat
                conn.setDoOutput(true);

                // Definice dat k odeslání
                String data = "{\"cID\": \"testUser\",\"key\": \"123123123\"}";

                // Odeslání dat
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                // Získání odpovědi
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Zpracování úspěšné odpovědi
                    // Například získání odpovědi serveru
                } else {
                    // Zpracování chybové odpovědi
                }

                // Uzavření spojení
                conn.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
