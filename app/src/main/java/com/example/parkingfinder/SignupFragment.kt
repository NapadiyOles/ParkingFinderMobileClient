package com.example.parkingfinder;

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.parkingfinder.databinding.FragmentSignupBinding
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

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

        binding.bnSignup.setOnClickListener {
            if(validateSignupForm()) {
                val name = binding.tiName.editText?.text.toString()
                val email = binding.tiEmail.editText?.text.toString()
                val pwd = binding.tiPwd.editText?.text.toString()
                registerUser(name, email, pwd)
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

    private fun saveUserData() {
        // Retrieve user input
        val name = binding.tiName.editText?.text.toString()
        val email = binding.tiEmail.editText?.text.toString()
        // Additional user data can be retrieved similarly

        // Open SharedPreferences editor
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("UserName", name)
            putString("UserEmail", email)
            // You can save additional user data here
            apply() // Use apply() to save the preferences asynchronously
        }
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

    private fun registerUser(name: String, email: String, password: String) {
        val url = "http://10.0.2.2:5222/authentication/register"  // Use 10.0.2.2 for localhost
//        val url = "http://localhost:5222/authentication/register"

        val params = HashMap<String, String>()
        params["name"] = name
        params["email"] = email
        params["password"] = password

        val jsonObject = JSONObject(params as Map<*, *>)

        val queue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            { response ->
                // Handle the successful response
                handleResponse(response)
            },
            { error ->
                val body = error.networkResponse?.data?.let { String(it, Charset.forName("UTF-8")) }
                val message = when (error.networkResponse?.statusCode) {
                    400 -> {
                        // Handle incorrect user data
                        body ?: "Bad request"
                    }
                    else -> {
                        // Handle other errors
                        body ?: "Unknown error"
                    }
                }

                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun handleResponse(response: JSONObject) {
        try {
            val guid = response.getString("guid")
            val name = response.getString("name")
            val email = response.getString("email")
            val token = response.getString("token")

            saveToSharedPreferences(guid, name, email, token)
            findNavController().navigate(R.id.action_signupFragment_to_profileFragment)
        } catch (e: JSONException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToSharedPreferences(guid: String, name: String, email: String, token: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("UserId", guid)
            putString("UserName", name)
            putString("UserEmail", email)
            putString("UserToken", token)
            putString("UserStatus", "Away")
            apply()
        }
    }


}