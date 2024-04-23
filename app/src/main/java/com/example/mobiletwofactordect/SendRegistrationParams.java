package com.example.mobiletwofactordect;

import static com.example.mobiletwofactordect.SetupServer.IP_ADDRESS_KEY;
import static com.example.mobiletwofactordect.SetupServer.PREFS_NAME;

import java.util.Base64;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
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

public class SendRegistrationParams extends AppCompatActivity {

    private TextView showQRInfo;

    public static JSONObject getInfoToSend(String user, String matchingKey, String ipAddress) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        JSONObject jsonObject = new JSONObject();
        String firebaseId = GetTokenForApp.getToken();

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        PublicKey publicKey = keyStore.getCertificate(user).getPublicKey();
        byte[] publicKeyBytes = publicKey.getEncoded();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);


        try {
            ECDSAKeyManager keyManager = new ECDSAKeyManager();
            keyManager.getAliases();

            // Vytvoření JSON objektu
            jsonObject.put("user", user);
            jsonObject.put("matchingKey", matchingKey);
            jsonObject.put("firebaseId", firebaseId);
            jsonObject.put("pubKey", publicKeyBase64);

            // Odeslání registrace s klíči
            sendRegistration(matchingKey, publicKeyBase64, firebaseId, user, ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject = new JSONObject();
        }

        return jsonObject;
    }



    private static void sendRegistration(String matchingKey, String publicKey, String firebaseId, String login, String ipAddress) {
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
                            .addFormDataPart("matchingKey", matchingKey)
                            .addFormDataPart("publicKey", publicKey)
                            .addFormDataPart("firebaseId", firebaseId)
                            .addFormDataPart("login", login)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://" + ipAddress + ":8443/index.php/apps/twofactormobile/api/1.0/set-device")
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    final String result = response.body().string();

                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (KeyManagementException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}
