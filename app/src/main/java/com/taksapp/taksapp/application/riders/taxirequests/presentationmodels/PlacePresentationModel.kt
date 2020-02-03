package com.taksapp.taksapp.application.riders.taxirequests.presentationmodels

import androidx.recyclerview.widget.DiffUtil
import java.io.Serializable

data class PlacePresentationModel(
    val primaryText: String,
    val secondaryText: String,
    val latitude: Double,
    val longitude: Double): Serializable {

    companion object {
        fun getDiffCallback(
            oldList: List<PlacePresentationModel>,
            newList: List<PlacePresentationModel>
        ) : PlaceDiffCallback =
            PlaceDiffCallback(
                oldList,
                newList
            )
    }

    class PlaceDiffCallback(
        private val oldList: List<PlacePresentationModel>,
        private val newList: List<PlacePresentationModel>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int)
                = areContentsTheSame(oldItemPosition, newItemPosition)

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem.primaryText == newItem.primaryText &&
                    oldItem.secondaryText == newItem.secondaryText &&
                    oldItem.latitude == newItem.latitude &&
                    oldItem.longitude == newItem.longitude
        }

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size
    }
}
