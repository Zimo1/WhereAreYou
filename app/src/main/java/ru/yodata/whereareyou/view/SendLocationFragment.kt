package ru.yodata.whereareyou.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.navGraphViewModels
import ru.yodata.whereareyou.MySender
import ru.yodata.whereareyou.R
import ru.yodata.whereareyou.databinding.FragmentLocationRequestBinding
import ru.yodata.whereareyou.databinding.FragmentSendLocationBinding
import ru.yodata.whereareyou.model.LocationMessage
import ru.yodata.whereareyou.model.LocationMessageType
import ru.yodata.whereareyou.viewmodel.LastLocationViewModel
import java.util.*

// Инициализация View Binding
private var _sendLocFrag: FragmentSendLocationBinding? = null
private val sendLocFrag get() = _sendLocFrag!!

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SendLocationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SendLocationFragment : Fragment(R.layout.fragment_send_location) {

    private val lastLocationViewModel: LastLocationViewModel by navGraphViewModels(R.id.nav_graph)

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Необходимо для View Binding:
        _sendLocFrag = FragmentSendLocationBinding.bind(view)
        with (lastLocationViewModel.location.value!!) {
            with (sendLocFrag) {
                latitudeTv.text = latitude.toString()
                longitudeTv.text = longitude.toString()
                accuracyTv.text = accuracy.toString()
                altitudeTv.text = altitude.toString()
                speedTv.text = (speed / 1000 * 3600).toString() // данные переводятся в км/ч
                var moment = Date(time)
                timeTv.text = String.format("%02d:%02d:%02d",
                        moment.hours, moment.minutes, moment.seconds)
            }
        }
        sendLocFrag.contactListBtn.setOnClickListener {
            Toast.makeText(
                    requireContext(),
                    "Выбор из Контактов пока не реализован. Введите номер вручную",
                    Toast.LENGTH_LONG
            ).show()
        }
        sendLocFrag.sendLocationBtn.isEnabled = true
        sendLocFrag.sendLocationBtn.setOnClickListener { view -> sendButtonListener(view) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Необходимо для View Binding:
        _sendLocFrag = null
    }

    fun sendButtonListener(view: View) {
        with(sendLocFrag) {
            MySender.sendLocationMessage(requireContext(),
                    LocationMessage(id = 1,
                            requestId = 0,
                            type = if (makeRequestCBox.isChecked) LocationMessageType.REQUEST
                                    else LocationMessageType.INFO,
                            incoming = false,
                            location = lastLocationViewModel.location.value!!,
                            chargingPercentage = 0,
                            comment = commentTvEd.text.toString(),
                            abonentPhoneNumber = recipientPhoneTvEd.text.toString(),
                    ))
            view.isEnabled = false
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SendLocationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SendLocationFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}