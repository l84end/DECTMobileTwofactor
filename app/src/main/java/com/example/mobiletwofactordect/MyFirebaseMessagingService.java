package com.example.mobiletwofactordect;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingSvc";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Získání dat z příchozí zprávy
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        Log.d(TAG, "Received notification with title: " + title);
        Log.d(TAG, "Received notification with body: " + body);

        // Odeslání dat do MainActivity pomocí Broadcastu
        Intent intent = new Intent("com.example.NOTIFICATION_DATA");
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
