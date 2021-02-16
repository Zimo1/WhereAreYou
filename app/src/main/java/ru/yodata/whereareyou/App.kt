package ru.yodata.whereareyou

import android.app.Application
import com.androidisland.vita.startVita

// Класс необходим, чтобы получить доступ к библиотеке Vita, которая управляет вьюмоделями
// https://github.com/FarshadTahmasbi/Vita
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startVita()
    }
}