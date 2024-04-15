package com.example.mobiletwofactordect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    Button loginButton;
    private TextView titleTextView;
    private TextView requestInfoTextView; // TextView pro zobrazení informací o požadavku
    private TextView responseTextView; // TextView pro zobrazení odpovědi serveru

    private String uid;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");
            uid = body;
            updateTextView(title, body);
        }
    };

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Přiřazení odkazů na TextView a Button
        titleTextView = findViewById(R.id.titleTextView);
        requestInfoTextView = findViewById(R.id.bodyTextView);
        loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterPhone.class);
                startActivity(intent);
            }
        });

        // Nastavení posluchače událostí pro tlačítko loginButton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volání metody pro odeslání POST požadavku
                    makePost();
            }
        });

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


    private void makePost() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Ignorování ověření certifikátu
                    TrustManager[] trustAllCertificates = new TrustManager[]{
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return new X509Certificate[]{};
                                }
                            }
                    };

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustAllCertificates, new SecureRandom());

                    OkHttpClient client = new OkHttpClient.Builder()
                            .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCertificates[0])
                            .hostnameVerifier((hostname, session) -> true)
                            .build();

                    // Vytvoření a provedení požadavku
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("uid", uid)
                            .addFormDataPart("key", "123123123")
                            .build();

                    Request request = new Request.Builder()
                            .url("https://192.168.1.239:8443/index.php/apps/twofactormobile/api/1.0/foo")
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    final String result = response.body().string();

                    // Aktualizace UI na hlavním vlákně
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTextView.setText("Server Response: " + result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // Zobrazení chyby na hlavním vlákně
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTextView.setText("Request Error");
                        }
                    });
                }
            }
        }).start();
    }


}
