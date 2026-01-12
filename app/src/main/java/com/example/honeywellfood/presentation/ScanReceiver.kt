package com.example.honeywellfood.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ScanReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_LOCAL_BARCODE_DATA = "com.example.honeywellfood.ACTION_LOCAL_BARCODE_DATA"
        const val EXTRA_DATA = "data"
        const val EXTRA_CODE_ID = "codeId"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val data = intent?.getStringExtra("data") ?: ""
        val codeId = intent?.getStringExtra("codeId") ?: ""

        val localIntent = Intent(ACTION_LOCAL_BARCODE_DATA).apply {
            putExtra(EXTRA_DATA, data)
            putExtra(EXTRA_CODE_ID, codeId)
        }

        context?.let {
            LocalBroadcastManager.getInstance(it).sendBroadcast(localIntent)
        }
    }
}