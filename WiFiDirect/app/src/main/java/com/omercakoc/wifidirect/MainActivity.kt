package com.omercakoc.wifidirect

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonConnection = findViewById<Button>(R.id.buttonConnection) as Button
        val buttonDiscover = findViewById<Button>(R.id.buttonDiscover) as Button
        val buttonSent = findViewById<Button>(R.id.buttonSent) as Button
        val listViewConnections = findViewById<ListView>(R.id.listViewConnections) as ListView
        val textViewMessage = findViewById<TextView>(R.id.textViewMessage) as TextView
        val textViewStatus = findViewById<TextView>(R.id.textViewStatus) as TextView
        val editTextMessage = findViewById<EditText>(R.id.editTextMessage) as EditText

        val wifiManager : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        buttonConnection.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view : View){
                if(wifiManager.isWifiEnabled){
                    wifiManager.setWifiEnabled(false)
                    buttonConnection.setText("Wi-Fi On")
                } else {
                    wifiManager.setWifiEnabled(true)
                    buttonConnection.setText("Wi-Fi Off")
                }
            }
        })
    }
}
