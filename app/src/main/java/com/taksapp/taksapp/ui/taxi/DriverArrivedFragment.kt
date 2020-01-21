package com.taksapp.taksapp.ui.taxi


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.FragmentDriverArrivingBinding


class DriverArrivedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDriverArrivingBinding>(
            inflater, R.layout.fragment_driver_arrived, container, false)
        return binding.root
    }
}
