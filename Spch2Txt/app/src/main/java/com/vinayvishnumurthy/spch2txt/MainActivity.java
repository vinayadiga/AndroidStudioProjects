package com.vinayvishnumurthy.spch2txt;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.cognitiveservices.speechrecognition.Contract;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClientWithIntent;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.IOException;
import java.io.InputStream;


import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents {

    //microsoft
    int m_waitSeconds = 0;
    DataRecognitionClient m_dataClient = null;
    MicrophoneRecognitionClient m_micClient = null;
    boolean m_isMicrophoneReco;
    SpeechRecognitionMode m_recoMode;
    boolean m_isIntent;

    private TextView txt_SpeechInput, txt_Status;
    private RadioGroup rg_speech_sdk;
    private RadioButton rb_selected_sdk;
    private ImageButton b_microphone;
    private Button b_connect;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    //server
    Socket client;
    PrintWriter printwriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //microsoft
        // Set the mode and microphone flag to your liking
        m_recoMode = SpeechRecognitionMode.ShortPhrase;
        m_isMicrophoneReco = true;
        m_isIntent = false;

        m_waitSeconds = m_recoMode == SpeechRecognitionMode.ShortPhrase ? 20 : 200;

        vAddListeners();
        initializeRecoClient();
    }

    void vAddListeners()
    {
        rg_speech_sdk = (RadioGroup) findViewById(R.id.radio_sdk);
        b_microphone = (ImageButton) findViewById(R.id.btnSpeak);
        txt_SpeechInput = (TextView) findViewById(R.id.textView);
        txt_Status = (TextView) findViewById(R.id.textView2);
        b_connect = (Button) findViewById(R.id.connect_button);

        b_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        b_microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected_rb = rg_speech_sdk.getCheckedRadioButtonId();
                rb_selected_sdk = (RadioButton) findViewById(selected_rb);
                if(rb_selected_sdk.getText().equals("Google Voice")){
                    promptSpeechInput();
                }
                else {
                    promptMSSpeechinput();
                }

            }
        });

    }

    void promptMSSpeechinput()
    {
        if (m_isMicrophoneReco) {
            // Speech recognition from the microphone.  The microphone is turned on and data from the microphone
            // is sent to the Speech Recognition Service.  A built in Silence Detector
            // is applied to the microphone data before it is sent to the recognition service.
            m_micClient.startMicAndRecognition();
        }
        //else {
          //  doDataRecognition(m_dataClient, m_recoMode);
        //}

    }

    void initializeRecoClient()
    {
        String language = "en-us";

        String primaryOrSecondaryKey = "f8991fa8f0114dfaa9b0ef0ad7d6b30c";
        String luisAppID = "";
        String luisSubscriptionID = "";

        if (m_isMicrophoneReco && null == m_micClient) {
            if (!m_isIntent) {
                m_micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(MainActivity.this,
                        m_recoMode,
                        language,
                        this,
                        primaryOrSecondaryKey);
            }
            else {
                MicrophoneRecognitionClientWithIntent intentMicClient;
                intentMicClient = SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(MainActivity.this,
                        language,
                        this,
                        primaryOrSecondaryKey,
                        luisAppID,
                        luisSubscriptionID);
                m_micClient = intentMicClient;

            }
        }
        else if (!m_isMicrophoneReco && null == m_dataClient) {
            if (!m_isIntent) {
                m_dataClient = SpeechRecognitionServiceFactory.createDataClient(this,
                        m_recoMode,
                        language,
                        this,
                        primaryOrSecondaryKey);
            }
            else {
                DataRecognitionClient intentDataClient;
                intentDataClient = SpeechRecognitionServiceFactory.createDataClientWithIntent(this,
                        language,
                        this,
                        primaryOrSecondaryKey,
                        luisAppID,
                        luisSubscriptionID);
                m_dataClient = intentDataClient;
            }
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "sme thing wrong",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txt_SpeechInput.setText(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    public void onPartialResponseReceived(String s) {
        txt_SpeechInput.append(s+"\n");
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult response) {
        boolean isFinalDicationMessage = m_recoMode == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (m_isMicrophoneReco && ((m_recoMode == SpeechRecognitionMode.ShortPhrase) || isFinalDicationMessage)) {
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            m_micClient.endMicAndRecognition();
        }

        if (!isFinalDicationMessage) {
            for (int i = 0; i < response.Results.length; i++) {
                txt_SpeechInput.append(i + " Confidence=" + response.Results[i].Confidence +
                        " Text=\"" + response.Results[i].DisplayText + "\"\n");
            }
        }
    }

    @Override
    public void onIntentReceived(String s) {
        txt_SpeechInput.append("********* Final Intent *********\n");
        txt_SpeechInput.append(s+"\n");
    }

    @Override
    public void onError(int errorCode, String response) {
        txt_SpeechInput.append("********* Error Detected *********\n");
        txt_SpeechInput.append(errorCode + " " + response + "\n");
    }

    @Override
    public void onAudioEvent(boolean recording) {
        if (!recording) {
            m_micClient.endMicAndRecognition();
        }
        txt_SpeechInput.append("********* Microphone status: " + recording + " *********\n");
    }

    /**
     * Speech recognition with data (for example from a file or audio source).
     * The data is broken up into buffers and each buffer is sent to the Speech Recognition Service.
     * No modification is done to the buffers, so the user can apply their
     * own VAD (Voice Activation Detection) or Silence Detection
     *
     * @param dataClient
     * @param speechRecognitionMode
     */
    void doDataRecognition(DataRecognitionClient dataClient, SpeechRecognitionMode recoMode)
    {
        try {
            // Note for wave files, we can just send data from the file right to the server.
            // In the case you are not an audio file in wave format, and instead you have just
            // raw data (for example audio coming over bluetooth), then before sending up any
            // audio data, you must first send up an SpeechAudioFormat descriptor to describe
            // the layout and format of your raw audio data via DataRecognitionClient's sendAudioFormat() method.
            String filename = recoMode == SpeechRecognitionMode.ShortPhrase ? "whatstheweatherlike.wav" : "batman.wav";
            InputStream fileStream = getAssets().open(filename);
            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            do {
                // Get  Audio data to send into byte buffer.
                bytesRead = fileStream.read(buffer);

                if (bytesRead > -1) {
                    // Send of audio data to service.
                    dataClient.sendAudio(buffer, bytesRead);
                }
            } while (bytesRead > 0);
        }
        catch(IOException ex) {
            Contract.fail();
        }
        finally {
            dataClient.endAudio();
        }
    }

    public void connect() {
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {

                    //client = new Socket("182.55.163.130", 9999);
                    client = new Socket("192.168.0.172", 9998);
                    printwriter = new PrintWriter(client.getOutputStream(), true);
                    printwriter.write(txt_SpeechInput.getText().toString()); // write the message to output stream
                    printwriter.flush();
                    printwriter.close();
                    boolean connection = true;
                    Log.d("socket", "connected " + connection);

                    // Toast in background becauase Toast cannnot be in main thread you have to create runOnuithread.
                    // this is run on ui thread where dialogs and all other GUI will run.
                    if (client.isConnected()) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                //Do your UI operations like dialog opening or Toast here
                                txt_Status.setText("Connected");
                                Toast.makeText(getApplicationContext(), "Messege send", Toast.LENGTH_SHORT).show();
                                txt_Status.setText("Data Sent");
                            }
                        });
                    }
                }
                catch (UnknownHostException e2){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Do your UI operations like dialog opening or Toast here
                            Toast.makeText(getApplicationContext(), "Unknown host please make sure IP address", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                catch (IOException e1) {
                    Log.d("socket", "IOException");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Do your UI operations like dialog opening or Toast here
                            txt_Status.setText("Something is wrong");
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).start();
    }

}
