package com.taksapp.taksapp.ui.taxi


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.taxirequest.viewmodels.TaxiRequestViewModel
import com.taksapp.taksapp.databinding.FragmentDriverArrivingBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.time.ExperimentalTime


@ExperimentalTime
class DriverArrivingFragment : Fragment() {
    private val viewModel: TaxiRequestViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDriverArrivingBinding>(
            inflater, R.layout.fragment_driver_arriving, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

}
