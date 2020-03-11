package com.taksapp.taksapp.ui.drivers.trips

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar

import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.trips.viewmodels.TripInProgressViewModel
import com.taksapp.taksapp.databinding.FragmentTripInProgressBinding
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TripInProgressFragment : Fragment() {
    private lateinit var tripInProgressViewModel: TripInProgressViewModel
    private val args: TripInProgressFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTripInProgressBinding>(
            inflater, R.layout.fragment_trip_in_progress, container, false
        )

        tripInProgressViewModel = getViewModel { parametersOf(args.trip) }

        binding.viewModel = tripInProgressViewModel
        binding.lifecycleOwner = this

        observeNavigationToFinished()
        observeSnackbarErrorEvent()

        return binding.root
    }

    private fun observeNavigationToFinished() {
        tripInProgressViewModel.navigateToFinished.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val trip = event.peekContent()

                trip?.let {
                    val navigateToTripFinishedAction =
                        TripInProgressFragmentDirections.toTripFinishedAction(trip)
                    requireView().findNavController().navigate(navigateToTripFinishedAction)
                }
            }
        )
    }

    private fun observeSnackbarErrorEvent() {
        tripInProgressViewModel.snackBarErrorEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val errorMessage = event.getContentIfNotHandled()

                errorMessage?.let {
                    val rootView = requireActivity().findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_LONG).show()
                }
            }
        )
    }

}
