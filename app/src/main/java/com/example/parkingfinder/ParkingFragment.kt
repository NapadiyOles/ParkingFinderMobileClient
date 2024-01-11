package com.example.parkingfinder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.parkingfinder.databinding.FragmentSubmitParkingBinding
import org.json.JSONObject
import java.nio.charset.Charset

class ParkingFragment : Fragment() {

    private var _binding: FragmentSubmitParkingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSubmitParkingBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bnReportPark.setOnClickListener {
            reportParking()
        }

        binding.bnReportBlock.setOnClickListener {
            reportBlocking()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun reportParking() {
        val url = "http://10.0.2.2:5222/parking/enter" // Replace with your actual URL

        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("UserId", null)
        val spotId = sharedPreferences.getString("CurrentSpot", null)
        val token = sharedPreferences.getString("UserToken", "")

        val params = JSONObject()
        params.put("userId", userId)
        params.put("spotId", spotId)

        val socketTimeout = 60000 // 30 seconds - change to what suits your needs
        val retryPolicy = DefaultRetryPolicy(
            socketTimeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST, url, params,
            {
                with(sharedPreferences.edit()) {
                    putString("UserStatus", "Leaving")
                    apply()
                }
                findNavController().navigate(R.id.action_parkingFragment_to_leavingFragment)             },
            { error ->
                val body = error.networkResponse?.data?.let { String(it, Charset.forName("UTF-8")) }
                val message = when (error?.networkResponse?.statusCode) {
                    409 -> {
                        body ?: "Conflict occurred"
                    }
                    404 -> {
                        body ?: "Not found"
                    }
                    401 -> {
                        body ?: "Not authorized"
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
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        jsonObjectRequest.retryPolicy = retryPolicy

        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }

    private fun reportBlocking() {
        val url = "http://10.0.2.2:5222/parking/block" // Replace with your actual URL

        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("UserId", null)
        val spotId = sharedPreferences.getString("CurrentSpot", null)
        val token = sharedPreferences.getString("UserToken", "")

        val params = JSONObject()
        params.put("userId", userId)
        params.put("spotId", spotId)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST, url, params,
            { _ -> },
            { error ->
                val body = error.networkResponse?.data?.let { String(it, Charset.forName("UTF-8")) }
                val message = when (error?.networkResponse?.statusCode) {
                    409 -> {
                        body ?: "Conflict occurred"
                    }
                    404 -> {
                        body ?: "Not found"
                    }
                    401 -> {
                        body ?: "Not authorized"
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
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)

        with(sharedPreferences.edit()) {
            putString("UserStatus", "Away")
            remove("CurrentSpot")
            apply()
        }
        findNavController().navigate(R.id.action_parkingFragment_to_profileFragment)
    }
}
