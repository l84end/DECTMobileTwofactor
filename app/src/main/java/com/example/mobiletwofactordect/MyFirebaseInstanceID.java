package com.example.mobiletwofactordect;


import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;


public class MyFirebaseInstanceID extends FirebaseMessagingService {

    private static final String TAG = "NEW_TOKENS";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
    }

}
