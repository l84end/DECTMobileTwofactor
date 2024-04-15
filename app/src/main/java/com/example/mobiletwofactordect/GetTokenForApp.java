package com.example.mobiletwofactordect;

import android.app.Activity;

import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class GetTokenForApp {

    private static final String TAG = "GetTokenForApp";
    private static String currentToken = null; // Přidáno pro ukládání aktuálního tokenu

    public static void setupFCMTokenListener(Activity activity) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Získejte nový FCM registrační token
                        String token = task.getResult();
                        currentToken = token; // Uložení tokenu pro pozdější načtení

                        // Zalogujte a zobrazte toast s tokenem
                        String msg = activity.getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Metoda pro načtení uloženého tokenu
    public static String getToken() {
        return currentToken;
    }
}

