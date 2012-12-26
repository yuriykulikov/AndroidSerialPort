package com.github.androidserialport.communication;

import android.os.Handler;
import android.os.Message;

/**
 * This interface is implemented by classes, which are reading strings from some
 * source (socket, uart, textview, etc.). Such class first concatenates a
 * string, then puts it to a message and sends this message to a handler, which
 * is passed as a parameter to the method register(Handler handler, int what).
 * 
 * @author Kate
 * 
 */
public interface IStringPublisher {
    /**
     * Register a {@link Handler} to receive messages containing published
     * strings when this strings are read by {@link IStringPublisher}.
     * 
     * @param handler
     *            - a client {@link Handler} which will receive messages
     * @param what
     *            - desired message code. Message will have this code as
     *            {@link Message#what}
     */
    public void register(Handler handler, int what);

    /**
     * 
     * @param handler
     */
    public void unregister(Handler handler);
}