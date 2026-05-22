package com.agent

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MainActivity : AppCompatActivity() {

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textView = TextView(this)
        textView.text = "Connecting..."

        setContentView(textView)

        val serverIp = "109.248.11.133"

        val ws = object : WebSocketClient(
            URI("ws://$serverIp:8000/ws/android1")
        ) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                runOnUiThread {
                    textView.text = "Connected!"
                }
            }

            override fun onMessage(message: String?) {
                runOnUiThread {
                    textView.text = "CMD: $message"
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                runOnUiThread {
                    textView.text = "Disconnected"
                }
            }

            override fun onError(ex: Exception?) {
                runOnUiThread {
                    textView.text = "ERROR"
                }
            }
        }

        ws.connect()
    }
}