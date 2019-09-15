package com.taksapp.taksapp.data.infrastructure

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.taksapp.taksapp.data.infrastructure.PushNotificationTokenRetriever.OnCompleteListener

class FirebasePushNotificationTokenRetriever : PushNotificationTokenRetriever {
    companion object {
        val TAG = FirebasePushNotificationTokenRetriever::class.simpleName
    }

    override fun getPushNotificationToken(listener: (Result<String>) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener(Result.success(task.result!!.token))
            } else {
                Log.w(TAG, "Could not get push notification token", task.exception)
                listener(Result.failure(task.exception!!))
            }
        }
    }

}