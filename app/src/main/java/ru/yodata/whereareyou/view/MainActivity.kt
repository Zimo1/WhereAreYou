package ru.yodata.whereareyou.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import ru.yodata.whereareyou.R
import ru.yodata.whereareyou.viewmodel.LastLocationViewModel

class MainActivity : AppCompatActivity() {

    // ViewModel хранит последнее полученное значение Location
    private val lastLocationViewModel  by lazy {
        vita.with(VitaOwner.Multiple(this)).getViewModel<LastLocationViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}