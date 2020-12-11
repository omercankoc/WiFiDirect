package com.carvio.wifimessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ActivityMessenger extends AppCompatActivity {

    private LinearLayout linearLayoutView;

    private LinearLayout linearLayoutConnection;
    private Button buttonStatus;
    private Button buttonDiscovery;

    private ListView listViewDevices;

    private LinearLayout linearLayoutMessenger;
    TextView textViewInfo;
    private TextView textViewMessage;
    private Button buttonSend;
    private EditText editTextMessage;

    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;

    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> listDevices = new ArrayList<WifiP2pDevice>();
    String[] arrayDeviceNames;
    WifiP2pDevice[] arrayDevices;

    static final int MESSAGE_READ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        initializeUIs();
        statusControl();
        statusChange();
        discoveryDevices();
        lister();

    }

    Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readBuffer = (byte[])msg.obj;
                    String tempMessage = new String(readBuffer,0,msg.arg1);
                    textViewMessage.setText(tempMessage);
            }
            return true;
        }
    });


    public void initializeUIs() {
        linearLayoutView = findViewById(R.id.linearLayoutView);
        linearLayoutConnection = findViewById(R.id.linearLayoutConnection);
        buttonStatus = findViewById(R.id.buttonStatus);
        buttonDiscovery = findViewById(R.id.buttonDiscovery);
        listViewDevices = findViewById(R.id.listViewDevices);
        linearLayoutMessenger = findViewById(R.id.linearLayoutMessenger);
        textViewInfo = findViewById(R.id.textViewInfo);
        textViewMessage = findViewById(R.id.textViewMessage);
        buttonSend = findViewById(R.id.buttonSend);
        editTextMessage = findViewById(R.id.editTextMessage);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        broadcastReceiver = new MessengerBroadcastReceiver(wifiP2pManager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.getDeviceList().equals(listDevices)) {
                listDevices.clear();
                listDevices.addAll(wifiP2pDeviceList.getDeviceList());
                arrayDeviceNames = new String[wifiP2pDeviceList.getDeviceList().size()];
                arrayDevices = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()){
                    arrayDeviceNames[index] = device.deviceName;
                    arrayDevices[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,arrayDeviceNames);
                listViewDevices.setAdapter(adapter);

                if(listDevices.size() == 0){
                    Toast.makeText(getApplicationContext(),"No Devices Found!",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                textViewInfo.setText("Server");
            } else if(wifiP2pInfo.groupFormed) {
                textViewInfo.setText("Client");
            }
        }
    };

    public void statusControl() {
        if (wifiManager.isWifiEnabled()) {
            buttonStatus.setText("WiFi is Enable");
        } else {
            buttonStatus.setText("Wifi is Disable");
        }
    }

    public void statusChange() {
        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    buttonStatus.setText("Wifi is Disable");
                } else {
                    wifiManager.setWifiEnabled(true);
                    buttonStatus.setText("WiFi is Enable");
                }
            }
        });
    }

    public void discoveryDevices() {
        buttonDiscovery.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        textViewInfo.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int i) {
                        textViewInfo.setText("Discovery is Failed");
                    }
                });
            }
        });
    }

    public void lister(){
        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = arrayDevices[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connectec to" +
                                device.deviceName,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(),"Not Connected!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    public class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket = skt;
            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    public class ServerThread extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try{
                serverSocket = new ServerSocket(8080);
                socket = serverSocket.accept();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public class ClientThread extends Thread{
        Socket socket;
        String host;

        public ClientThread(InetAddress hostAddress){
            host = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try{
                socket.connect(new InetSocketAddress(host,8080),500);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}