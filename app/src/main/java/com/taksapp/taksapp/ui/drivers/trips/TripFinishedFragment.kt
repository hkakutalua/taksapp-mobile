package com.taksapp.taksapp.ui.drivers.trips

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs

import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.trips.viewmodels.TripFinishedViewModel
import com.taksapp.taksapp.databinding.FragmentTripFinishedBinding
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class TripFinishedFragment : Fragment() {
    private lateinit var tripFinishedViewModel: TripFinishedViewModel
    private val args: TripFinishedFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTripFinishedBinding>(
            inflater, R.layout.fragment_arrived, container, false
        )

        tripFinishedViewModel = getViewModel { parametersOf(args.trip) }

        binding.viewModel = tripFinishedViewModel
        binding.lifecycleOwner = this

        binding.buttonConfirm.setOnClickListener {
            requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
        }

        return binding.root
    }

}
