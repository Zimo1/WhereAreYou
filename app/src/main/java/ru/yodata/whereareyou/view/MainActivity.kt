package ru.yodata.whereareyou.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.yodata.whereareyou.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        Log.d("PERM", "Callback получен в Активити!")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    } */
}