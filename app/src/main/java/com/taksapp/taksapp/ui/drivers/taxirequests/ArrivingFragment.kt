package com.taksapp.taksapp.ui.drivers.taxirequests


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.taxirequests.viewmodels.ArrivingViewModel
import com.taksapp.taksapp.databinding.FragmentArrivingBinding
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class ArrivingFragment : Fragment() {
    private lateinit var arrivingViewModel: ArrivingViewModel
    private val args: ArrivingFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentArrivingBinding>(
            inflater, R.layout.fragment_arriving, container, false
        )

        arrivingViewModel = getViewModel { parametersOf(args.taxiRequest) }

        binding.viewModel = arrivingViewModel
        binding.lifecycleOwner = this

        observeNavigateToArrivedEvent()
        observeSnackbarErrorEvent()
        observeNavigateToMainEvent()
        observeNavigateToMainWithErrorEvent()

        return binding.root
    }

    private fun observeNavigateToMainWithErrorEvent() {
        arrivingViewModel.navigateToMainWithErrorEvent.observe(
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
                        .setNeutralButton(android.R.string.ok, navigateBack())
                        .setCancelable(false)
                        .show()
                }

                requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
            }
        )
    }

    private fun observeNavigateToMainEvent() {
        arrivingViewModel.navigateToMain.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
            }
        )
    }

    private fun observeSnackbarErrorEvent() {
        arrivingViewModel.snackBarErrorEvent.observe(
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

    private fun observeNavigateToArrivedEvent() {
        arrivingViewModel.navigateToArrivedEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val taxiRequest = event.peekContent()

                taxiRequest?.let {
                    val navigateToArrivedDriverAction =
                        ArrivingFragmentDirections.toArrivedToRiderAction(taxiRequest)
                    requireView().findNavController().navigate(navigateToArrivedDriverAction)
                }
            }
        )
    }
}
