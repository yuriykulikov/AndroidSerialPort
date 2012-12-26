package com.github.androidserialport;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.github.androidserialport.communication.CommunicationManager;
import com.github.androidserialport.communication.ITxChannel;

/**
 * To write strings to simply send an intent {@link #ACTION_SEND_STRING} with
 * the string attached as {@link #EXTRA_STRING}
 * 
 */
public class WritingService extends Service {
    public static final String ACTION_SEND_STRING = "com.github.androidserialport.WritingService.ACTION_SEND_STRING";
    public static final String EXTRA_STRING = "EXTRA_STRING";

    private ITxChannel txChannel;

    @Override
    public void onCreate() {
        super.onCreate();
        txChannel = CommunicationManager.getTxChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_SEND_STRING)) {
            String stringToSend = intent.getStringExtra(EXTRA_STRING);
            txChannel.sendString(stringToSend);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We do not bind
        return null;
    }
}
