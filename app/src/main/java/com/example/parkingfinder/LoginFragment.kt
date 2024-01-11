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
import com.example.parkingfinder.databinding.FragmentLoginBinding
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

public class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bnLogin.setOnClickListener {
            if (validateLoginForm()) {
                val email = binding.tiEmail.editText?.text.toString()
                val pwd = binding.tiPwd.editText?.text.toString()
                loginUser(email, pwd)
            }
        }

        binding.bnGotoSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateLoginForm(): Boolean {
        val isEmailValid = ValidationUtils.validateEmail(binding.tiEmail)
        val isPwdValid = ValidationUtils.validatePassword(binding.tiPwd)

        return isEmailValid && isPwdValid
    }

    private fun loginUser(email: String, password: String) {
        val url = "http://10.0.2.2:5222/authentication/login"

        val params = HashMap<String, String>()
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
                val message = when (error?.networkResponse?.statusCode) {
                    401 -> {
                        body ?: "User is not registered"
                    }
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
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
        } catch (e: JSONException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToSharedPreferences(guid: String, name: String, email: String, token: String) {
        val sharedPreferences =
            requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
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