package ru.yodata.whereareyou

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents.getMessagesFromIntent
import androidx.core.content.ContextCompat.startActivity
import ru.yodata.whereareyou.model.PlainSms
import ru.yodata.whereareyou.view.ReceiverActivity
import java.util.*

// BroadcastReceiver обрабатывающий входящие SMS
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras // получить данные интента (SMS)
        if(bundle!=null) {
            // Запись всех частей SMS в массив строк
            val smsParts = getMessagesFromIntent(intent)
            // "Своя" SMS определяется по заголовку: если заголовок SMS совпадает с заданным,
            // значит это "своя" SMS и ее нужно передать в ReceiverActivity на обработку.
            // Иначе просто ничего не делать.
            if (smsParts[0].displayMessageBody.substring(SMS_HEADER_LENGHT) == SMS_HEADER) {
                // Сформировать интент на вызов ReceiverActivity, в который вложить данные
                // полученной SMS для дальнейшей обработки
                val smsData = PlainSms(
                        phoneNumber = smsParts[0].displayOriginatingAddress,
                        time = Date(),
                        smsText = smsParts.joinToString { "" }
                )
                val startIntent = Intent(context, ReceiverActivity::class.java).apply {
                    putExtra(SMS_BUNDLE_KEY, smsData) }
                context!!.startActivity(startIntent)
                //abortBroadcast()
            }
        }
    }
}