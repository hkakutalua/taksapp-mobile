package com.taksapp.taksapp.ui.drivers.taxirequests


import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.taksapp.taksapp.R


class IncomingRequestDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setView(R.layout.fragment_incoming_request_dialog)
                .setPositiveButton(R.string.text_accept) { _, _ -> }
                .setNegativeButton(R.string.text_refuse) { _, _ -> }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
