package ru.yodata.whereareyou

import android.location.Location

// Интерфейс со своим собственным методом onLocationChanged, который нужен для подмены встроенного
// одноименного метода onLocationChanged системного интерфейса android.location.LocationListener
// Именно сюда приходит значение новой локации (Location) от датчиков позиционирования при
// возникновении события "смена позиции".
// Интерфейс нужен, чтобы передать эту локацию во фрагмент, занимающийся обработкой принимаемых
// локаций. Фрагмент должен реализовывать этот интерфейс.
interface LocationListenable {

    fun onLocationChanged(newLocation: Location) {

    }
}