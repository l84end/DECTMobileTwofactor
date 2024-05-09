package com.example.mobiletwofactordect;

import static com.example.mobiletwofactordect.SetupServer.IP_ADDRESS_KEY;
import static com.example.mobiletwofactordect.SetupServer.PREFS_NAME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.biometric.BiometricPrompt;

import android.content.SharedPreferences;
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
    private Button loginButton;
    private Button registerButton;
    private Button denyRegister;
    private Button setServerButton;
    private TextView titleTextView;
    private TextView requestInfoTextView;
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
            // Zobrazit tlačítko pouze pokud je příchozí data validní
            if (dataIsValid(title, body, challenge)) {
                loginButton.setVisibility(View.VISIBLE);
                denyRegister.setVisibility(View.VISIBLE);
            }
        }
    };


    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleTextView = findViewById(R.id.titleTextView);
        requestInfoTextView = findViewById(R.id.bodyTextView);
        denyRegister = findViewById(R.id.denyRegister);
        denyRegister.setVisibility(View.GONE);  // Skrytí tlačítka na začátku
        loginButton = findViewById(R.id.loginButton);
        loginButton.setVisibility(View.GONE);  // Skrytí tlačítka na začátku
        registerButton = findViewById(R.id.registerButton);
        setServerButton = findViewById(R.id.setServer);

        GetTokenForApp.setupFCMTokenListener(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
                denyRegister.setVisibility(View.GONE);
                loginButton.setVisibility(View.GONE);
            }
        });

        GetTokenForApp.setupFCMTokenListener(this);
        handleIntent(getIntent());

        denyRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denyRegister.setVisibility(View.GONE);
                loginButton.setVisibility(View.GONE);
                cancelAuthentication();
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterPhone.class);
                startActivity(intent);
            }
        });

        setServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetupServer.class);
                startActivity(intent);
            }
        });

        handleIntent(getIntent());
        showBiometricPromptForLogin();
    }

    private void cancelAuthentication() {
        titleTextView.setText("Přihlášení zrušeno");
    }

    private void showBiometricPromptForLogin() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Potvrzení otiskem prstu")
                .setDescription("Proveďte potvrzení otiskem prstu pro přístup do aplikace.")
                .setNegativeButtonText("Zrušit")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                continueApp();  // Continue with the application normally
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finish();  // Exit the application if authentication fails or is canceled
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                finish();  // Exit the application on authentication failure
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    private boolean dataIsValid(String title, String body, String challenge) {
        // Implementujte vaše vlastní ověření dat
        return title != null && body != null && challenge != null;
    }

    private void login() {
        ECDSAKeyManager signMessage = new ECDSAKeyManager();
        signedDataToSend = signMessage.signMessage(dataToSend, uid);
        System.out.println("Podpis je: " + signedDataToSend);
        makePost(signedDataToSend);
    }


    private void makePost(String dataToSend) {
        Context appContext = getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String ipAddress = prefs.getString(IP_ADDRESS_KEY, "");
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
                            .url("https://" + ipAddress + ":8443/index.php/apps/twofactormobile/api/1.0/foo")
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    final String result = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTextView.setText("Požadavek byl úspěšně odeslán na server");
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


    private void continueApp() {
    }

    public void updateTextView(String title, String body, String challenge) {
        titleTextView.setText(title + "\nPřihlášení uživatele: " + body);
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
                loginButton.setVisibility(View.VISIBLE);
                denyRegister.setVisibility(View.VISIBLE);
            }
        }
    }
}
