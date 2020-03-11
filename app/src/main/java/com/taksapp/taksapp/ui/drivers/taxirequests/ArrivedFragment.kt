package com.taksapp.taksapp.ui.drivers.taxirequests


import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar

import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.taxirequests.viewmodels.ArrivedViewModel
import com.taksapp.taksapp.databinding.FragmentArrivedBinding
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ArrivedFragment : Fragment() {
    private lateinit var arrivedViewModel: ArrivedViewModel
    private val args: ArrivedFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentArrivedBinding>(
            inflater, R.layout.fragment_arrived, container, false
        )

        arrivedViewModel = getViewModel { parametersOf(args.taxiRequest) }

        binding.viewModel = arrivedViewModel
        binding.lifecycleOwner = this

        observeNavigateToMainWithErrorEvent()
        observeNavigateToMainEvent()
        observeNavigationToTripInProgress()
        observeSnackbarErrorEvent()

        return binding.root
    }

    private fun observeNavigateToMainWithErrorEvent() {
        arrivedViewModel.navigateToMainWithErrorEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val errorMessage = event.peekContent()

                errorMessage?.let {
                    fun navigateBack(): (DialogInterface, Int) -> Unit = { _, _ ->
                        requireView().findNavController().popBackStack(
                            R.id.driverMainFragment, false)
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error_title)
                        .setMessage(errorMessage)
                        .setNeutralButton(android.R.string.ok) { _, _->}
                        .setCancelable(false)
                        .show()
                }

                requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
            }
        )
    }

    private fun observeNavigationToTripInProgress() {
        arrivedViewModel.navigateToTripEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val trip = event.peekContent()

                trip?.let {
                    val navigateToTripInProgressAction =
                        ArrivedFragmentDirections.toTripInProgressAction(trip)
                    requireView().findNavController().navigate(navigateToTripInProgressAction)
                }
            }
        )
    }

    private fun observeNavigateToMainEvent() {
        arrivedViewModel.navigateToMain.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
            }
        )
    }

    private fun observeSnackbarErrorEvent() {
        arrivedViewModel.snackBarErrorEvent.observe(
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
