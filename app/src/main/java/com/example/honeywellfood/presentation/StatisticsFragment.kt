package com.example.honeywellfood.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.honeywellfood.R
import com.example.honeywellfood.databinding.FragmentStatisticsBinding
import com.example.honeywellfood.domain.model.ExpiryDistribution
import com.example.honeywellfood.domain.model.StatisticsData
import com.example.honeywellfood.presentation.viewmodel.ScanViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private val viewModel: ScanViewModel by viewModels()
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val expiryColors = intArrayOf(
        //временная конструкция
        Color.parseColor("#FF6B6B"), // красный(просроченные)
        Color.parseColor("#FFA726"), // оранжевый(до 7 дней)
        Color.parseColor("#42A5F5"), // синий(до 30 дней)
        Color.parseColor("#66BB6A")  // зеленый(более 30 дней)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        observeStatistics()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeStatistics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statistics.collect { statistics ->
                updateExpiryChart(statistics.expiryDistribution)
                updateCategoryChart(statistics.categoryDistribution)
                updateSummaryText(statistics)
            }
        }
    }

    private fun updateExpiryChart(distribution: ExpiryDistribution) {
        val entries = mutableListOf<PieEntry>()

        if (distribution.total > 0) {
            if (distribution.expired > 0) {
                entries.add(PieEntry(distribution.expired.toFloat(), "Просрочено"))
            }
            if (distribution.lessThan7Days > 0) {
                entries.add(PieEntry(distribution.lessThan7Days.toFloat(), "До 7 дней"))
            }
            if (distribution.lessThan30Days > 0) {
                entries.add(PieEntry(distribution.lessThan30Days.toFloat(), "До 30 дней"))
            }
            if (distribution.moreThan30Days > 0) {
                entries.add(PieEntry(distribution.moreThan30Days.toFloat(), "Свежие"))
            }
        } else {
            entries.add(PieEntry(1f, "Нет данных"))
        }

        binding.expiredCount.text = distribution.expired.toString()
        binding.expiringWeekCount.text = distribution.lessThan7Days.toString()
        binding.expiringMonthCount.text = distribution.lessThan30Days.toString()
        binding.freshCount.text = distribution.moreThan30Days.toString()

        binding.expiredCount.setTextColor(expiryColors[0])
        binding.expiringWeekCount.setTextColor(expiryColors[1])
        binding.expiringMonthCount.setTextColor(expiryColors[2])
        binding.freshCount.setTextColor(expiryColors[3])

        val dataSet = PieDataSet(entries, "").apply {
            colors = if (distribution.total > 0) {
                val colorsList = mutableListOf<Int>()
                if (distribution.expired > 0) colorsList.add(expiryColors[0])
                if (distribution.lessThan7Days > 0) colorsList.add(expiryColors[1])
                if (distribution.lessThan30Days > 0) colorsList.add(expiryColors[2])
                if (distribution.moreThan30Days > 0) colorsList.add(expiryColors[3])
                colorsList
            } else {
                listOf(Color.GRAY)
            }
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            setDrawValues(true)
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.4f
            valueLineColor = Color.WHITE
            sliceSpace = 2f
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChartExpiry))
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }

        binding.pieChartExpiry.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            setDrawEntryLabels(false)
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(110)
            holeRadius = 45f
            transparentCircleRadius = 50f
            setDrawCenterText(true)
            centerText = if (distribution.total > 0) {
                "Всего:\n${distribution.total}"
            } else {
                "Нет данных"
            }
            setCenterTextSize(16f)
            setCenterTextColor(Color.WHITE)

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textColor = Color.WHITE
                textSize = 12f
                yEntrySpace = 5f
                yOffset = 10f
            }

            animateY(1000, Easing.EaseInOutQuad)
            this.data = data
            invalidate()
        }
    }

    private fun updateCategoryChart(categoryDistribution: Map<String, Int>) {
        val total = categoryDistribution.values.sum()
        val entries = mutableListOf<PieEntry>()

        if (total > 0) {
            val sortedCategories = categoryDistribution.toList()
                .sortedByDescending { it.second }

            sortedCategories.forEach { (category, count) ->
                val displayName = if (category.length > 15) {
                    "${category.take(12)}..."
                } else {
                    category
                }
                entries.add(PieEntry(count.toFloat(), displayName))
            }

        } else {
            entries.add(PieEntry(1f, "Нет данных"))
        }

        val categoryColors = mutableListOf<Int>()
        val predefinedColors = listOf(
            //временная конструкция
            Color.parseColor("#FF6B6B"), // красный
            Color.parseColor("#4ECDC4"), // бирюзовый
            Color.parseColor("#FFD166"), // желтый
            Color.parseColor("#06D6A0"), // зеленый
            Color.parseColor("#118AB2"), // синий
            Color.parseColor("#EF476F"), // розовый
            Color.parseColor("#7209B7"), // фиолетовый
            Color.parseColor("#F3722C"), // оранжевый
            Color.parseColor("#43AA8B"), // особый синий
            Color.parseColor("#073B4C"), // темно-синий
        )

        repeat(entries.size) { index ->
            categoryColors.add(predefinedColors[index % predefinedColors.size])
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = if (total > 0) categoryColors else listOf(Color.GRAY)
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            setDrawValues(true)
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1Length = 0.0f //подобрать подходящее значение
            valueLinePart2Length = 0.0f //подобрать подходящее значение
            valueLineColor = Color.WHITE
            sliceSpace = 2f
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChartCategories))
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }

        binding.pieChartCategories.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            setDrawEntryLabels(false)
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(110)
            holeRadius = 45f
            transparentCircleRadius = 50f
            setDrawCenterText(true)
            centerText = if (total > 0) {
                "Всего:\n${total}"
            } else {
                "Нет данных"
            }
            setCenterTextSize(16f)
            setCenterTextColor(Color.WHITE)

            legend.isEnabled = false

            createCustomLegend(entries, categoryColors)

            animateY(1000, Easing.EaseInOutQuad)
            this.data = data
            invalidate()
        }
    }

    private fun createCustomLegend(entries: List<PieEntry>, colors: List<Int>) {
        val legendContainer = binding.legendContainer
        legendContainer.removeAllViews()

        val context = requireContext()
        val layoutInflater = LayoutInflater.from(context)

        entries.forEachIndexed { index, entry ->
            val legendItem = layoutInflater.inflate(
                R.layout.legend_item,
                legendContainer,
                false
            ) as LinearLayout

            val colorView = legendItem.findViewById<View>(R.id.colorView)
            val textView = legendItem.findViewById<TextView>(R.id.legendText)

            colorView.setBackgroundColor(colors[index])
            textView.text = entry.label
            textView.setTextColor(Color.WHITE)

            legendContainer.addView(legendItem)
        }
    }

    private fun updateSummaryText(statistics: StatisticsData) {
        val totalProducts = statistics.expiryDistribution.total
        val totalCategories = statistics.categoryDistribution.size

        binding.summaryText.text = when {
            totalProducts == 0 -> "Нет отсканированных продуктов"
            totalProducts == 1 -> "1 продукт в истории"
            totalProducts <= 4 -> "$totalProducts продукта в истории"
            else -> "$totalProducts продуктов в истории"
        }

        if (totalProducts > 0) {
            binding.categoryCountText.text = "Категорий: $totalCategories"
            binding.categoryCountText.visibility = View.VISIBLE
        } else {
            binding.categoryCountText.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}