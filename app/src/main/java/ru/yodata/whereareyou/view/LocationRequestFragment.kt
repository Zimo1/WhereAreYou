package ru.yodata.whereareyou.view

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import com.google.android.gms.maps.model.*
import ru.yodata.whereareyou.*
import ru.yodata.whereareyou.databinding.FragmentLocationRequestBinding
import ru.yodata.whereareyou.model.LocationMessage
import ru.yodata.whereareyou.model.LocationMessageType
import java.util.*

// Инициализация View Binding
private var _locationFrag: FragmentLocationRequestBinding? = null
private val locationFrag get() = _locationFrag!!

// Фрагмент, в котором происходит запрос локации абонента без передачи ему своей локации
class LocationRequestFragment : Fragment(R.layout.fragment_location_request) {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_location_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        super.onViewCreated(view, savedInstanceState)
        // Необходимо для View Binding:
        _locationFrag = FragmentLocationRequestBinding.bind(view)
        // Кнопка очистки комментария
        locationFrag.commentClearBtn2.setOnClickListener {
            locationFrag.commentTvEd2.setText("") }
        // Назначить лиснер кнопке перехода на экран запроса местоположения у другого пользователя
        locationFrag.sendRequestBtn.setOnClickListener{
            requestButtonListener(it)
            requireActivity().onBackPressed() // вернуться на предыдущий экран
        }

        Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")

    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Необходимо для View Binding:
        _locationFrag = null
    }

    // Слушатель кнопки запроса данных о местоположении абонента
    fun requestButtonListener(button: View) {
        with(locationFrag) {
            MySender.sendLocationMessage(requireContext(),
                LocationMessage(id = 1,
                    type = LocationMessageType.REQUEST,
                    incoming = true,
                    chargingPercentage = 0,
                    comment = commentTvEd2.text.toString(),
                    abonentPhoneNumber = recipientPhoneTvEd2.text.toString(),
                )
            )
            button.isEnabled = false
        }
    }
}