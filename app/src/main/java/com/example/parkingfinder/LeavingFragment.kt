package com.example.parkingfinder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.parkingfinder.databinding.FragmentSubmitLeavingBinding
import org.json.JSONObject
import java.nio.charset.Charset

class LeavingFragment : Fragment() {

    private var _binding: FragmentSubmitLeavingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSubmitLeavingBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bnReportLeave.setOnClickListener {
            reportLeaving()
        }

        binding.bnAddFavourite.setOnClickListener {
            val userData = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            val spot = userData.getString("CurrentSpot", null)!!
            val favourites = requireActivity().getSharedPreferences("Favourites", Context.MODE_PRIVATE)
            if(favourites.contains(spot)) {
                Toast.makeText(requireContext(), "Current spot is already saved", Toast.LENGTH_SHORT).show()
            } else {
                val editText = EditText(requireContext())

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val margin = resources.getDimensionPixelSize(R.dimen.edit_margin)
                layoutParams.setMargins(margin, 0, margin, 0)
                editText.layoutParams = layoutParams

                AlertDialog.Builder(requireContext())
                    .setTitle("Add to favorites")
                    .setMessage("Enter a name for this location:")
                    .setView(editText)
                    .setPositiveButton("Save") { dialog, which ->
                        val name = editText.text.toString()
                        if (name.isNotEmpty()) {
                            saveFavoriteLocation(name, spot, favourites)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun saveFavoriteLocation(name: String, spot: String, sharedPreferences: SharedPreferences) {
        with(sharedPreferences.edit()) {
            putString(spot, name) // Using spot ID as key and name as value
            apply()
        }
        Toast.makeText(requireContext(), "Location saved as favorite", Toast.LENGTH_SHORT).show()
    }


    private fun reportLeaving() {
        val url = "http://10.0.2.2:5222/parking/leave" // Replace with your actual URL

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
        findNavController().navigate(R.id.action_leavingFragment_to_profileFragment)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
