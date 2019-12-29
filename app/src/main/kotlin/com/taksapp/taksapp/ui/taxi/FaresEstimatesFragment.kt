package com.taksapp.taksapp.ui.taxi


import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.FragmentFaresEstimatesBinding
import com.taksapp.taksapp.ui.taxi.adapters.CompaniesAdapter
import com.taksapp.taksapp.ui.taxi.viewmodels.TaxiRequestViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FaresEstimatesFragment : Fragment() {
    private val taxiRequestViewModel: TaxiRequestViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentFaresEstimatesBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_fares_estimates, container, false)
        binding.viewModel = taxiRequestViewModel

        val companiesAdapter = CompaniesAdapter()
        binding.recyclerViewCompanies.adapter = companiesAdapter
        binding.recyclerViewCompanies.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val companiesSelectionTracker = buildSelectionTracker(binding)
        companiesAdapter.setSelectionTracker(companiesSelectionTracker)

        taxiRequestViewModel.fareEstimationWithRoute.observeForever { fareEstimation ->
            companiesAdapter.updateCompanies(fareEstimation.fares)
        }

        requireActivity().onBackPressedDispatcher.addCallback {
            taxiRequestViewModel.clearDirections()
            Navigation.findNavController(context as Activity, R.id.fragment_navigation_host)
                .popBackStack()
        }

        return binding.root
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
