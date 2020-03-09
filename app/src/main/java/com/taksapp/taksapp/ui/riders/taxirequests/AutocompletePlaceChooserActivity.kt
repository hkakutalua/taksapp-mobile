package com.taksapp.taksapp.ui.riders.taxirequests

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.ActivityAutocompletePlaceChooserBinding
import com.taksapp.taksapp.ui.riders.taxirequests.adapters.AutocompletePlaceAdapter
import com.taksapp.taksapp.application.riders.taxirequests.viewmodels.AutocompletePlaceChooserViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
class AutocompletePlaceChooserActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SELECTED_PLACE = "EXTRA_SELECTED_PLACE"
    }

    private val placeChooserViewModel: AutocompletePlaceChooserViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAutocompletePlaceChooserBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_autocomplete_place_chooser)
        binding.viewModel = placeChooserViewModel
        binding.lifecycleOwner = this

        val autocompletePlaceAdapter =
            AutocompletePlaceAdapter()
        binding.recyclerViewSuggestions.adapter = autocompletePlaceAdapter
        binding.recyclerViewSuggestions.layoutManager = LinearLayoutManager(this)

        autocompletePlaceAdapter.listenForSelectedPlace { place ->
            val intent = Intent()
            intent.putExtra(EXTRA_SELECTED_PLACE, place)
            setResult(RESULT_OK, intent)
            finish()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        placeChooserViewModel.places.observe(this, Observer { places ->
            autocompletePlaceAdapter.updatePlaces(places)
        })

        placeChooserViewModel.error.observe(this, Observer { errorEvent ->
            if (!errorEvent.hasBeenHandled) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    errorEvent.getContentIfNotHandled() ?: "",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }
}
