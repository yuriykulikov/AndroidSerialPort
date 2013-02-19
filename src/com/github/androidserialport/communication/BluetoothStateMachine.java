package com.github.androidserialport.communication;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.github.androidutils.logger.Logger;

/**
 * This class is a model for a Bluetooth connection. Manages connection,
 * pairing, discovering and adapter.
 * 
 * <pre>
 * @startuml
 * 
 * Inactive : Adapter is up but no connection was established yet
 * Discovering : Adapter is currently discovering BT devices
 * Connecting : Device is paired, we can now connect it
 * Connected : Socket is open and ready to use
 * 
 * [*] --> Discovering
 * Inactive -down-> Discovering : DISCOVER (retry)
 * Discovering -down-> Connecting : FOUND
 * Inactive -down-> Connecting : FOUND
 * Connecting -down-> Connected : SOCKET_OPENED
 * Connected -up-> Inactive : SOCKET_DIED
 * 
 * @enduml
 * </pre>
 * 
 * @author Yuriy
 * 
 *         TODO use {@link Handler#sendMessageAtFrontOfQueue(Message)} for state
 *         stansitions?
 * 
 */
public class BluetoothStateMachine implements Handler.Callback {
    private static final int DISCOVER = 1;
    private static final int FOUND = 2;
    private static final int SOCKET_OPENED = 4;
    private static final int SOCKET_DIED = 5;
    private static final int DISCOVERY_FINISHED = 6;

    protected static final String TAG = "BluetoothStateMachine";
    private static final int PAIRED = 0;

    private State mState;

    private final Handler mHander;
    private final BluetoothAdapter mBluetoothAdapter;

    private Handler mClientHandler;

    private int mWhatOpened;

    private int mWhatCLosed;

    abstract class State implements Handler.Callback {
        public void onEnter(Object object) {
        }

        public void onExit() {
        }
    }

    State inactive = new State() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case FOUND:
            case PAIRED:
                handleDeviceFound((BluetoothDevice) msg.obj);
                return true;

            case DISCOVER:
                transitTo(discovering, null);
                return true;

