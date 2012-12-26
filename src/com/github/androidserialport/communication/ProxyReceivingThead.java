package com.github.androidserialport.communication;

import java.util.Map;

import android.os.Handler;

/**
 * ProxyReceivingThead is a representative for the receiving threads
 * (StringPublishers), which are used by current connection type: Serial port,
 * Bluetooth or Emulator. Nobody knows, what is current connection type and
 * current receiving thread. They use Proxy instead.
 * 
 * @author lyavinskova
 * 
 */
public class ProxyReceivingThead implements IStringPublisher {

    private final Map<String, IStringPublisher> publishers;

    /**
     * 
     * @param publishers
     *            - publishers are mapped with their names
     * @param sharedPreferences
     *            TODO
     */
    public ProxyReceivingThead(Map<String, IStringPublisher> publishers) {
        this.publishers = publishers;
    }

    @Override
    public void register(Handler handler, int what) {
        // publishers.get("serial").register(handler, what);
        publishers.get("emulation").register(handler, what);
        publishers.get("emulationSimple").register(handler, what);
        publishers.get("bluetooth").register(handler, what);

    }

    @Override
    public void unregister(Handler handler) {
        // publishers.get("serial").unregister(handler);
        publishers.get("emulation").unregister(handler);
        publishers.get("emulationSimple").unregister(handler);
        publishers.get("bluetooth").unregister(handler);

    }

    /*
     * public ProxyReceivingThead(Map<String, IStringPublisher> publishers,
     * SharedPreferences sp) { this.publishers = publishers; String
     * activePublisherName = sp.getString(COMM_TYPE, "emulation");
     * activeStringPublisher = publishers.get(activePublisherName);
     * sp.registerOnSharedPreferenceChangeListener(this); }
     * 
     * public void register(Handler handler, int what) {
     * activeStringPublisher.register(handler, what);
     * 
     * }
     * 
     * public void unregister(Handler handler) {
     * activeStringPublisher.unregister(handler);
     * 
     * }
     * 
     * public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
     * if (key.equals(COMM_TYPE)) { String activePublisherName =
     * sp.getString(COMM_TYPE, "emulation"); activeStringPublisher =
     * publishers.get(activePublisherName); Logger.d("ProxyReceivingThead",
     * "activePublisherName = " + activePublisherName); } }
     */
}
