package com.taksapp.taksapp.ui.taxi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.taxirequest.viewmodels.TaxiRequestViewModel
import com.taksapp.taksapp.databinding.FragmentTaxiRequestAcceptanceWaitBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TaxiRequestAcceptanceWaitFragment : Fragment() {
    private val viewModel: TaxiRequestViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTaxiRequestAcceptanceWaitBinding>(
            inflater, R.layout.fragment_taxi_request_acceptance_wait, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}
