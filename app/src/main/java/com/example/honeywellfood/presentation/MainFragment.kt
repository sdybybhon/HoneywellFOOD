package com.example.honeywellfood.presentation

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.lifecycle.Observer
import com.example.honeywellfood.R
import com.example.honeywellfood.presentation.viewmodel.ScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: ScanViewModel by viewModels()
    private lateinit var btnToggleScan: Button
    private lateinit var btnHistory: Button
    private lateinit var tvScanResult: TextView

    private lateinit var btnStatistics: Button
    private var productDialog: ProductInfoDialogFragment? = null
    private var lastScannedCodeId: String = ""

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ScanReceiver.ACTION_LOCAL_BARCODE_DATA) {
                var data = intent.getStringExtra(ScanReceiver.EXTRA_DATA) ?: ""
                val codeId = intent.getStringExtra(ScanReceiver.EXTRA_CODE_ID) ?: ""

                data = data.trim()

                if (codeId == "d" || codeId == "c") {
                    data = data.replace("[^\\d]".toRegex(), "")

                    if (codeId == "d" && data.length == 12) {
                        Log.d("MainFragment", "EAN13 with 12 digits detected, calculating checksum")
                        val checksum = calculateEAN13Checksum(data)
                        data += checksum
                        Log.d("MainFragment", "Added checksum digit: $checksum, full barcode: $data")
                    }
                }

                if (data.length < 8) {
                    tvScanResult.text = "Ошибка: слишком короткий штрихкод: '$data'"
                    return
                }

                Log.d("MainFragment", "Processed barcode: '$data' (length: ${data.length})")

                val symbology = getSymbologyName(codeId)

                lastScannedCodeId = codeId

                tvScanResult.text = buildString {
                    append("Штрихкод отсканирован!\n\n")
                    append("Штрихкод: $data\n")
                    append("Длина: ${data.length}\n")
                    append("Тип: $symbology\n")
                    append("🔍 Ищем информацию о продукте...")
                }

                viewModel.onBarcodeScanned(data, symbology)

                if (viewModel.isScanning.value == true) {
                    btnToggleScan.postDelayed({
                        startScanning()
                    }, 1500)
                }
            }
        }
    }

    private fun calculateEAN13Checksum(barcode12: String): Char {
        if (barcode12.length != 12) {
            throw IllegalArgumentException("EAN13 barcode must be 12 digits without checksum")
        }

        var sum = 0
        for (i in barcode12.indices) {
            val digit = barcode12[i].digitToInt()
            sum += if (i % 2 == 0) digit * 1 else digit * 3
        }

        val remainder = sum % 10
        val checksum = if (remainder == 0) 0 else 10 - remainder

        return checksum.digitToChar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        btnToggleScan = view.findViewById(R.id.btnToggleScan)
        btnHistory = view.findViewById(R.id.btnHistory)
        btnStatistics = view.findViewById(R.id.btnStatistics)
        tvScanResult = view.findViewById(R.id.tvScanResult)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isScanning.observe(viewLifecycleOwner, Observer { isScanning ->
            btnToggleScan.text = if (isScanning) "Остановить сканирование" else "Начать сканирование"
            if (isScanning) {
                claimScanner()
                startScanning()
            } else {
                stopScanning()
                releaseScanner()
            }
        })

        viewModel.showProductDialog.observe(viewLifecycleOwner, Observer { dialogData ->
            dialogData?.let { (barcode, productName, symbology) ->
                showProductInfoDialog(barcode, productName, symbology)
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

        btnStatistics.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, StatisticsFragment())
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
        if (viewModel.isScanning.value == true) {
            stopScanning()
            releaseScanner()
        }
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

    private fun startScanning() {
        val intent = Intent(ACTION_CONTROL_SCANNER).apply {
            putExtra(EXTRA_SCAN, true)
        }
        requireContext().sendBroadcast(intent)
    }

    private fun stopScanning() {
        val intent = Intent(ACTION_CONTROL_SCANNER).apply {
            putExtra(EXTRA_SCAN, false)
        }
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

    private fun showProductInfoDialog(barcode: String, productName: String?, symbology: String) {
        productDialog?.dismiss()

        productDialog = ProductInfoDialogFragment.newInstance(barcode, productName).apply {
            setListener(object : ProductInfoDialogFragment.OnDialogActionListener {
                override fun onProductSaved(
                    productName: String,
                    category: String?,
                    expiryDate: Long,
                    barcode: String
                ) {
                    viewModel.addProductWithInfo(barcode, productName, category, expiryDate, symbology)

                    val remainingDays = calculateRemainingDays(expiryDate)
                    val remainingText = when {
                        remainingDays < 0 -> " (просрочено)"
                        remainingDays == 0 -> " (истекает сегодня)"
                        remainingDays == 1 -> " (ост. 1 д.)"
                        remainingDays <= 30 -> " (ост. $remainingDays д.)"
                        else -> ""
                    }

                    tvScanResult.text = buildString {
                        append("Продукт сохранен!\n\n")
                        append("Название: $productName\n")
                        if (category != null) {
                            append("Категория: $category\n")
                        }
                        append("Штрихкод: $barcode\n")
                        append("Годен до: ${formatDate(expiryDate)}$remainingText\n")
                        append("Тип: $symbology")
                    }

                    Toast.makeText(
                        requireContext(),
                        "Продукт добавлен в историю",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDialogCanceled() {
                    tvScanResult.text = buildString {
                        append("Добавление продукта отменено =(\n\n")
                        append("Штрихкод: $barcode\n")
                        append("Длина: ${barcode.length}\n")
                        append("Тип: $symbology")
                    }
                }
            })
        }

        productDialog?.show(childFragmentManager, "ProductInfoDialog")
    }

    private fun calculateRemainingDays(expiryDate: Long): Int {
        val diff = expiryDate - Date().time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    companion object {
        private const val ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER"
        private const val ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER"
        private const val ACTION_CONTROL_SCANNER = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER"
        private const val EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER"
        private const val EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE"
        private const val EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES"
        private const val EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN"
    }
}