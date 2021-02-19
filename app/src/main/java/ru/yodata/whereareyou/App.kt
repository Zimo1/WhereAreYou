package ru.yodata.whereareyou

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.androidisland.vita.startVita

// Класс необходим, чтобы получить доступ к библиотеке Vita, которая управляет вьюмоделями
// https://github.com/FarshadTahmasbi/Vita
// А так же здесь происходит создание и регистрация канала нотификаций
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация библиотеки вьюмоделей
        startVita()
        // Инициализация счетчика id сообщений в SharedPreferences (если его там еще не было)
        mySharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        if (!mySharedPreferences.contains(MESSAGE_ID))
            mySharedPreferences.edit().putInt(MESSAGE_ID, START_MESSAGE_ID).apply()
        // Звук нотификации
        notificationSound = Uri.parse("android.resource://" + packageName +
                "/" + R.raw.echosms)
        // Создание канала нотификации
        createNotificationChannel()


    }

    // Создание и регистрация канала нотификаций
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                setImportance(importance)
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(notificationSound, audioAttributes)
                enableVibration(true)
                setShowBadge(true)

            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
