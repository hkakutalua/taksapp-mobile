package com.taksapp.taksapp.ui.taxi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.FragmentTaxiRequestAcceptanceWaitBinding

class TaxiRequestAcceptanceWaitFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTaxiRequestAcceptanceWaitBinding>(
            inflater, R.layout.fragment_taxi_request_acceptance_wait, container, false)
        return binding.root
    }
}
