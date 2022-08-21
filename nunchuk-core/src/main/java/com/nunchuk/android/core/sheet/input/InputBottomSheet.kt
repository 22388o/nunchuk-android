package com.nunchuk.android.core.sheet.input

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.nunchuk.android.arch.args.FragmentArgs
import com.nunchuk.android.core.base.BaseBottomSheet
import com.nunchuk.android.core.databinding.DialogInputBottomSheetBinding
import com.nunchuk.android.core.util.setUnderline
import com.nunchuk.android.widget.util.addTextChangedCallback

class InputBottomSheet : BaseBottomSheet<DialogInputBottomSheetBinding>() {

    private lateinit var listener: InputBottomSheetListener

    private val args: InputBottomSheetArgs by lazy { InputBottomSheetArgs.deserializeFrom(arguments) }

    override fun initializeBinding(inflater: LayoutInflater, container: ViewGroup?): DialogInputBottomSheetBinding {
        return DialogInputBottomSheetBinding.inflate(inflater, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (activity is InputBottomSheetListener) {
            activity as InputBottomSheetListener
        } else if (parentFragment is InputBottomSheetListener) {
            parentFragment as InputBottomSheetListener
        } else {
            throw IllegalArgumentException("activity or parentFragment must implement InputBottomSheetListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.title.text = args.title
        binding.edit.setText(args.currentInput)
        binding.btnSave.setUnderline()

        binding.edit.addTextChangedCallback {
            binding.btnSave.isVisible = it.isNotEmpty()
        }

        binding.iconClose.setOnClickListener {
            onCloseClicked()
        }
        binding.btnSave.setOnClickListener {
            onSaveClicked()
        }
    }

    private fun onSaveClicked() {
        val newInput = binding.edit.text.toString()
        if (newInput != args.currentInput) {
            listener.onInputDone(newInput)
        }
        dismiss()
    }

    private fun onCloseClicked() {
        dismiss()
    }

    companion object {
        private const val TAG = "InputBottomSheet"

        private fun newInstance(currentInput: String, title: String) = InputBottomSheet().apply {
            arguments = InputBottomSheetArgs(title, currentInput).buildBundle()
        }

        fun show(fragmentManager: FragmentManager, currentInput: String, title: String): InputBottomSheet {
            return newInstance(currentInput, title).apply { show(fragmentManager, TAG) }
        }
    }
}

interface InputBottomSheetListener {
    fun onInputDone(newInput: String)
}

data class InputBottomSheetArgs(val title: String, val currentInput: String) : FragmentArgs {

    override fun buildBundle() = Bundle().apply {
        putString(EXTRA_CURRENT_INPUT, currentInput)
        putString(EXTRA_TITLE, title)
    }

    companion object {
        private const val EXTRA_CURRENT_INPUT = "EXTRA_CURRENT_INPUT"
        private const val EXTRA_TITLE = "EXTRA_TITLE"

        fun deserializeFrom(data: Bundle?) = InputBottomSheetArgs(
            data?.getString(EXTRA_TITLE).orEmpty(),
            data?.getString(EXTRA_CURRENT_INPUT).orEmpty(),
        )
    }
}