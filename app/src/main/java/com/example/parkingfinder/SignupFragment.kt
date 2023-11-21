package com.example.parkingfinder;

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.parkingfinder.databinding.FragmentSignupBinding

public class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bnLogin.setOnClickListener {
            if(validateSignupForm()) {

            }
        }

        binding.bnGotoLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateSignupForm(): Boolean {
        val isNameValid = ValidationUtils.validateName(binding.tiName)
        val isEmailValid = ValidationUtils.validateEmail(binding.tiEmail)
        val isPwdValid = ValidationUtils.validatePassword(binding.tiPwd)
        val isConfirmValid =
            ValidationUtils.validatePassword(binding.tiConfirmPwd)
                    && ValidationUtils.checkConfirmation(binding.tiPwd, binding.tiConfirmPwd)

        return isNameValid && isEmailValid && isPwdValid && isConfirmValid
    }
}