package com.github.androidserialport.communication;

import java.io.IOException;

public interface IRxChannel {
    /**
     * 
     * @return
     * @throws IOException
     */
    String readString() throws IOException;
}
