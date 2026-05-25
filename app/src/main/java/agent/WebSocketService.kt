package com.agent

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

class WebSocketService : Service() {

    private lateinit var ws: WebSocketClient

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startAgentForeground()

        connectWebSocket()

        return START_STICKY
    }

    private fun startAgentForeground() {

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

        val notification = NotificationCompat.Builder(
            this,
            channelId
        )
            .setContentTitle("Phone Agent")
            .setContentText("Running")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun connectWebSocket() {

        val serverIp = "109.248.11.133"

        ws = object : WebSocketClient(
            URI("ws://$serverIp:8000/ws/android1")
        ) {

            override fun onOpen(
                handshakedata: ServerHandshake?
            ) {

                println("CONNECTED")
            }

            override fun onMessage(message: String?) {

                try {

                    val json = JSONObject(message!!)
                    val type = json.getString("type")

                    when(type) {

                        "battery" -> {

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

                        "vibrate" -> {

                            val vibrator = getSystemService(
                                VIBRATOR_SERVICE
                            ) as Vibrator

                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    1000,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        }

                        "flash" -> {

                            val cameraManager = getSystemService(
                                CAMERA_SERVICE
                            ) as CameraManager

                            val cameraId =
                                cameraManager.cameraIdList[0]

                            cameraManager.setTorchMode(
                                cameraId,
                                true
                            )

                            Thread {

                                Thread.sleep(3000)

                                cameraManager.setTorchMode(
                                    cameraId,
                                    false
                                )

                            }.start()
                        }

                        "ring" -> {

                            val audioManager = getSystemService(
                                AUDIO_SERVICE
                            ) as AudioManager

                            audioManager.setStreamVolume(
                                AudioManager.STREAM_RING,
                                audioManager.getStreamMaxVolume(
                                    AudioManager.STREAM_RING
                                ),
                                0
                            )

                            val notification =
                                RingtoneManager.getDefaultUri(
                                    RingtoneManager.TYPE_RINGTONE
                                )

                            val ringtone =
                                RingtoneManager.getRingtone(
                                    applicationContext,
                                    notification
                                )

                            ringtone.play()
                        }
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

            while (!ws.isOpen) {

                try {

                    Thread.sleep(5000)

                    connectWebSocket()

                } catch (e: Exception) {

                    println(e.message)
                }
            }

        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}