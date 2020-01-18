package com.taksapp.taksapp.data.infrastructure

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.iid.FirebaseInstanceId
import java.util.concurrent.ExecutionException
import com.taksapp.taksapp.arch.utils.Result

class FirebasePushNotificationTokenRetriever : PushNotificationTokenRetriever {
    companion object {
        val TAG = FirebasePushNotificationTokenRetriever::class.simpleName
    }

    override suspend fun getPushNotificationToken(): Result<String, String> {
        return try {
            val task = Tasks.await(FirebaseInstanceId.getInstance().instanceId)
            Result.success(task.token)
        } catch (e: ExecutionException) {
            Log.w(TAG, "Could not get push notification token", e)
            Result.error(e.localizedMessage)
        } catch (e: InterruptedException) {
            Log.w(TAG, "Could not get push notification token", e)
            Result.error(e.localizedMessage)
        }
    }

}