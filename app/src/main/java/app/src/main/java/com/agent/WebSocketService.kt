package com.agent

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class WebSocketService : Service() {

    lateinit var ws: WebSocketClient

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForegroundService()

        connectWebSocket()

        return START_STICKY
    }

    private fun connectWebSocket() {

        val serverIp = "109.248.11.133"

        ws = object : WebSocketClient(
            URI("ws://$serverIp:8000/ws/android1")
        ) {

            override fun onOpen(handshakedata: ServerHandshake?) {

                println("CONNECTED")
            }

            override fun onMessage(message: String?) {

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

                    println(e.message)
                }
            }

            override fun onClose(
                code: Int,
                reason: String?,
                remote: Boolean
            ) {

                reconnect()
            }

            override fun onError(ex: Exception?) {

                reconnect()
            }
        }

        ws.connect()
    }

    private fun reconnect() {

        Thread {
            Thread.sleep(5000)

            connectWebSocket()
        }.start()
    }

    private fun startForegroundService() {

        val channelId = "agent_service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Agent Service",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Phone Agent")
            .setContentText("Connected")
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}