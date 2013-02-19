package com.github.androidserialport;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.github.androidserialport.communication.CommunicationManager;
import com.github.androidserialport.communication.IStringPublisher;
import com.github.androidutils.logger.Logger;

public class ReadingService extends Service implements Handler.Callback {
    public static final String ACTION_READ_STRING = "com.github.androidserialport.ReadingService.ACTION_READ_STRING";
    public static final String EXTRA_STRING = "EXTRA_STRING";
    private static final int EVENT_STRING_RECEIVED = 1;
    /**
     * This handler is the internal Handler of the ReadingService used to
     * receive strings from {@link IStringPublisher}
     */
    private final Handler mHandler = new Handler(this);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO start/stop
        // currently only start
        IStringPublisher stringPublisher = CommunicationManager.getStringPublisher();
        stringPublisher.register(mHandler, EVENT_STRING_RECEIVED);
        return START_STICKY;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case EVENT_STRING_RECEIVED:
            Intent intent = new Intent(ACTION_READ_STRING);
            intent.putExtra(EXTRA_STRING, (String) msg.obj);
            sendBroadcast(intent);
            return true;

        default:
            Logger.getDefaultLogger().d("unknown data");
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // not used, we do not bind, we use intents
        return null;
    }
}
