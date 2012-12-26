package com.github.androidserialport.communication;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.better.wakelock.Logger;

public class Bluetooth implements ITxChannel, IStringPublisher {
    private BluetoothSocket mSocket;

    private final Context mContext;
    private Handler mHandler;
    private int mWhat;

    private final Handler socketHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case 1111:
                mSocket = (BluetoothSocket) msg.obj;
                // if socket was successfully opened start reading thread
                // if we already have registered Handler
                // register it in the new thread
                if (mSocket != null) {
                    IRxChannel rxChannel = new BtRxChannel(mSocket);
                    mReadingThread = new ReadingThread(rxChannel);
                    mReadingThread.registerFinished(socketHandler, 3333);
                    mReadingThread.start();
                    if (mHandler != null) {
                        mReadingThread.register(mHandler, mWhat);
                    }
                }

                break;

            case 3333:
                Logger.d("Reading thread exited");
                stateMachine.onSocketFailed();
                break;

            default:
                break;
            }
        };
    };

    private ReadingThread mReadingThread;

    private final BluetoothStateMachine stateMachine;

    private class BtRxChannel implements IRxChannel {
        private final BluetoothSocket mSocket;
        private final CharBuffer buffer;
        private InputStreamReader inputStreamReader;

        public BtRxChannel(BluetoothSocket socket) {
            mSocket = socket;
            buffer = CharBuffer.allocate(4);
            try {
                inputStreamReader = new InputStreamReader(mSocket.getInputStream());
            } catch (IOException e) {
                Logger.d("Was not able to create InputStreamReader! - " + e.getMessage());
            }
        }

        @Override
        public String readString() throws IOException {
            int size = inputStreamReader.read(buffer);
            buffer.flip();
            return new String(buffer.array(), 0, size);
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND to handle "paired" events
    // this is required for connecting to devices which were paired after the
    // application started.
    private final BroadcastReceiver deviceFoundIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.d("action = " + action);
            // TODO handle disconnects gracefully

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                stateMachine.onDeviceFound((BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                stateMachine.onDevicePaired((BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                stateMachine.onDiscoveryFinished();
            }
        }
    };

    public Bluetooth(Context context) {
        mContext = context;

        stateMachine = new BluetoothStateMachine(BluetoothAdapter.getDefaultAdapter());

        stateMachine.registerSocketListener(socketHandler, 1111, 2222);
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mContext.registerReceiver(deviceFoundIntentReceiver, filter);
    }

    @Override
    public void sendString(String stringToSend) {
        if (mSocket != null) {
            try {
                mSocket.getOutputStream().write(stringToSend.getBytes());
                Logger.d("String " + stringToSend + " was sent to BT socket");
            } catch (IOException e) {
                Logger.d("Was not able to write! " + e.getMessage());
                stateMachine.onSocketFailed();
                mSocket = null;
            }
        } else {
            Logger.d("No socket is present!");
        }
    }

    @Override
    public void register(Handler handler, int what) {
        Logger.d("Registering " + handler);
        // Register handler in our active reading thread.
        // Store the Handler for future use in case we have to start a new
        // reading thread, e.g. when BT is disconnected and connected again
        mHandler = handler;
        mWhat = what;
        if (mReadingThread != null) {
            mReadingThread.register(handler, what);
        }

    }

    @Override
    public void unregister(Handler handler) {
        if (mReadingThread != null) {
            mReadingThread.unregister(handler);
        }
        mHandler = null;
    }

}
