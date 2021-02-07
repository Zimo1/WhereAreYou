package ru.yodata.whereareyou.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.yodata.whereareyou.R
import ru.yodata.whereareyou.SMS_BUNDLE_KEY
import ru.yodata.whereareyou.databinding.ActivityReceiverBinding
import ru.yodata.whereareyou.model.PlainSms

// Вторая точка входа в приложение находится в постоянном броадкаст ресивере MySmsReceiver.
// Он просматривает все входящие SMS и если находит в них запрос локации, запускает данное
// ReceiverActivity, в котором происходит обработка запроса и ответ на него (если требуется)
class ReceiverActivity : AppCompatActivity() {
    // Инициализация View Binding
    private lateinit var activity: ActivityReceiverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация View Binding
        activity = ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(activity.root)   //(R.layout.activity_receiver)

        val plainSms = intent.getSerializableExtra(SMS_BUNDLE_KEY) as PlainSms
        activity.abonentTv.text = plainSms.smsText

    }
}