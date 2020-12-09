package com.carvio.wifimessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityMessenger extends AppCompatActivity {

    private LinearLayout linearLayoutView;

    private LinearLayout linearLayoutConnection;
    private Button buttonStatus;
    private Button buttonDiscovery;

    private ListView listViewDevices;

    private LinearLayout linearLayoutMessenger;
    private TextView textViewMessage;
    private Button buttonSend;
    private EditText editTextMessage;

    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        initializeUIs();
        statusControl();
        statusChange();

    }

    public void initializeUIs(){
        linearLayoutView = findViewById(R.id.linearLayoutView);
        linearLayoutConnection = findViewById(R.id.linearLayoutConnection);
        buttonStatus = findViewById(R.id.buttonStatus);
        buttonDiscovery = findViewById(R.id.buttonDiscovery);
        listViewDevices = findViewById(R.id.listViewDevices);
        linearLayoutMessenger = findViewById(R.id.linearLayoutMessenger);
        textViewMessage = findViewById(R.id.textViewMessage);
        buttonSend = findViewById(R.id.buttonSend);
        editTextMessage = findViewById(R.id.editTextMessage);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void statusControl(){
        if(wifiManager.isWifiEnabled()){
            buttonStatus.setText("WiFi is Enable");
        } else {
            buttonStatus.setText("Wifi is Disable");
        }
    }

    public void statusChange(){
        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                    buttonStatus.setText("Wifi is Disable");
                } else {
                    wifiManager.setWifiEnabled(true);
                    buttonStatus.setText("WiFi is Enable");
                }
            }
        });
    }
}