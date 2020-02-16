package com.taksapp.taksapp.ui.drivers.taxirequests


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.taxirequests.viewmodels.IncomingTaxiRequestViewModel
import com.taksapp.taksapp.databinding.FragmentIncomingTaxiRequestBinding
import com.taksapp.taksapp.ui.utils.CountdownCreator
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime


@ExperimentalTime
class IncomingTaxiRequestFragment : Fragment() {
    companion object {
        private const val TAXI_REQUEST_COUNTDOWN_ID = "TAXI_REQUEST_COUNTDOWN"
    }

    private lateinit var incomingTaxiRequestViewModel: IncomingTaxiRequestViewModel
    private val args: IncomingTaxiRequestFragmentArgs by navArgs()
    private val countdownCreator = CountdownCreator()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentIncomingTaxiRequestBinding>(
            inflater, R.layout.fragment_incoming_taxi_request, container, false)

        incomingTaxiRequestViewModel = getViewModel { parametersOf(args.taxiRequest) }

        binding.viewModel = incomingTaxiRequestViewModel
        binding.lifecycleOwner = this

        binding.buttonRefuse.setOnClickListener {
            requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
        }

        observeNavigationToMain()
        observeDenyCountdownCycleOfTaxiRequest(binding)
        observeNavigationToTaxiRequest()
        observeNavigationToMainWithError()
        observeSnackBarError()

        blockBackNavigation()

        return binding.root
    }

    private fun blockBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { }
    }

    private fun observeNavigationToMain() {
        incomingTaxiRequestViewModel.navigateToMainScreen.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
            })
    }

    private fun observeDenyCountdownCycleOfTaxiRequest(binding: FragmentIncomingTaxiRequestBinding) {

        incomingTaxiRequestViewModel.startTaxiRequestSecondsCountdownEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val countDownTimeInSeconds =
                    event.getContentIfNotHandled() ?: return@Observer

                countdownCreator.createCountdown(
                    TAXI_REQUEST_COUNTDOWN_ID,
                    countDownTimeInSeconds
                ) { remainingSeconds ->
                    binding.textviewCounter.text = remainingSeconds.toString()
                }
            })

        incomingTaxiRequestViewModel.pauseTaxiRequestCountdownEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                countdownCreator.pauseCountdown(TAXI_REQUEST_COUNTDOWN_ID)
            }
        )

        incomingTaxiRequestViewModel.resumeTaxiRequestCountdownEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                countdownCreator.resumeCountdown(TAXI_REQUEST_COUNTDOWN_ID)
            }
        )
    }

    private fun observeNavigationToTaxiRequest() {
        incomingTaxiRequestViewModel.navigateToTaxiRequestEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val taxiRequest = event.peekContent()

                taxiRequest?.let {
                    val navigateToArrivingRiderAction =
                        IncomingTaxiRequestFragmentDirections.toArrivingToRiderAction(taxiRequest)
                    requireView().findNavController().navigate(navigateToArrivingRiderAction)
                }
            }
        )
    }

    private fun observeNavigationToMainWithError() {
        incomingTaxiRequestViewModel.navigateToMainScreenWithErrorEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val errorMessage = event.peekContent()

                errorMessage?.let {
                    fun navigateBack(): (DialogInterface, Int) -> Unit = { _, _ ->
                        requireView().findNavController().popBackStack(R.id.driverMainFragment, false)
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error_title)
                        .setMessage(errorMessage)
                        .setNeutralButton(android.R.string.ok, navigateBack())
                        .setCancelable(false)
                        .show()
                }
            }
        )
    }

    private fun observeSnackBarError() {
        incomingTaxiRequestViewModel.snackBarErrorEvent.observe(
            this, Observer { event ->
                if (event.hasBeenHandled)
                    return@Observer

                val errorMessage = event.peekContent()

                errorMessage?.let {
                    val rootView = requireActivity().findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_LONG)
                }
            }
        )
    }
}
