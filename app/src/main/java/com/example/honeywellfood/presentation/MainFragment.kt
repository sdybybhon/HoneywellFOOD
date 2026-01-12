package com.example.honeywellfood.presentation

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.lifecycle.Observer
import com.example.honeywellfood.R
import com.example.honeywellfood.presentation.viewmodel.ScanViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: ScanViewModel by viewModels()
    private lateinit var btnToggleScan: Button
    private lateinit var btnHistory: Button
    private lateinit var tvScanResult: TextView

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ScanReceiver.ACTION_LOCAL_BARCODE_DATA) {
                val data = intent.getStringExtra(ScanReceiver.EXTRA_DATA) ?: ""
                val codeId = intent.getStringExtra(ScanReceiver.EXTRA_CODE_ID) ?: ""
                val symbology = getSymbologyName(codeId)

                tvScanResult.text = "Скан: $data\nТип: $symbology"

                viewModel.addScan(data, symbology)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        btnToggleScan = view.findViewById(R.id.btnToggleScan)
        btnHistory = view.findViewById(R.id.btnHistory)
        tvScanResult = view.findViewById(R.id.tvScanResult)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isScanning.observe(viewLifecycleOwner, Observer { isScanning ->
            btnToggleScan.text = if (isScanning) "Остановить сканирование" else "Начать сканирование"
            if (isScanning) {
                claimScanner()
            } else {
                releaseScanner()
            }
        })

        btnToggleScan.setOnClickListener {
            viewModel.toggleScanning()
        }

        btnHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, HistoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(localReceiver, IntentFilter(ScanReceiver.ACTION_LOCAL_BARCODE_DATA))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(localReceiver)
        releaseScanner()
    }

    private fun claimScanner() {
        val intent = Intent(ACTION_CLAIM_SCANNER).apply {
            putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
            putExtra(EXTRA_PROFILE, "DEFAULT")
            val bundle = Bundle().apply {
                putBoolean("DPR_DATA_INTENT", true)
                putString("DPR_DATA_INTENT_ACTION", "com.example.honeywellfood.ACTION_BARCODE_DATA")
            }
            putExtra(EXTRA_PROPERTIES, bundle)
        }
        requireContext().sendBroadcast(intent)
    }

    private fun releaseScanner() {
        val intent = Intent(ACTION_RELEASE_SCANNER)
        requireContext().sendBroadcast(intent)
    }

    private fun getSymbologyName(codeId: String): String {
        return when (codeId) {
            "s" -> "QR Code"
            "j" -> "Code 128"
            "d" -> "EAN13"
            "c" -> "UPCA"
            "b" -> "Code 39"
            "r" -> "PDF417"
            "w" -> "DataMatrix"
            "z" -> "Aztec"
            else -> "Unknown ($codeId)"
        }
    }

    companion object {
        private const val ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER"
        private const val ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER"
        private const val EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER"
        private const val EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE"
        private const val EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES"
    }
}