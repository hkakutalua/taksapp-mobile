package com.taksapp.taksapp.ui.taxi


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.FragmentLocationsSelectionBinding
import com.taksapp.taksapp.ui.taxi.presentationmodels.PlacePresentationModel
import com.taksapp.taksapp.ui.taxi.viewmodels.TaxiRequestViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LocationsSelectionFragment : Fragment() {
    companion object {
        private const val REQUEST_CODE_CHOOSE_START_LOCATION = 100
        private const val REQUEST_CODE_CHOOSE_DESTINATION_LOCATION = 200
    }

    private val taxiRequestViewModel: TaxiRequestViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentLocationsSelectionBinding>(
            inflater, R.layout.fragment_locations_selection, container, false
        )

        binding.viewModel = taxiRequestViewModel
        binding.lifecycleOwner = this

        binding.viewStartLocation.setOnClickListener {
            if (taxiRequestViewModel.estimatingFare.value == null ||
                taxiRequestViewModel.estimatingFare.value == false
            ) {
                val intent = Intent(context, AutocompletePlaceChooserActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_START_LOCATION)
            }
        }

        binding.viewDestinationLocation.setOnClickListener {
            if (taxiRequestViewModel.estimatingFare.value == null ||
                taxiRequestViewModel.estimatingFare.value == false
            ) {
                val intent = Intent(context, AutocompletePlaceChooserActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_DESTINATION_LOCATION)
            }
        }

        taxiRequestViewModel.errorEvent.observeForever { event ->
            val parentActivity = context as AppCompatActivity

            if (!event.hasBeenHandled) {
                Snackbar.make(
                    parentActivity.findViewById(android.R.id.content),
                    event.getContentIfNotHandled() ?: "",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }


        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.hasExtra(AutocompletePlaceChooserActivity.EXTRA_SELECTED_PLACE).let {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val place =
                    data?.getSerializableExtra(AutocompletePlaceChooserActivity.EXTRA_SELECTED_PLACE) as PlacePresentationModel

                when (requestCode) {
                    REQUEST_CODE_CHOOSE_START_LOCATION -> taxiRequestViewModel.changeStartLocation(
                        place
                    )
                    REQUEST_CODE_CHOOSE_DESTINATION_LOCATION -> taxiRequestViewModel.changeDestinationLocation(
                        place
                    )
                }
            }
        }
    }
}
