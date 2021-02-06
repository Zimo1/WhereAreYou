package ru.yodata.whereareyou.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.yodata.whereareyou.R
import ru.yodata.whereareyou.SMS_BUNDLE_KEY

class ReceiverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)

        val plainSms = intent.getStringExtra(SMS_BUNDLE_KEY)

    }
}