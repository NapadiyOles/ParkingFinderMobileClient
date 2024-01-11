package com.example.parkingfinder

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.parkingfinder.databinding.FragmentProfileBinding
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.nio.charset.Charset

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bnFavourites.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }

        binding.bnFind.setOnClickListener {
            suggestParkingLocation()
        }

        binding.bnLogout.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                remove("UserToken")
                remove("CurrentSpot")
                apply()
            }
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        updateGreeting()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun suggestParkingLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    createRequest(it.latitude, it.longitude)
                } ?: run {
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createRequest(latitude: Double, longitude: Double) {
        val url = "http://10.0.2.2:5222/parking/suggest" // Replace with your actual URL

        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("UserId", null)
        val token = sharedPreferences.getString("UserToken", "")

        val params = JSONObject()
        params.put("userId", userId)
        params.put("latitude", latitude)
        params.put("longitude", longitude)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.PUT, url, params,
            { response ->
                val currentSpot = response.getString("id")
                val suggestedLatitude = response.getDouble("latitude")
                val suggestedLongitude = response.getDouble("longitude")
                if(openMap(suggestedLatitude, suggestedLongitude)) {
                    with(sharedPreferences.edit()) {
                        putString("CurrentSpot", currentSpot)
                        putString("UserStatus", "Parking")
                        apply()
                    }
                    findNavController().navigate(R.id.action_profileFragment_to_parkingFragment)
                }                                                                  },
            { error ->
                val body = error.networkResponse?.data?.let { String(it, Charset.forName("UTF-8")) }
                val message = when (error?.networkResponse?.statusCode) {
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
                        if(body.isNullOrEmpty()) "Error ${error.networkResponse?.statusCode ?: ""} occurred" else body
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
    }

    private fun openMap(latitude: Double, longitude: Double) : Boolean {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Parking+Spot)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//        mapIntent.setPackage("com.google.android.apps.maps") // Optional: Ensure Google Maps app responds

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
            return true
        }

        Toast.makeText(requireContext(), "No map applications installed", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun updateGreeting() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("UserName", "User") ?: "User"
        binding.tvGreeting.text = getString(R.string.tv_greeting, userName)
    }
}