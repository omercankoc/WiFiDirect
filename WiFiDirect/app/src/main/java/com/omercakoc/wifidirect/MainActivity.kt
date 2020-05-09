package com.omercakoc.wifidirect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity() {

    // Definition:
    var buttonConnection : Button? = null
    var buttonDiscover : Button? = null
    var buttonSent : Button? = null
    var listViewConnections : ListView? = null
    var textViewMessage : TextView? = null
    var textViewStatus : TextView? = null
    var editTextMessage : EditText? = null

    var wifiManager : WifiManager? = null

    val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    var channel: WifiP2pManager.Channel? = null
    var receiver: BroadcastReceiver? = null

    var intentFilter : IntentFilter? = null

    private val peers = mutableListOf<WifiP2pDevice>()
    var deviceNameArray = ArrayList<String>()
    var deviceArray = ArrayList<WifiP2pDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize:
        buttonConnection = findViewById<Button>(R.id.buttonConnection) as Button
        buttonDiscover = findViewById<Button>(R.id.buttonDiscover) as Button
        buttonSent = findViewById<Button>(R.id.buttonSent) as Button
        listViewConnections = findViewById<ListView>(R.id.listViewConnections) as ListView
        textViewMessage = findViewById<TextView>(R.id.textViewMessage) as TextView
        textViewStatus = findViewById<TextView>(R.id.textViewStatus) as TextView
        editTextMessage = findViewById<EditText>(R.id.editTextMessage) as EditText

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        channel = manager?.initialize(this, mainLooper, null)
        channel?.also { channel ->
            receiver = WiFiDirectBroadcastReceiver(manager!!, channel, this)
        }

        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        buttonConnection!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view : View?){
                if(wifiManager!!.isWifiEnabled){
                    wifiManager!!.setWifiEnabled(false)
                    buttonConnection!!.setText("Wi-Fi On")
                } else {
                    wifiManager!!.setWifiEnabled(true)
                    buttonConnection!!.setText("Wi-Fi Off")
                }
            }
        })

        buttonDiscover!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                manager!!.discoverPeers(channel, object : WifiP2pManager.ActionListener{
                    override fun onSuccess() {
                        textViewStatus!!.setText("Discovery Started!")
                    }

                    override fun onFailure(reason: Int) {
                        textViewStatus!!.setText("Discovery Started Failed!")
                    }
                })
            }
        })
    }

    val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        if (peerList.deviceList != peers) {
            peers.clear()
            peers.addAll(peerList.deviceList)

            deviceNameArray = ArrayList<String>(peerList.deviceList.size)
            deviceArray = ArrayList<WifiP2pDevice>(peerList.deviceList.size)
            var index : Int = 0

            /*
            lateinit var device : WifiP2pDevice
            for(device in peerList.deviceList){
                deviceNameArray[index] = device.deviceName
                deviceArray[index] = device
            } */

            peerList.deviceList.forEach {
                deviceNameArray[index] = it.deviceName
                deviceArray[index] = it
            }

            val adapter : ArrayAdapter<String> =
                ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,deviceNameArray)
            listViewConnections!!.adapter = adapter

            if(peers.isEmpty()){
                Toast.makeText(this,"No Device Found!",Toast.LENGTH_LONG).show()
                return@PeerListListener
            }
        }
    }

    /* register the broadcast receiver with the intent values to be matched */
    override fun onResume() {
        super.onResume()
        receiver?.also { receiver ->
            registerReceiver(receiver, intentFilter)
        }
    }

    /* unregister the broadcast receiver */
    override fun onPause() {
        super.onPause()
        receiver?.also { receiver ->
            unregisterReceiver(receiver)
        }
    }
}
