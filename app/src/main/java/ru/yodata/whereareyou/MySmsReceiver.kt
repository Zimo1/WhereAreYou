package ru.yodata.whereareyou

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents.getMessagesFromIntent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import ru.yodata.whereareyou.model.PlainSms
import ru.yodata.whereareyou.view.ReceiverActivity
import java.util.*

// BroadcastReceiver является второй точкой входа в приложение, обрабатывает входящие SMS и если
// находит запрос локации, запускает ReceiverActivity, в которой происходит обработка запроса и
// ответ на него (если требуется)
class MySmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "BroadcastReceiver запущен!")
        /*Toast.makeText(
            context,
            "BroadcastReceiver запущен!",
            Toast.LENGTH_LONG
        ).show()*/
        val bundle = intent?.extras // получить данные интента (SMS)
        if (bundle != null) {
            Log.d(TAG, "BroadcastReceiver extras содержит данные")
            // Запись всех частей SMS в массив строк
            val smsParts = getMessagesFromIntent(intent)
            /*Toast.makeText(
                context,
                "Заголовок: ${smsParts[0].displayMessageBody.take(SMS_HEADER_LENGHT)}",
                Toast.LENGTH_LONG
            ).show()*/
            // "Своя" SMS определяется по заголовку: если заголовок SMS совпадает с заданным
            // в Settings значением SMS_HEADER, значит это "своя" SMS и ее нужно передать в
            // ReceiverActivity на обработку. Иначе просто ничего не делать.
            if (smsParts[0].displayMessageBody.take(SMS_HEADER_LENGHT) == SMS_HEADER) {
                // Сформировать интент на вызов ReceiverActivity, в который вложить данные
                // полученной SMS для дальнейшей обработки
                Log.d(TAG, "Обнаружена SMS с данными локации для обработки")
                val smsData = PlainSms(
                        phoneNumber = smsParts[0].displayOriginatingAddress, // номер телефона отправителя
                        time = Date(), // текущее время и дата
                        // сам текст SMS, склееный из частей, если их несколько
                        smsText = smsParts.joinToString("") {it -> it.displayMessageBody}
                )
                val startIntent = Intent(context, ReceiverActivity::class.java).apply {
                    putExtra(SMS_BUNDLE_KEY, smsData)
                    putExtra(MODE_BUNDLE_KEY, ReceiverActivityMode.SMS)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)} // без этого флага будет вылет
                context!!.startActivity(startIntent) // запуск интента
                Log.d(TAG, "Отправлен интент на запуск ReceiverActivity")
                abortBroadcast() // не пропускать эту SMS дальше, чтобы она не появилась в списке SMS - не работает
            }
        }
        /*else Toast.makeText(
            context,
            "BroadcastReceiver extras пуст!",
            Toast.LENGTH_LONG
        ).show()*/
    }
}