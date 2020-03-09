package com.taksapp.taksapp.ui.riders.taxirequests.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.riders.taxirequests.presentationmodels.PlacePresentationModel

class AutocompletePlaceAdapter : RecyclerView.Adapter<AutocompletePlaceAdapter.PlaceViewHolder>() {
    private var places = listOf<PlacePresentationModel>()
    private var onSelectedPlace: (place: PlacePresentationModel) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_place_suggestion, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun getItemCount() = places.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.placeNameTextView.text = place.primaryText
        holder.placeAddressTextView.text = place.secondaryText
    }

    fun listenForSelectedPlace(onSelectedPlace: (place: PlacePresentationModel) -> Unit) {
        this.onSelectedPlace = onSelectedPlace
    }

    fun updatePlaces(newPlaces: List<PlacePresentationModel>) {
        val diffResult = DiffUtil.calculateDiff(PlacePresentationModel.getDiffCallback(this.places, newPlaces))
        this.places = newPlaces
        diffResult.dispatchUpdatesTo(this)
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeNameTextView: TextView = itemView.findViewById(R.id.text_place_name)
        val placeAddressTextView: TextView = itemView.findViewById(R.id.text_place_address)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != -1) {
                    onSelectedPlace(places[adapterPosition])
                }
            }
        }
    }
}