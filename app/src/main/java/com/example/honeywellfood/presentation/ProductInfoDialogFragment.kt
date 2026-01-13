package com.example.honeywellfood.presentation

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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

        binding.etProductName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                binding.btnSave.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }

        binding.btnPickDate.setOnClickListener {
            hideKeyboard()
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            hideKeyboard()
            saveProduct(barcode)
        }

        binding.btnCancel.setOnClickListener {
            hideKeyboard()
            dismiss()
        }

        binding.etProductName.requestFocus()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etProductName.windowToken, 0)
        binding.etProductName.clearFocus()
    }

    private fun saveProduct(barcode: String) {
        val productNameInput = binding.etProductName.text.toString().trim()
        val expiryDate = selectedExpiryDate?.timeInMillis

        if (productNameInput.isBlank()) {
            Toast.makeText(requireContext(), "Введите название продукта", Toast.LENGTH_SHORT).show()
            binding.etProductName.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etProductName, InputMethodManager.SHOW_IMPLICIT)
            return
        }

        if (expiryDate == null) {
            Toast.makeText(requireContext(), "Выберите срок годности", Toast.LENGTH_SHORT).show()
            return
        }

        listener?.onSubmit(productNameInput, expiryDate, barcode)
        dismiss()
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