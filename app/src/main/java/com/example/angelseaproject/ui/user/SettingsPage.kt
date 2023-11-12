package com.example.angelseaproject.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.angelseaproject.R
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.User
import com.example.angelseaproject.databinding.NurseSettingsBinding

class SettingsPage : Fragment(R.layout.nurse_settings) {
    private var _binding: NurseSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = NurseSettingsBinding.bind(view)

        binding.settingLogout.setOnClickListener {
            val intent = Intent(activity, loginPage::class.java)
            LoaclData.User = User("","","","")
            LoaclData.Operations.clear()
            LoaclData.allOperations.clear()
            startActivity(intent)
        }

        binding.teamInfo.setOnClickListener {
            Log.d("test", "?")
            val action = SettingsPageDirections.actionNavigationSettingsToTeamInfo()
            findNavController().navigate(action)
        }

        binding.btnChangePassword.setOnClickListener {
            val action = SettingsPageDirections.actionNavigationSettingsToChangePassword()
            findNavController().navigate(action)
        }

        binding.btnEditProfile.setOnClickListener {
            val action = SettingsPageDirections.actionNavigationSettingsToEditProfile()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}