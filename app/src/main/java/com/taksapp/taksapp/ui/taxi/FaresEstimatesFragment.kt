package com.taksapp.taksapp.ui.taxi


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.FragmentFaresEstimatesBinding
import com.taksapp.taksapp.ui.taxi.adapters.CompaniesAdapter
import com.taksapp.taksapp.application.taxirequest.viewmodels.FareEstimationViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FaresEstimatesFragment : Fragment() {
    companion object {
        const val TAXI_REQUEST_ACTIVITY_CODE = 100
    }

    private val fareEstimationViewModel: FareEstimationViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentFaresEstimatesBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_fares_estimates, container, false)
        binding.viewModel = fareEstimationViewModel
        binding.lifecycleOwner = this

        val companiesAdapter = setUpCompaniesRecyclerView(binding)

        fareEstimationViewModel.fareEstimationWithRoute
            .observeForever { fareEstimation ->
                companiesAdapter.updateCompanies(fareEstimation.fares)
            }

        fareEstimationViewModel.errorEvent.observe(this, Observer { event ->
            val parentActivity = context as AppCompatActivity

            if (!event.hasBeenHandled) {
                Snackbar.make(
                    parentActivity.findViewById(android.R.id.content),
                    event.getContentIfNotHandled() ?: "",
                    Snackbar.LENGTH_LONG
                ).show()
            }

        })

        fareEstimationViewModel.navigateToTaxiRequestEvent.observe(this,
            Observer { eventWithTaxiRequest ->
                if (eventWithTaxiRequest.hasBeenHandled)
                    return@Observer

                val intent = Intent(context, TaxiRequestActivity::class.java)
                intent.putExtra(
                    TaxiRequestActivity.EXTRA_TAXI_REQUEST,
                    eventWithTaxiRequest.getContentIfNotHandled()
                )
                startActivityForResult(intent, TAXI_REQUEST_ACTIVITY_CODE)
            })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity()) {
            if (fareEstimationViewModel.sendingTaxiRequest.value == true)
                return@addCallback

            fareEstimationViewModel.clearDirections()
            Navigation.findNavController(context as Activity, R.id.fragment_navigation_host)
                .popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        if (requestCode == TAXI_REQUEST_ACTIVITY_CODE) {
            val errorKind = data?.getStringExtra(TaxiRequestActivity.EXTRA_ERROR_KIND)

            if (errorKind == TaxiRequestActivity.ERROR_KIND_TAXI_REQUEST_TIMEOUT) {
                val alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.text_taxi_request_finished)
                    .setMessage(R.string.text_no_drivers_answered_to_request)
                    .setIcon(R.drawable.ic_logo)
                    .setNeutralButton(android.R.string.ok) { _,_ -> }
                    .create()
                alertDialog.show()
            }

            if (errorKind == TaxiRequestActivity.ERROR_KIND_TAXI_REQUEST_CANCELLED) {
                val alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.text_taxi_request_finished)
                    .setMessage(R.string.text_taxi_request_cancelled_by_driver)
                    .setIcon(R.drawable.ic_logo)
                    .setNeutralButton(android.R.string.ok) { _,_ -> }
                    .create()
                alertDialog.show()
            }
        }
    }

    private fun setUpCompaniesRecyclerView(binding: FragmentFaresEstimatesBinding): CompaniesAdapter {
        val companiesAdapter = CompaniesAdapter()
        binding.recyclerViewCompanies.adapter = companiesAdapter
        binding.recyclerViewCompanies.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val companiesSelectionTracker = buildSelectionTracker(binding)
        companiesAdapter.setSelectionTracker(companiesSelectionTracker)
        return companiesAdapter
    }

    private fun buildSelectionTracker(binding: FragmentFaresEstimatesBinding): SelectionTracker<Long> {
        val companiesSelectionTracker = SelectionTracker.Builder<Long>(
            "companies-tracker",
            binding.recyclerViewCompanies,
            StableIdKeyProvider(binding.recyclerViewCompanies),
            CompaniesAdapter.CompanyDetailsLookup(binding.recyclerViewCompanies),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
            .build()
        // Workaround to disable long-pressing to select items.
        // See: https://stackoverflow.com/questions/55494599/how-to-select-first-item-without-long-press-using-recyclerviews-selectiontracke
        companiesSelectionTracker.select(-1)
        return companiesSelectionTracker
    }
}
