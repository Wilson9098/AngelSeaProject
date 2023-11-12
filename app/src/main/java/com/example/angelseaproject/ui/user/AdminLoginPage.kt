package com.example.angelseaproject.ui.user

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.angelseaproject.R
import com.example.angelseaproject.databinding.AdminAuthenticateBinding
import com.example.angelseaproject.ui.home.HomeFragmentDirections


class AdminLoginPage : Fragment(R.layout.admin_authenticate) {
    private var _binding: AdminAuthenticateBinding? = null
    private val binding get() = _binding!!
    private var adminPassword = "123"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AdminAuthenticateBinding.bind(view)

        binding.adminAuthenticateButton.setOnClickListener {
            val input = binding.myAdminPassword.text.toString()

            if (input == adminPassword) {
                val action = AdminLoginPageDirections.actionAdminLoginPageToOperationFragment()
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
            }
        }
    }

}