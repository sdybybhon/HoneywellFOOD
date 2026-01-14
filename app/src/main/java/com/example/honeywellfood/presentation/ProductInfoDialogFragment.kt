package com.example.honeywellfood.presentation

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.honeywellfood.R
import com.example.honeywellfood.databinding.DialogProductInfoBinding
import com.example.honeywellfood.domain.model.ProductCategory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ProductInfoDialogFragment : DialogFragment() {

    interface OnDialogActionListener {
        fun onProductSaved(productName: String, category: String?, expiryDate: Long, barcode: String)
        fun onDialogCanceled()
    }

    private var listener: OnDialogActionListener? = null
    private var _binding: DialogProductInfoBinding? = null
    private val binding get() = _binding!!
    private var selectedExpiryDate: Calendar? = null
    private var wasSaved = false
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val datePattern = Pattern.compile("^(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[0-2])\\.\\d{4}$")

    fun setListener(listener: OnDialogActionListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogProductInfoBinding.inflate(requireActivity().layoutInflater)

        val barcode = arguments?.getString("BARCODE") ?: ""
        val productName = arguments?.getString("PRODUCT_NAME") ?: ""

        binding.etProductName.setText(productName)

        val categories = listOf("Не выбрано") + ProductCategory.allCategories

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
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.setPopupBackgroundResource(R.color.background_dark)

        binding.etProductName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                binding.etExpiryDate.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.etExpiryDate.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                validateAndParseDate(binding.etExpiryDate.text.toString())
                binding.btnSave.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.etExpiryDate.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingHyphen = false
            private var hyphenStart = 0
            private var deletingBackward = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isFormatting) return

                if (count == 1 && after == 0) {
                    val charToDelete = s?.getOrNull(start)
                    if (charToDelete == '.') {
                        deletingHyphen = true
                        hyphenStart = start
                        if (start > 0 && s[start - 1].isDigit()) {
                            deletingBackward = true
                        }
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFormatting) return

                val text = s?.toString() ?: ""
                if (text.isEmpty()) return

                val cleanString = text.replace(".", "")

                val formatted = StringBuilder()
                for (i in cleanString.indices) {
                    if (i == 2 || i == 4) {
                        formatted.append('.')
                    }
                    if (i < 8) {
                        formatted.append(cleanString[i])
                    }
                }

                isFormatting = true
                binding.etExpiryDate.setText(formatted.toString())

                var cursorPos = start
                if (deletingHyphen) {
                    if (deletingBackward) {
                        cursorPos = maxOf(hyphenStart - 1, 0)
                    } else {
                        cursorPos = hyphenStart
                    }
                    deletingHyphen = false
                    deletingBackward = false
                } else {
                    when {
                        count == 1 && before == 0 -> {
                            when (start) {
                                2, 5 -> cursorPos = start + 2
                                else -> cursorPos = start + 1
                            }
                        }
                        count == 0 && before == 1 -> {
                            cursorPos = maxOf(start, 0)
                        }
                    }
                }

                binding.etExpiryDate.setSelection(minOf(cursorPos, formatted.length))
                isFormatting = false

                if (formatted.length == 10) {
                    validateAndParseDate(formatted.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tilExpiryDate.setStartIconOnClickListener {
            hideKeyboard()
            showDatePicker()
        }

        binding.tilExpiryDate.setEndIconOnClickListener {
            binding.etExpiryDate.setText("")
            selectedExpiryDate = null
            binding.tilExpiryDate.error = null
        }

        binding.btnSave.setOnClickListener {
            hideKeyboard()
            saveProduct(barcode)
        }

        binding.btnCancel.setOnClickListener {
            hideKeyboard()
            dismissWithCancel()
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }

        binding.etProductName.requestFocus()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    private fun dismissWithCancel() {
        listener?.onDialogCanceled()
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!wasSaved) {
            listener?.onDialogCanceled()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etProductName.windowToken, 0)
        binding.etProductName.clearFocus()
    }

    private fun saveProduct(barcode: String) {
        val productNameInput = binding.etProductName.text.toString().trim()

        val selectedCategory = binding.spinnerCategory.selectedItem as? String
        val category = if (selectedCategory != "Не выбрано") selectedCategory else null

        val dateInput = binding.etExpiryDate.text.toString().trim()

        if (productNameInput.isBlank()) {
            Toast.makeText(requireContext(), "Введите название продукта", Toast.LENGTH_SHORT).show()
            binding.etProductName.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etProductName, InputMethodManager.SHOW_IMPLICIT)
            return
        }

        if (dateInput.isBlank()) {
            binding.tilExpiryDate.error = "Введите срок годности"
            binding.etExpiryDate.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etExpiryDate, InputMethodManager.SHOW_IMPLICIT)
            return
        }

        if (selectedExpiryDate == null) {
            val dateValid = validateAndParseDate(dateInput)
            if (!dateValid) {
                return
            }
        }

        val expiryDate = selectedExpiryDate?.timeInMillis ?: return

        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        if (selectedExpiryDate!!.before(now)) {
            binding.tilExpiryDate.error = "Дата не может быть в прошлом"
            binding.etExpiryDate.requestFocus()
            return
        }

        wasSaved = true
        listener?.onProductSaved(productNameInput, category, expiryDate, barcode)
        dismiss()
    }

    private fun validateAndParseDate(dateString: String): Boolean {
        binding.tilExpiryDate.error = null

        if (!datePattern.matcher(dateString).matches()) {
            binding.tilExpiryDate.error = "Неверный формат даты (дд.мм.гггг)"
            return false
        }

        try {
            dateFormat.isLenient = false
            val parsedDate = dateFormat.parse(dateString)

            if (parsedDate != null) {
                val day = dateString.substring(0, 2).toInt()
                val month = dateString.substring(3, 5).toInt()
                val year = dateString.substring(6, 10).toInt()

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)

                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                if (day > maxDay) {
                    binding.tilExpiryDate.error = "Неверный день для месяца"
                    return false
                }

                calendar.set(Calendar.DAY_OF_MONTH, day)
                selectedExpiryDate = calendar
                return true
            }
        } catch (e: ParseException) {
            binding.tilExpiryDate.error = "Неверная дата"
            return false
        } catch (e: NumberFormatException) {
            binding.tilExpiryDate.error = "Неверный формат числа"
            return false
        }

        return false
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
                    set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val formattedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etExpiryDate.setText(formattedDate)
                binding.tilExpiryDate.error = null
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