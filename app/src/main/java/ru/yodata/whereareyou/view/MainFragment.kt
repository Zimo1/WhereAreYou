package ru.yodata.whereareyou.view

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import ru.yodata.whereareyou.PermissionsAccess
import ru.yodata.whereareyou.R
import ru.yodata.whereareyou.databinding.FragmentMainBinding

// Инициализация View Binding
private var _mainFrag: FragmentMainBinding? = null
private val mainFrag get() = _mainFrag!!

// Массив, в котором указываются все необходимые приложению разрешения для запроса их у пользователя
val askPermissions = arrayOf<String>(
        // Разрешения получения местоположения
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        //Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        // Разрешения для работы с SMS
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
)

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

// Главный экран.
// С него начинается работа приложения
class MainFragment : Fragment(R.layout.fragment_main) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        // Запрос разрешений
        if (savedInstanceState == null) PermissionsAccess.startRequest(
                requireContext(),this, askPermissions, true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _mainFrag = FragmentMainBinding.bind(view)
        // Кнопка перехода на экран работы с GPS и картой
        mainFrag.getLocationBtn.setOnClickListener{ view ->
                view.findNavController()
                .navigate(R.id.action_mainFragment_to_locationRequestFragment)}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Необходимо для View Binding:
        _mainFrag = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // Функция возвращает результат запроса разрешений у пользователя (разрешил или нет).
    // Если хотя бы одно разрешение не получено, приложение завершается, т.к. без разрешений
    // его работа невозможна.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        // Проверить даны ли пользователем запрошенные разрешения
        if (!PermissionsAccess.finishRequestSuccessful(
                        requireContext(),
                        this,
                        true))
            requireActivity().onBackPressed() // команда выхода из приложения
    }
}