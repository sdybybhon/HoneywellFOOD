package com.example.honeywellfood.presentation

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.honeywellfood.R
import com.example.honeywellfood.databinding.DialogProductInfoBinding
import java.util.*

class ProductInfoDialogFragment : DialogFragment() {

    interface OnProductInfoSubmitListener {
        fun onSubmit(productName: String, expiryDate: Long, barcode: String)
    }

    private var listener: OnProductInfoSubmitListener? = null
    private var _binding: DialogProductInfoBinding? = null
    private val binding get() = _binding!!
    private var selectedExpiryDate: Calendar? = null

    fun setListener(listener: OnProductInfoSubmitListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogProductInfoBinding.inflate(requireActivity().layoutInflater)

        val barcode = arguments?.getString("BARCODE") ?: ""
        val productName = arguments?.getString("PRODUCT_NAME") ?: ""

        binding.etProductName.setText(productName)

        binding.btnPickDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            val productNameInput = binding.etProductName.text.toString().trim()
            val expiryDate = selectedExpiryDate?.timeInMillis

            if (productNameInput.isBlank()) {
                Toast.makeText(requireContext(), "Введите название продукта", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expiryDate == null) {
                Toast.makeText(requireContext(), "Выберите срок годности", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            listener?.onSubmit(productNameInput, expiryDate, barcode)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedExpiryDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                val formattedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.tvSelectedDate.text = formattedDate
            },
            year,
            month,
            day
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(barcode: String, productName: String? = null): ProductInfoDialogFragment {
            return ProductInfoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("BARCODE", barcode)
                    putString("PRODUCT_NAME", productName ?: "")
                }
            }
        }
    }
}