package com.github.androidserialport;

import android.app.Application;

import com.better.wakelock.LogcatLogWriter;
import com.better.wakelock.Logger;
import com.github.androidserialport.communication.CommunicationManager;

public class SerialPortApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CommunicationManager.init(getApplicationContext());

        Logger logger = Logger.getDefaultLogger();
        logger.addLogWriter(new LogcatLogWriter());
    }
}
