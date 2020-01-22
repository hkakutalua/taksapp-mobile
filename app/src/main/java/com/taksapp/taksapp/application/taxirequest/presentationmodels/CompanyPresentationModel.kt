package com.taksapp.taksapp.application.taxirequest.presentationmodels

import androidx.recyclerview.widget.DiffUtil

data class CompanyPresentationModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val fare: String
) {

    companion object {
        fun getDiffCallback(
            oldList: List<CompanyPresentationModel>,
            newList: List<CompanyPresentationModel>
        ): CompanyDiffCallback = CompanyDiffCallback(oldList, newList)
    }

    class CompanyDiffCallback(
        private val oldList: List<CompanyPresentationModel>,
        private val newList: List<CompanyPresentationModel>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem.fare == newItem.fare &&
                    oldItem.imageUrl == newItem.imageUrl &&
                    oldItem.name == newItem.name
        }

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CompanyPresentationModel)
            return false

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}