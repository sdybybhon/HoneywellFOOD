package com.example.honeywellfood.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honeywellfood.R
import com.example.honeywellfood.presentation.adapter.HistoryAdapter
import com.example.honeywellfood.presentation.viewmodel.ScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter()
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.btnClearHistory).setOnClickListener {
            showClearHistoryDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scanHistory.collect { scans ->
                adapter.submitList(scans)
            }
        }
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистить историю")
            .setMessage("Вы уверены, что хотите удалить всю историю сканирования?")
            .setPositiveButton("Очистить") { _, _ ->
                viewModel.clearHistory()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}