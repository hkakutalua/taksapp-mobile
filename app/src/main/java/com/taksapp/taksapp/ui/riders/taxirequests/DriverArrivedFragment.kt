package com.taksapp.taksapp.ui.riders.taxirequests


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.riders.taxirequests.viewmodels.TaxiRequestViewModel
import com.taksapp.taksapp.databinding.FragmentDriverArrivedBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.time.ExperimentalTime


@ExperimentalTime
class DriverArrivedFragment : Fragment() {
    private val viewModel: TaxiRequestViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDriverArrivedBinding>(
            inflater, R.layout.fragment_driver_arrived, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}
