package ru.yodata.whereareyou

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

/*
Библиотека для получения приложением любых Android-разрешений от пользователя.
Представляет собой класс PermissionsAccess со статическими методами и свойствами,
т.е создавать экземпляр класса PermissionsAccess не нужно. Пример вызова в приложении:
   PermissionsAccess.startRequest(...) - запуск процесса получения разрешений.
   PermissionsAccess.deniedPermissions - список разрешений, в получении которых пользователь
   в итоге отказал.

ПОРЯДОК ИСПОЛЬЗОВАНИЯ:
1. В начале файла фрагмента (.kt), из которого будет происходить запрос разрешений (как правило
самый первый фрагмент, с которого начинается работа приложения) на самом верхнем уровне создать
массив String с нужными разрешениями (скопировать):
val askPermissions = arrayOf<String>(
        // Разрешения получения местоположения
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
)
Так же необходимо все эти разрешения внести в файл AndroidManifest.xml перед тегом <application...
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

2. Если локалей запланировано несколько, создать их (кроме английской, она будет локалью по умолчанию).

3. В файл ресурсов строк res\values\strings\strings.xml соответствующей локали скопировать
    следующие строки:
- для английского языка (если локалей несколько, строки для английского языка следует копировать
  в локаль по умолчанию (default), а не в английскую локаль (её в этом случае создавать не нужно)):
    <string name="permissions_request_introduction_title">Permissions required</string>
    <string name="permissions_request_introduction_msg">Now you will be asked for permissions, without which the application will not be able to work. Please confirm all requested permissions.</string>
    <string name="permissions_request_epilogue_title">Permissions required</string>
    <string name="permissions_request_epilogue_msg">Please accept all requested permissions. Without them, the application can not work.</string>
    <string name="permissions_request_epilogue_ok_btn">Grant permissions</string>
    <string name="permissions_request_epilogue_cancel_btn">No, exit the app</string>

- для русского языка:
    <string name="permissions_request_introduction_title">Требуются разрешения</string>
    <string name="permissions_request_introduction_msg">Сейчас будут запрошены разрешения, без которых приложение не сможет работать. Просим подтвердить все запрашиваемые разрешения.</string>
    <string name="permissions_request_epilogue_title">Требуются разрешения</string>
    <string name="permissions_request_epilogue_msg">Пожалуйста примите все запрошенные разрешения. Без них работа приложения невозможна.</string>
    <string name="permissions_request_epilogue_ok_btn">Предоставить разрешения</string>
    <string name="permissions_request_epilogue_cancel_btn">Нет, выйти из приложения</string>
Содержание надписей можно изменить по своему желанию, имена строк менять нельзя.

4. В функцию onCreateView фрагмента внести:
    if (savedInstanceState == null) PermissionsAccess.startRequest(
                requireContext(),this, askPermissions, true)

5. Внутри класса фрагмента переопределить метод onRequestPermissionsResult. Скопировать:
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (!PermissionsAccess.finishRequestSuccessful(
                        requireContext(),
                        this,
                        true))
            requireActivity().onBackPressed() // команда выхода из приложения
    }

*/

open class PermissionsAccess {  //: ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val TAG = "PERM"
        const val PERMISSION_REQUEST_CODE = 123
        // Список запрещенных разрешений. Изначально пуст. Заполняется в функции isAvailable
        val deniedPermissions = mutableListOf<String>()
        var requestFinished = false

