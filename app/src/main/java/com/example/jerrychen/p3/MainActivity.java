package com.example.jerrychen.p3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 */
public class MainActivity extends Activity
{
    private TextView statusLabel, value;
    private BluetoothAdapter myBluetooth;
    private BluetoothSocket btSocket;
    private BluetoothDevice btDevice;
    private OutputStream oStream;
    private InputStream iStream;
    private byte[] readBuffer;
    private int readBufferIndex;
    private volatile boolean stopListening;
    byte[] packetBytes;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connectButton = (Button) findViewById(R.id.connect);
        final Button disconnectButton = (Button) findViewById(R.id.disconnect);
        Button ledOnButton = (Button) findViewById(R.id.ledon);
        Button ledOffButton = (Button) findViewById(R.id.ledoff);
        statusLabel = (TextView) findViewById(R.id.label);
        value = (TextView) findViewById(R.id.textView1);

        // Setting up the "Connect" button
        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (findArduino())
                {
                    try
                    {
                        connectToArduino();
                    }
                    catch (IOException e)
                    {
                        createToast("CONNECTION FAILED");
                    }
                }
            }
        });

        // Setting up the "Disconnect" button
        disconnectButton.setOnClickListener(new View.OnClickListener()
        {

            @Override

            public void onClick(View v)
            {
                try
                {
                    disconnectBt();
                }
                catch (IOException ex)
                {

                }
            }
        });

        // Setting up the "LED On" button
        ledOnButton.setOnClickListener(new View.OnClickListener()
        {
            @Override

            public void onClick(View v)
            {
                try
                {
                    sendRequest("1");
                }
                catch (IOException ex)
                {
                    createToast("FAILED TO TURN LED ON");
                }
            }
        });

        // Setting up the "LED Off" button
        ledOffButton.setOnClickListener(new View.OnClickListener()
        {
            @Override

            public void onClick(View v)
            {
                try
                {
                    sendRequest("0");
                }
                catch (IOException ex)
                {
                    createToast("FAILED TO TURN LED OFF");
                }
            }
        });
    }

    /**
     * This method checks if Bluetooth is enabled, and promtps to activate it if not. Secondly it
     * looks for the Arduino in the previously paired devices.
     *
     * Note: If this method is called whn Bluetooth is deactivated, one will need to call the method
     * once more to find the Arduino also.
     *
     * @return true if the Arduino is found in the previously paired devices of the Bluetooth
     *         adapter.
     */
    private boolean findArduino()
    {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null)
        {
            statusLabel.setText("No bluetooth adapter available");
        }

        if (!myBluetooth.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
            statusLabel.setText("Try again with BlueTooth turned on");
            return false;
        }

        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                String devName=device.getName();
                devName = devName.replaceAll("(\\r|\\n)", "");

                if (devName.equals("ITEAD"))
                {
                    btDevice = device;
                    statusLabel.setText("Bluetooth Device '" + device.getName() + "' Found");
                    Toast.makeText(MainActivity.this,"Bluetooth Device '" + device.getName() + "' Found",Toast.LENGTH_LONG).show();
                    return true;
                }
            }
        }
        statusLabel.setText("Bluetooth Device NOT Found");
        return false;
    }

    /**
     * This methods establishes a connection between the Android device and the Bluetooth device
     * found in the method findArduino().
     *
     * @throws IOException
     */
    private void connectToArduino() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
        btSocket.connect();
        oStream = btSocket.getOutputStream();
        iStream = btSocket.getInputStream();
        ackListener();
        statusLabel.setText("Bluetooth connection established");
    }

    /**
     * This method starts a new thread that will listen for feedback messages from the Arduino.
     */
    private void ackListener()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; // This is the ASCII code for a newline character

        stopListening = false;
        readBufferIndex = 0;
        readBuffer = new byte[1024];
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!Thread.currentThread().isInterrupted() && !stopListening)
                {
                    try
                    {
                        int input = iStream.available();
                        if (input > 0)
                        {
                            //byte[]
                            packetBytes = new byte[input];
                            iStream.read(packetBytes);

                            for (int i = 0; i < input; i++)
                            {
                                byte b = packetBytes[i];
                                if (b == '>')
                                {
                                    byte[] encodedBytes = new byte[readBufferIndex];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                            encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferIndex = 0;

                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            value.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferIndex++] = b;
                                }
                            }


/*							runOnUiThread(new Runnable() {
							    public void run() {
							        // Update UI elements
									value.setText(""+packetBytes[0]);
							    }
							});
							*/
                        }
                    }
                    catch (IOException ex)
                    {
                        stopListening = true;
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * This method sends some input data over the established Bluetooth connection.
     *
     * @param input the data to be sent over Bluetooth.
     * @throws IOException
     */
    private void sendRequest(String input) throws IOException
    {
        oStream.write(input.getBytes());
        statusLabel.setText("Request Sent");
    }

    /**
     * Stops the feedback-listening thread and closes sockets and streams.
     *
     * @throws IOException
     */
    private void disconnectBt() throws IOException
    {
        stopListening = true;
        oStream.close();
        iStream.close();
        btSocket.close();
        statusLabel.setText("Bluetooth Disconnected");
    }

    /**
     * A method to easily generate temporary messages on the screen.
     *
     * @param message The message to be displayed.
     */
    private void createToast(String message)
    {
        Toast toast = Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}