package com.rutgers.smdr.logging

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TimberRemoteTree(firestore: FirebaseFirestore, private val deviceDetails: DeviceDetails) : Timber.DebugTree() {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a zzz", Locale.getDefault())
    private val date = dateFormat.format(Date(System.currentTimeMillis()))

    private val logRef = firestore.collection("logs/$date/${deviceDetails.deviceId}")

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val timestamp = System.currentTimeMillis()
        val time = timeFormat.format(Date(timestamp))
        val remoteLog = RemoteLog(priorityAsString(priority), tag, message, t.toString(), time)

        logRef
            .document("-DeviceDetails")
            .set(deviceDetails)
            .addOnCanceledListener { Log.e("TimberRemoteTree", "submitting log cancelled") }
            .addOnSuccessListener { Log.d("TimberRemoteTree", "log submitted succesfully") }
            .addOnFailureListener { Log.d("TimberRemoteTree", "log submitted failed") }
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Log.d("TimberRemoteTree", "log submitted succesfully")
                } else {
                    Log.e("TimberRemoteTree", "log could not be submitted", result.exception)
                }
            }

        logRef
            .document(timestamp.toString())
            .set(remoteLog)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Log.d("TimberRemoteTree","log submitted succesfully")
                } else {
                    Log.e("TimberRemoteTree","log could not be submitted", result.exception)
                }
            }

        super.log(priority, tag, message, t)
    }

    private fun priorityAsString(priority: Int): String = when (priority) {
        Log.VERBOSE -> "VERBOSE"
        Log.DEBUG -> "DEBUG"
        Log.INFO -> "INFO"
        Log.WARN -> "WARN"
        Log.ERROR -> "ERROR"
        Log.ASSERT -> "ASSERT"
        else -> priority.toString()
    }
}
