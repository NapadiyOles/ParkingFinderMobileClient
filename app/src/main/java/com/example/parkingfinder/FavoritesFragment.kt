package com.example.parkingfinder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.parkingfinder.databinding.FragmentFavoritesBinding
import org.json.JSONObject
import java.nio.charset.Charset

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var favoritesAdapter: FavouritesListAdapter

    private var selectedLocation: FavoriteLocation? = null

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarFavorites.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_favoritesFragment_to_profileFragment)
        }

        binding.bnCheckAvailability.isEnabled = false
        binding.bnDeleteLocation.isEnabled = false

        binding.bnCheckAvailability.setOnClickListener {
            selectedLocation?.let { location ->
                checkFavouriteSpot(location.guid)
            }
        }

        binding.bnDeleteLocation.setOnClickListener {
            selectedLocation?.let { location ->
                showDeleteConfirmationDialog(location)
            }
        }

        loadFavorites()
        setupRecyclerView()
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

    private fun checkFavouriteSpot(spotId: String) {
        val url = "http://10.0.2.2:5222/parking/favourite" // Replace with your actual URL

        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("UserId", null)
        val token = sharedPreferences.getString("UserToken", "")

        val params = JSONObject()
        params.put("userId", userId)
        params.put("spotId", spotId)

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
                    findNavController().navigate(R.id.action_favoritesFragment_to_parkingFragment)
                }
            },
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

    private fun showDeleteConfirmationDialog(location: FavoriteLocation) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Location")
            .setMessage("Are you sure you want to delete this location: ${location.name}?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteLocation(location)
                loadAndUpdateFavorites()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun onLocationSelected(location: FavoriteLocation) {
        selectedLocation = location
        binding.bnCheckAvailability.isEnabled = true
        binding.bnDeleteLocation.isEnabled = true
    }

    private fun deleteLocation(location: FavoriteLocation) {
        val sharedPreferences = requireActivity().getSharedPreferences("Favourites", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(location.guid).apply()
    }

    private fun loadAndUpdateFavorites() {
        val newFavorites = loadFavorites()
        favoritesAdapter.updateData(newFavorites)
        binding.bnCheckAvailability.isEnabled = false
        binding.bnDeleteLocation.isEnabled = false
        selectedLocation = null
    }

    private fun loadFavorites(): List<FavoriteLocation> {
        val sharedPreferences = requireActivity().getSharedPreferences("Favourites", Context.MODE_PRIVATE)
        val favoritesMap = sharedPreferences.all
        return favoritesMap.map { entry ->
            FavoriteLocation(name = entry.value as String, guid = entry.key)
        }
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavouritesListAdapter(loadFavorites()) { location ->
            onLocationSelected(location)
        }
        binding.rvFavoriteLocations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoritesAdapter
        }
    }

}