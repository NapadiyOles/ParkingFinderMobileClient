package com.example.parkingfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class FavoriteLocation(val name: String, val guid: String)

class FavouritesListAdapter(
    private var locations: List<FavoriteLocation>,
    private val onItemSelected: (FavoriteLocation) -> Unit
) : RecyclerView.Adapter<FavouritesListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocationName: TextView = view.findViewById(R.id.tv_location_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_location, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]
        holder.tvLocationName.text = location.name
        holder.itemView.setOnClickListener {
            onItemSelected(location)
        }
    }

    override fun getItemCount(): Int = locations.size

    // Function to update the data in the adapter
    fun updateData(newLocations: List<FavoriteLocation>) {
        locations = newLocations
        notifyDataSetChanged()
    }
}
