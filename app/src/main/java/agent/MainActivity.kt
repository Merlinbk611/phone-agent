package com.agent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val status = TextView(this)
        status.text = "Phone Agent Ready"

        val button = Button(this)
        button.text = "START AGENT"

        button.setOnClickListener {

            try {

                startService(
                    Intent(this, WebSocketService::class.java)
                )

                status.text = "SERVICE STARTED"

            } catch (e: Exception) {

                status.text = "ERROR: ${e.message}"
            }
        }

        layout.addView(status)
        layout.addView(button)

        setContentView(layout)
    }
}