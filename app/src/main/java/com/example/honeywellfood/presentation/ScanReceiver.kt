package com.example.honeywellfood.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ScanReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_LOCAL_BARCODE_DATA = "com.example.honeywellfood.ACTION_LOCAL_BARCODE_DATA"
        const val EXTRA_DATA = "data"
        const val EXTRA_CODE_ID = "codeId"
        private const val TAG = "ScanReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Received broadcast: ${intent?.action}")
        Log.d(TAG, "Intent extras: ${intent?.extras}")

        val data = intent?.getStringExtra("data") ?: ""
        val codeId = intent?.getStringExtra("codeId") ?: ""

        Log.d(TAG, "Raw barcode data: '$data' (length: ${data.length})")
        Log.d(TAG, "Code ID: '$codeId'")

        val localIntent = Intent(ACTION_LOCAL_BARCODE_DATA).apply {
            putExtra(EXTRA_DATA, data)
            putExtra(EXTRA_CODE_ID, codeId)
        }

        context?.let {
            LocalBroadcastManager.getInstance(it).sendBroadcast(localIntent)
        }
    }
}