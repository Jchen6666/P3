package com.example.jerrychen.p3;

import java.io.IOException;
import java.io.OutputStream;

public class ArduinoController {
    /**
     * This method sends some input data over the established Bluetooth connection.
     *
     * @param input the data to be sent over Bluetooth.
     * @throws IOException
     */
    public static void sendRequest(OutputStream oStream, String input) throws IOException
    {
        oStream.write(input.getBytes());
        //  statusLabel.setText("Request Sent");
    }
    public static void sendRequest(OutputStream oStream,int input)throws IOException{
        oStream.write(input);
    }
}
