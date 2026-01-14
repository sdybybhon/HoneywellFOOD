package com.example.honeywellfood.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honeywellfood.R
import com.example.honeywellfood.presentation.adapter.HistoryAdapter
import com.example.honeywellfood.presentation.viewmodel.ScanViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerCategoryFilter: Spinner
    private lateinit var adapter: HistoryAdapter

    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvHistory)
        spinnerCategoryFilter = view.findViewById(R.id.spinnerCategoryFilter)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = HistoryAdapter { scanItem ->
            showDeleteItemDialog(scanItem)
        }

        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.btnClearHistory).setOnClickListener {
            showClearHistoryDialog()
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupCategorySpinner()
        observeScans()
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Все") + com.example.honeywellfood.domain.model.ProductCategory.allCategories

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_dark))

                val density = resources.displayMetrics.density
                val padding = (12 * density).toInt()
                textView.setPadding(padding, padding, padding, padding)

                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoryFilter.adapter = adapter
        spinnerCategoryFilter.setPopupBackgroundResource(R.color.background_dark)

        spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = categories[position]
                selectedCategory = if (selected == "Все") null else selected
                observeScans()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
                observeScans()
            }
        }
    }

    private fun observeScans() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scanHistory.collect { scans ->
                val filteredScans = if (selectedCategory != null) {
                    scans.filter { it.category == selectedCategory }
                } else {
                    scans
                }
                adapter.submitList(filteredScans)
            }
        }
    }

    private fun showClearHistoryDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Очистить историю")
            .setMessage("Вы уверены, что хотите удалить всю историю сканирования?")
            .setPositiveButton("Очистить") { _, _ ->
                viewModel.clearHistory()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteItemDialog(scanItem: com.example.honeywellfood.domain.model.ScanItem) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Удалить продукт")
            .setMessage("Удалить \"${scanItem.productName ?: "Неизвестный продукт"}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteScan(scanItem)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}