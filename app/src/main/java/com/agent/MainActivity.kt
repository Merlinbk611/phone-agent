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
        textView.text = "Starting..."

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
                        textView.text = "MSG: $message"
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