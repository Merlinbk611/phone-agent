package com.agent

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import org.json.JSONObject

class MainActivity : Activity() {

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textView = TextView(this)
        textView.text = "STARTING"

        setContentView(textView)

        try {

            val serverIp = "109.248.11.133"

            val ws = object : WebSocketClient(
                URI("ws://$serverIp:8000/ws/android1")
            ) {

                override fun onOpen(handshakedata: ServerHandshake?) {
                    runOnUiThread {
                        textView.text = "CONNECTED"
                    }
                }

                override fun onMessage(message: String?) {

    runOnUiThread {
        textView.text = message
    }

    try {

        val json = JSONObject(message!!)
        val type = json.getString("type")

        if (type == "battery") {

            val batteryStatus = registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val level = batteryStatus?.getIntExtra(
                BatteryManager.EXTRA_LEVEL,
                -1
            )

            val response = JSONObject()
            response.put("type", "battery")
            response.put("value", level)

            send(response.toString())
        }

    } catch (e: Exception) {

        runOnUiThread {
            textView.text = "JSON ERROR"
        }
    }
}

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    runOnUiThread {
                        textView.text = "CLOSED"
                    }
                }

                override fun onError(ex: Exception?) {
                    runOnUiThread {
                        textView.text = "ERROR: ${ex?.message}"
                    }
                }
            }

            ws.connect()

        } catch (e: Exception) {

            textView.text = "CRASH: ${e.message}"
        }
    }
}