            default:
                return false;
            }
        }

        @Override
        public void onEnter(Object object) {
            Logger.getDefaultLogger().d("allrighty, now we wait until retry");
            mHander.sendEmptyMessageDelayed(DISCOVER, 15000);
        }

        @Override
        public void onExit() {
            mHander.removeMessages(DISCOVER);
        };
    };

    State discovering = new State() {

        private int retriesLeft;

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case DISCOVER:
                mBluetoothAdapter.startDiscovery();
                return true;

            case FOUND:
            case PAIRED:
                mBluetoothAdapter.cancelDiscovery();
                handleDeviceFound((BluetoothDevice) msg.obj);
                return true;

            case DISCOVERY_FINISHED:
                if (retriesLeft == 0) {
                    transitTo(inactive, null);
                } else {
                    retriesLeft--;
                    mHander.sendEmptyMessageDelayed(DISCOVER, 5000);
                }
                return true;

            default:
                return false;
            }
        }

        @Override
        public void onEnter(Object object) {
            retriesLeft = 3;
            BluetoothDevice deviceToConnect = null;
            // If there are paired devices
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Logger.getDefaultLogger().d("BT device was found. Name: " + device.getName());
                if (device.getName().contains("linvor")) {
                    deviceToConnect = device;
                    break;
                }
            }
            if (deviceToConnect != null) {
                transitTo(connecting, deviceToConnect);
            } else {
                mHander.sendEmptyMessage(DISCOVER);
            }
        }

        @Override
        public void onExit() {
            mHander.removeMessages(DISCOVER);
        };
    };

    State connecting = new State() {
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case SOCKET_OPENED:
                if (msg.arg1 == 0) {
                    BluetoothSocket socket = (BluetoothSocket) msg.obj;
                    transitTo(connected, socket);
                } else {
                    transitTo(inactive, null);
                }
                return true;

            default:
                return false;
            }
        }

        @Override
        public void onEnter(Object object) {
            // we start connecting to the socket
            Message callback = mHander.obtainMessage(SOCKET_OPENED);
            Thread connectThread = new ConnectingThread((BluetoothDevice) object, callback);
            connectThread.start();
        };

        class ConnectingThread extends Thread {
            private final BluetoothDevice mDevice;
            private final Message mCallback;

            public ConnectingThread(BluetoothDevice device, Message callback) {
                mDevice = device;
                mCallback = callback;
            }

            @Override
            public void run() {
                Logger.getDefaultLogger().d("Opening socket from " + mDevice.getName());
                // Get a BluetoothSocket to connect with the given
                // BluetoothDevice
                try {
                    // MY_UUID is the app's UUID string, also used by the
                    // server
                    // code
                    BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    Logger.getDefaultLogger().d("Created socket for " + socket.getRemoteDevice().getName());
                    // Connect the device through the socket. This will
                    // block
                    // until it succeeds or throws an exception
                    socket.connect();
                    mCallback.obj = socket;
                    mCallback.sendToTarget();
                } catch (IOException e) {
                    mCallback.arg1 = -1;
                    mCallback.obj = e;
                    mCallback.sendToTarget();
                    Logger.getDefaultLogger().d("was not able to create socket - " + e.getMessage());
                }
            };
        }
    };

    State connected = new State() {
        BluetoothSocket mSocket;

        @Override
        public void onEnter(Object object) {
            Message msg = mClientHandler.obtainMessage(mWhatOpened);
            mSocket = (BluetoothSocket) object;
            msg.obj = mSocket;
            msg.sendToTarget();
        };

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case SOCKET_DIED:
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Logger.getDefaultLogger().d("failed to close the socket");
                }
                mSocket = null;
                transitTo(inactive, null);
                return true;

            default:
                return false;
            }
        }

    };

    public BluetoothStateMachine(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
        mHander = new Handler(this);
        mState = discovering;
        mHander.sendEmptyMessageDelayed(DISCOVER, 100);
    }

    public void registerSocketListener(Handler handler, int whatOpen, int whatClosed) {
        // TODO divide these guys
        mClientHandler = handler;
        mWhatOpened = whatOpen;
        mWhatCLosed = whatClosed;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Logger.getDefaultLogger().d("got " + whatToName(msg.what));
        boolean handled = mState.handleMessage(msg);
        if (handled == false) {
            Logger.getDefaultLogger().d(mState + " did not handle " + whatToName(msg.what));
        }
        return true;
    }

    private void handleDeviceFound(BluetoothDevice device) {
        boolean nameFits = "linvor".equals(device.getName());
        if (nameFits) {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                transitTo(connecting, device);
            } else {
                Logger.getDefaultLogger().d("not paired. Has to be paired manually");
            }
        }
    }

    private void transitTo(State state, Object object) {
        mState.onExit();
        mState = state;
        mState.onEnter(object);
    }

    public void onDeviceFound(BluetoothDevice device) {
        Message message = mHander.obtainMessage(FOUND);
        message.obj = device;
        message.sendToTarget();
    }

    public void onDevicePaired(BluetoothDevice device) {
        Message message = mHander.obtainMessage(PAIRED);
        message.obj = device;
        message.sendToTarget();
    }

    public void onDiscoveryFinished() {
        Message message = mHander.obtainMessage(DISCOVERY_FINISHED);
        message.sendToTarget();
    }

    public void onSocketFailed() {
        Message message = mHander.obtainMessage(SOCKET_DIED);
        message.sendToTarget();
    }

    private static String whatToName(int what) {
        switch (what) {
        case DISCOVER:
            return "DISCOVER";
        case FOUND:
            return "FOUND";
        case PAIRED:
            return "PAIRED";
        case SOCKET_OPENED:
            return "SOCKET_OPENED";
        case SOCKET_DIED:
            return "SOCKET_DIED";
        case DISCOVERY_FINISHED:
            return "DISCOVERY_FINISHED";
        default:
            return "UNKNOWN";
        }
    }

}
