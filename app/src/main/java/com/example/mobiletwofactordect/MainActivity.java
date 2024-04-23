package com.example.mobiletwofactordect;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.biometric.BiometricPrompt;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    Button loginButton;
    private TextView titleTextView;
    private TextView requestInfoTextView;
    private TextView responseTextView;
    private String dataToSend = "";
    private String signedDataToSend;

    private String uid;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");
            String challenge = intent.getStringExtra("challenge");
            dataToSend = challenge;
            uid = body;
            updateTextView(title, body, challenge);
        }
    };

    private final OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleTextView = findViewById(R.id.titleTextView);
        requestInfoTextView = findViewById(R.id.bodyTextView);
        loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button setServerButton = findViewById(R.id.setServer);

        GetTokenForApp.setupFCMTokenListener(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBiometricPromptForLogin();
            }
        });
        handleIntent(getIntent());

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBiometricPromptForRegistration();
            }
        });

        setServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.set_server_param);
                dialog.show();
            }
        });

    }

    private void showBiometricPromptForLogin() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Potvrzení otiskem prstu")
                .setDescription("Proveďte potvrzení otiskem prstu pro přihlášení.")
                .setNegativeButtonText("Zrušit")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        ECDSAKeyManager signMessage = new ECDSAKeyManager();
                        signedDataToSend = signMessage.signMessage(dataToSend, uid);
                        System.out.println("Podpis 123123 je: " + signedDataToSend);
                        makePost(signedDataToSend);
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void showBiometricPromptForRegistration() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Potvrzení otiskem prstu")
                .setDescription("Proveďte potvrzení otiskem prstu pro přechod na registraci.")
                .setNegativeButtonText("Zrušit")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Intent intent = new Intent(MainActivity.this, RegisterPhone.class);
                        startActivity(intent);
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.NOTIFICATION_DATA");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void updateTextView(String title, String body, String challenge) {
        titleTextView.setText("Title: " + title + "\nBody: " + body + "\nChallenge: " + challenge);
    }


    private void makePost(String dataToSend) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("uid", uid)
                            .addFormDataPart("key", dataToSend)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://192.168.1.239:8443/index.php/apps/twofactormobile/api/1.0/foo")
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    final String result = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTextView.setText("Server Response: " + result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");
            String challenge = intent.getStringExtra("challenge");
            if (title != null && body != null) {
                dataToSend = challenge;
                uid = body;
                titleTextView.setText(title);
                requestInfoTextView.setText(body);
            }
        }
    }
}