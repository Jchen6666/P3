package com.example.jerrychen.p3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity   {
    private SpeechRecognizer mySpeechRecognizer;
    private TextToSpeech mTTS;
    private boolean connected=false;
    private boolean speakable;
    private static int sensitivity=150;
    private TextView statusLabel, value;
    private BluetoothAdapter myBluetooth;
    private BluetoothSocket btSocket;
    private BluetoothDevice btDevice;
    private OutputStream oStream;
    private InputStream iStream;
    private byte[] readBuffer;
    private int readBufferIndex;
    private volatile boolean stopListening;
    private StringBuilder sb = new StringBuilder();
    ImageView bluetoothImage;
    byte[] packetBytes;
    Handler h;
  //  final int RECIEVE_MESSAGE=1;
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab=findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_mic_black_24dp);

        bluetoothImage=(ImageView)findViewById(R.id.bluetooth);
        bluetoothImage.setBackgroundResource(R.drawable.bluetooth);
        initializeTextToSpeech();
       initializeSpeechRecognizer();
        fab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

                if (intent.resolveActivity(getPackageManager())!=null) {
                    startActivityForResult(intent,10);
                }else {
                    createToast("Your device doesn't support speech input");
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
            if (resultCode == RESULT_OK&&data != null) {
                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result.get(0).toLowerCase().contains("connect")){
                    if (connected == false) {
                        if (findArduino()) {
                            try {
                                connectToArduino();
                                speak("connected");
                            }catch (IOException e){
                                speak("connection failed");
                            }
                        }
                    }
                }
                if (result.get(0).toLowerCase().contains("disconnect")){
                    try {
                        disconnectBt();
                    } catch (IOException e){

                    }

                }


            }
            break;
        }
    }

    private void initializeSpeechRecognizer() {

        if (SpeechRecognizer.isRecognitionAvailable(this)){

            mySpeechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle bundle) {
                    Log.d("Speech","available");

                    List<String> results=bundle.getStringArrayList( SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(results.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    private void processResult(String command) {
        command=command.toLowerCase();

        //to connect arduino
        if (command.indexOf("connect")!=-1){
            if (connected == false) {
                if (findArduino()) {
                    try {
                        speak("connecting");
                        connectToArduino();
                       // bluetoothImage.setBackgroundResource(R.drawable.bluetooth);
                        speak("connected");
                        Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        createToast("CONNECTION FAILED");
                    }
                }
            }


        }
        //to disconnect
    }

    private void initializeTextToSpeech() {
        mTTS=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status==TextToSpeech.SUCCESS){
                    int result= mTTS.setLanguage(Locale.ENGLISH);
                    if (result==TextToSpeech.LANG_MISSING_DATA|| result==TextToSpeech.LANG_NOT_SUPPORTED){
                        speakable=false;
                        Log.d("TTS","not supported");
                    }else {
                        speakable=true;
                    }
                }else {
                    Log.d("TTS","Error");
                    speakable=false;
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
           // statusLabel.setText("No bluetooth adapter available");
        }

        if (!myBluetooth.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
          //  statusLabel.setText("Try again with BlueTooth turned on");
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
                   // statusLabel.setText("Bluetooth Device '" + device.getName() + "' Found");
                    return true;
                }
            }
        }
       // statusLabel.setText("Bluetooth Device NOT Found");
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
        speak("connecting");
        ackListener();
         sendRequest(sensitivity);
       // sendRequest(150);
        connected=true;
   //     statusLabel.setText("Bluetooth connection established");
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
                //value.setText("Hello");

                while (!Thread.currentThread().isInterrupted() && !stopListening)
                {
                    try
                    {
//                        Log.d("Test","Hello");
//                        value.setText("Hello");
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
                                          //  value.setText(data);
                                            Log.d("Test",data);
                                            if (!data.equals("")&&Integer.parseInt(data.trim())<150){

                                                Log.d("Test",data+"dangerous");
                                                speak("warning");
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferIndex++] = b;

                                    //value.setText(b);
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
      //  statusLabel.setText("Request Sent");
    }
    private void sendRequest(int input)throws IOException{
        oStream.write(input);
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
        connected=false;
        speak("disconnected");
     //   statusLabel.setText("Bluetooth Disconnected");
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
    private void speak(final String s){
if (!mTTS.isSpeaking()) {
    mTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null);
}
    }
    public void buttonConnect(View view) {
        if (connected == false) {
            if (findArduino()) {
                try {
                    connectToArduino();
                  //  bluetoothImage.setBackgroundResource(R.drawable.bluetooth);
                    Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    speak("connection failed");
                    createToast("CONNECTION FAILED");
                }
            }
        }else {
            try
            {
                disconnectBt();
              //  bluetoothImage.setBackgroundResource(R.drawable.ic_launcher_background);
                Toast.makeText(MainActivity.this,"disconnected",Toast.LENGTH_LONG).show();
            }
            catch (IOException ex)
            {

            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mTTS!=null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
}
