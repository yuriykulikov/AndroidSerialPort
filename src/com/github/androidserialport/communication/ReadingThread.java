package com.github.androidserialport.communication;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;

import com.better.wakelock.Logger;

public class ReadingThread extends Thread implements IStringPublisher {
    private static final String TAG = "ReceivingThread";

    private final IRxChannel rxChannel;
    private Handler recipientHandler;
    private int msgWhat;

    private Handler finishedHandler;
    private int msgWhatFinished;

    /**
     * 
     * @param rxChannel
     * @param handler
     *            handler to which thread will send messages
     * @param msgWhat
     *            user-defined message code so that the recipient can identify
     *            what this message is about
     */
    public ReadingThread(IRxChannel rxChannel) {
        super();

        this.rxChannel = rxChannel;

    }

    @Override
    public void run() {
        try {
            String receivedString = "";
            while (!isInterrupted()) {
                String receivedStringSymbol = rxChannel.readString();
                receivedString = receivedString.concat(receivedStringSymbol);
                if (receivedString.contains("\n")) {
                    {
                        Logger.d("Received string: " + receivedString);
                    }
                    Message msg;
                    if (recipientHandler != null) {
                        msg = recipientHandler.obtainMessage();
                        msg.what = msgWhat;
                        msg.obj = receivedString;
                        recipientHandler.sendMessage(msg);
                    } else {
                        {
                            Logger.d("recipientHandler is null");
                        }
                    }
                    receivedString = "";
                }
            }
        } catch (IOException e) {
            Logger.e("IOException in ReadingThread - " + e.getMessage());
        }
        Logger.e("ReadingThread is finished");
        if (finishedHandler != null) {
            finishedHandler.sendEmptyMessage(msgWhatFinished);
        }

    }

    @Override
    public void register(Handler handler, int what) {
        this.recipientHandler = handler;
        this.msgWhat = what;
        if (!isAlive()) {
            {
                Logger.d("start");
            }
            start();
        }
    }

    public void registerFinished(Handler handler, int what) {
        finishedHandler = handler;
        msgWhatFinished = what;
    }

    @Override
    public void unregister(Handler handler) {
        recipientHandler = null;

    }

}
