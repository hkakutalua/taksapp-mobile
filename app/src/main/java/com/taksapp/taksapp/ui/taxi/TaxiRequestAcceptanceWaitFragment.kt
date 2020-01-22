package com.taksapp.taksapp.ui.taxi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.FragmentTaxiRequestAcceptanceWaitBinding
import com.taksapp.taksapp.application.taxirequest.viewmodels.TaxiRequestViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class TaxiRequestAcceptanceWaitFragment : Fragment() {
    private val viewModel: TaxiRequestViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.showTimeoutMessageAndNavigateBackEvent.observe(
            this, Observer {
                val intent = Intent(requireContext(), RiderMainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra(
                    TaxiRequestActivity.EXTRA_ERROR_KIND,
                    TaxiRequestActivity.ERROR_KIND_TAXI_REQUEST_TIMEOUT)
                requireActivity().setResult(Activity.RESULT_OK, intent)
                requireActivity().finish()
            })

        val binding = DataBindingUtil.inflate<FragmentTaxiRequestAcceptanceWaitBinding>(
            inflater, R.layout.fragment_taxi_request_acceptance_wait, container, false)
        return binding.root
    }
}