        // Функция проверяет доступность заданных в permissions разрешений (но не запрашивает их)
        fun isAvailable(context: Context, permissions: Array<String>): Boolean {
            var availability = true
            deniedPermissions.clear()
            // Цикл по указанным разрешениям для проверки их доступности
            for (permission in permissions) {
                val checkPermission = ActivityCompat.checkSelfPermission(context, permission)
                Log.d(TAG, "${permission.toString()}: $checkPermission")
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    // Если текущее разрешение еще не дано, занести его в список запрещенных
                    deniedPermissions.add(permission)
                    // Хотя бы одно не данное разрешение означает отрицательный результат всей проверки
                    availability = false
                }
            }
            return availability
        }

        // Функция запрашивает заданные в permissions разрешения у пользователя, если они
        // еще не получены.
        // showIntro == true перед запросом разрешений выводит на экран предупреждение (диалог),
        // оповещающее пользователя о последующем запросе.
        fun startRequest(context: Context,
                         fragment: Fragment,
                         permissions: Array<String>,
                         showIntro: Boolean,
                         requestCode: Int = PERMISSION_REQUEST_CODE)  {
            if (!isAvailable(context, permissions)) {
                // Разрешения еще не получены - запросить их
                requestFinished = false
                Log.d(TAG, "Начинаю запрос разрешений у пользователя...")
                if (showIntro) { // Показать предупреждение пользователю перед запросом разрешений...
                    val dialog = IntroDialogFragment()
                    dialog.setCancelable(false)
                    //dialog.retainInstance = true
                    dialog.show(fragment.childFragmentManager, TAG)
                }
                // иначе запросить разрешения без предупреждения
                else fragment.requestPermissions(deniedPermissions.toTypedArray(), requestCode)
            }
        }

        // Функция предназначена для вызова в колбеке onRequestPermissionsResult, для разбора
        // результатов запроса разрешений у пользователя, запущенного функцией startRequest (см.выше).
        // - showEpilogue == true при отказе пользователя от дачи разрешений выводит на экран
        // диалог, в котором можно повторно запустить процесс выдачи разрешений, либо выйти
        // из приложения.
        // При showEpilogue == false диалог не выводится, функция завершает работу. При этом
        // если разрешения были даны пользователем, функция возвращает значение true, иначе - false.
        // В последнем случае завершения работы приложения не происходит, это остается на усмотрение
        // разработчика, который должен ориентироваться на значение, возвращаемое функцией.
        fun finishRequestSuccessful(context: Context,
                                    fragment: Fragment,
                                    showEpilogue: Boolean,
                          ): Boolean {
            var success = true
            requestFinished = true
            //Log.d( PermissionsAccess.TAG, "Сработал onRequestPermissionsResult")
            Log.d(TAG, "Запрос разрешений окончен. Результат:")
            if (!isAvailable(context, deniedPermissions.toTypedArray())) {
                if (showEpilogue) {
                    val dialog = EpilogueDialogFragment()
                    dialog.setCancelable(false)
                    //dialog.retainInstance = true
                    dialog.show(fragment.childFragmentManager, TAG + "2")
                }
                else success = false
            }
            else Log.d(TAG, "Все разрешения получены.")
            return success
        }

        // Класс для создания диалога-предупреждения пользователя о запуске запроса разрешений
        class IntroDialogFragment : DialogFragment() {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return requireParentFragment().let {
                    val builder = AlertDialog.Builder(it.requireContext())
                    builder.setTitle(getString(R.string.permissions_request_introduction_title))
                            .setMessage(getString(R.string.permissions_request_introduction_msg))
                            //.setCancelable(false) - здесь это не работает, нужно вызывать при создании фрагмента
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                requireParentFragment().requestPermissions(deniedPermissions.toTypedArray(),
                                        PERMISSION_REQUEST_CODE)
                            }
                    builder.create()
                } //?: throw IllegalStateException("Activity cannot be null")
            }

        }

        // Класс для создания диалога повторного запроса разрешений у пользователя после того,
        // как он отказался их дать с первого раза
        class EpilogueDialogFragment : DialogFragment() {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return requireParentFragment().let {
                    val builder = AlertDialog.Builder(it.requireContext())
                    builder.setTitle(getString(R.string.permissions_request_epilogue_title))
                            .setMessage(getString(R.string.permissions_request_epilogue_msg))
                            //.setCancelable(false) - здесь это не работает, нужно вызывать при создании фрагмента
                            .setPositiveButton(R.string.permissions_request_epilogue_ok_btn) {
                                _, _ ->
                                Log.d(TAG, "Повторный запрос разрешений.")
                                requireParentFragment().requestPermissions(
                                        deniedPermissions.toTypedArray(),
                                        PERMISSION_REQUEST_CODE)
                            }
                            .setNegativeButton(R.string.permissions_request_epilogue_cancel_btn) {
                                _, _ ->
                                Log.d(TAG, "Отказ в выдаче разрешений! Работа приложения завершена.")
                                requireActivity().onBackPressed() // команда выхода из приложения
                            }
                    builder.create()
                }
            }

        }
    }
}