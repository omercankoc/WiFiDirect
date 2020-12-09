package com.carvio.wifimessenger;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class MessengerBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private ActivityMessenger activityMessenger;

    public MessengerBroadcastReceiver(WifiP2pManager wifiP2pManager,
                                      WifiP2pManager.Channel channel,
                                      ActivityMessenger activityMessenger){
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.activityMessenger = activityMessenger;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(context,"WiFi is ON",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context,"WiFi is OFF",Toast.LENGTH_LONG).show();
            }

        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(wifiP2pManager!=null){
                wifiP2pManager.requestPeers(channel,activityMessenger.peerListListener);
            }

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }
}
