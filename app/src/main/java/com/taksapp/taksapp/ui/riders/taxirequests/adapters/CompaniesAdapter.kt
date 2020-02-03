package com.taksapp.taksapp.ui.riders.taxirequests.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.riders.taxirequests.presentationmodels.CompanyPresentationModel
import de.hdodenhof.circleimageview.CircleImageView

class CompaniesAdapter : RecyclerView.Adapter<CompaniesAdapter.CompanyViewHolder>() {
    private var companies = listOf<CompanyPresentationModel>()
    private var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_company_fare, parent, false)
        return CompanyViewHolder(view)
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = companies.size

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]

        Picasso.get()
            .load(company.imageUrl)
            .into(holder.logoImageView)
        holder.nameTextView.text = company.name
        holder.fareTextView.text = company.fare

        selectionTracker?.let {
            holder.isSelected = it.isSelected(position.toLong())
        }
    }

    fun updateCompanies(newCompanies: List<CompanyPresentationModel>) {
        val diffResult =
            DiffUtil.calculateDiff(
                CompanyPresentationModel.getDiffCallback(
                    companies,
                    newCompanies
                )
            )
        diffResult.dispatchUpdatesTo(this)
        this.companies = newCompanies
    }

    fun setSelectionTracker(selectionTracker: SelectionTracker<Long>) {
        this.selectionTracker = selectionTracker
    }

    inner class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logoImageView: CircleImageView = itemView.findViewById(R.id.image_company)
        val nameTextView: TextView = itemView.findViewById(R.id.text_company_name)
        val fareTextView: TextView = itemView.findViewById(R.id.text_company_fare)
        private val checkedImageView = itemView.findViewById<AppCompatImageView>(R.id.imageview_checked)
        private val opaqueImageView = itemView.findViewById<AppCompatImageView>(R.id.imageview_opaque)

        var isSelected
            get() = itemView.isActivated
            set(isActivated) {
                itemView.isActivated = isActivated

                if (isActivated) {
                    checkedImageView.visibility = View.VISIBLE
                    opaqueImageView.visibility = View.VISIBLE
                } else {
                    checkedImageView.visibility = View.GONE
                    opaqueImageView.visibility = View.GONE
                }
            }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getSelectionKey() = itemId
                override fun getPosition() = adapterPosition
            }
    }

    class CompanyDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)

            if (view != null) {
                val viewHolder = recyclerView.getChildViewHolder(view) as CompanyViewHolder
                return viewHolder.getItemDetails()
            }

            return null
        }
    }
}