package com.example.test.ui.main.children

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test.application.SharedManager
import com.example.test.databinding.FragmentMyInfoBinding
import com.example.test.ui.intro.IntroActivity
import com.example.test.ui.my_info.MyReservationsActivity

class MyInfoFragment : Fragment() {
    private var _binding: FragmentMyInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            nameTextView.text = SharedManager.getInstance().getCurrentUser()?.username

            reservationHistoryButton.setOnClickListener {
                startActivity(Intent(requireContext(), MyReservationsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }

            logOut.setOnClickListener {
                SharedManager.getInstance().run {
                    saveBearerToken(null)
                    saveCurrentUser(null)
                }

                startActivity(Intent(requireContext(), IntroActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                requireActivity().finish()
            }
        }
    }
}