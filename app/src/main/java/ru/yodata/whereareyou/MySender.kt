package ru.yodata.whereareyou

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import ru.yodata.whereareyou.model.LocationMessage

const val TAG = "SMS"
const val SENT = "SMS_SENT"
const val DELIVERED = "SMS_DELIVERED"

class MySender {

    companion object {
        val sendBroadcastReceiver: SendBroadcastReceiver by lazy { SendBroadcastReceiver() }
        val deliveryBroadcastReceiver: DeliveryBroadcastReceiver by lazy { DeliveryBroadcastReceiver() }

        class SendBroadcastReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Toast.makeText(
                                context,
                                "Сообщение отправлено",
                                Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "SMS отправлено!")}
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Log.d(TAG, "Общая ошибка")
                    SmsManager.RESULT_ERROR_NULL_PDU -> Log.d(TAG, "PDU пуст")
                    SmsManager.RESULT_ERROR_RADIO_OFF -> Log.d(TAG, "Модуль телефона выключен")
                    SmsManager.RESULT_INVALID_SMS_FORMAT -> Log.d(TAG, "Неверный формат SMS")
                    else -> Log.d(TAG, "Прочая ошибка")
                }
                context!!.unregisterReceiver(this)
            }
        }

        class DeliveryBroadcastReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Toast.makeText(
                                context,
                                "Сообщение доставлено",
                                Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "SMS доставлено!")}
                    Activity.RESULT_CANCELED -> Log.d(TAG, "SMS не доставлено")
                    else -> Log.d(TAG, "С доставкой SMS непонятно что случилось")
                }
                context!!.unregisterReceiver(this)
            }

        }
        fun sendLocationMessage(context: Context, locationMessage: LocationMessage) {
            val sentPI = PendingIntent.getBroadcast(context, 0,  Intent(SENT), 0)
            val  deliveredPI = PendingIntent.getBroadcast(context, 0, Intent(DELIVERED), 0)
            context.registerReceiver(sendBroadcastReceiver, IntentFilter(SENT));
            context.registerReceiver(deliveryBroadcastReceiver, IntentFilter(DELIVERED));
            val smsManager = SmsManager.getDefault()
            val smsParts = smsManager.divideMessage(locationMessage.toString())
            Log.d(TAG, "SMS разделено на части: ${smsParts.size}")
            Log.d(TAG, "Длина одной части: ${smsParts[0].length}")
            val sentArrayIntents = ArrayList<PendingIntent>()
            val deliveredArrayIntents = ArrayList<PendingIntent>()
            for (part in smsParts) {
                sentArrayIntents.add(sentPI)
                deliveredArrayIntents.add(deliveredPI)
            }
            smsManager.sendMultipartTextMessage(locationMessage.abonentPhoneNumber, null,
                    smsParts, sentArrayIntents, deliveredArrayIntents)
        }
    }
}