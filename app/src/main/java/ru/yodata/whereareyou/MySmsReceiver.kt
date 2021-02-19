package ru.yodata.whereareyou

import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents.getMessagesFromIntent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import ru.yodata.whereareyou.model.PlainSms
import ru.yodata.whereareyou.view.ReceiverActivity
import java.util.*

// BroadcastReceiver является второй точкой входа в приложение, обрабатывает входящие SMS и если
// находит в SMS запрос локации, формирует Notification, которая запускает ReceiverActivity,
// производящую обработку запроса и ответ на него (если требуется)
class MySmsReceiver : BroadcastReceiver() {
    /*companion object {
        var notificationIdCounter = 2
    }*/
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
            // "Своя" SMS определяется по заголовку: если заголовок SMS совпадает с заданным
            // в Settings значением SMS_HEADER, значит это "своя" SMS и ее нужно передать в
            // ReceiverActivity на обработку. Иначе просто ничего не делать.
            if (smsParts[0].displayMessageBody.take(SMS_HEADER_LENGHT) == SMS_HEADER) {
                // Сформировать интент на вызов ReceiverActivity, в который вложить данные
                // полученной SMS для дальнейшей обработки
                Log.d(TAG, "Обнаружена SMS с данными локации для обработки")
                val smsData = PlainSms(
                        // номер телефона отправителя SMS
                        phoneNumber = smsParts[0].displayOriginatingAddress,
                        time = Date(), // текущее время и дата
                        // сам текст SMS, склееный из частей, если их несколько
                        smsText = smsParts.joinToString("") {it -> it.displayMessageBody}
                )
                val startIntent = Intent(context, ReceiverActivity::class.java).apply {
                    putExtra(SMS_BUNDLE_KEY, smsData)
                    putExtra(MODE_BUNDLE_KEY, ReceiverActivityMode.SMS)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                // Прочитать счетчик id сообщений из SharedPreferences
                var curMessageId = mySharedPreferences.getInt(MESSAGE_ID, START_MESSAGE_ID)
                // ReceiverActivity будет запускаться через нотификацию, т.к сейчас система блокирует
                // вызов активити из background напрямую. Для нотификации нужен PendingIntent.
                // PendingIntent на каждую нотификацию будет свой (curMessageId) и он будет
                // автоматически закрываться после одного запуска (флаг FLAG_ONE_SHOT)
                val pendingStartIntent: PendingIntent = PendingIntent.getActivity(context,
                        curMessageId, startIntent, FLAG_ONE_SHOT)
                val notificationBuilder = NotificationCompat.Builder(context!!, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_menu_compass)
                        .setContentTitle(smsData.phoneNumber)
                        .setContentText("#$curMessageId ${context.getString(
                                                            R.string.notification_content_text)}")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // показать на lockscreen
                        .setSound(notificationSound)
                        // запуск ReceiverActivity по тапу пользователя на изображении нотификации
                        .setFullScreenIntent(pendingStartIntent, true)
                        //.setContentIntent(pendingStartIntent)
                        // автоматически закрывает notification, когда пользователь тапает по нему
                        .setAutoCancel(true)
                // Запустить отображение нотификации на экране
                with (NotificationManagerCompat.from(context)) {
                    notify(TAG, curMessageId, notificationBuilder.build())
                    // Увеличить значение счетчика id сообщений и записать его в SharedPreferences
                    curMessageId++
                    mySharedPreferences.edit().putInt(MESSAGE_ID, curMessageId).apply()
                }
                //context!!.startActivity(startIntent) // запуск интента
                Log.d(TAG, "Отправлено notification на запуск ReceiverActivity")
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