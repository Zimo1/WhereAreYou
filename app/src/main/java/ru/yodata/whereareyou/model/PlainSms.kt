package ru.yodata.whereareyou.model

import java.io.Serializable
import java.util.*

// Класс для хранения SMS в естественном виде и передачи данных между BroadcastReceiver и Activity
data class PlainSms (
    val phoneNumber: String, // номер телефона абонента
    val time: Date, // время появления SMS
    val smsText: String // текст SMS
) : Serializable // класс сериализуемый для передачи данных
