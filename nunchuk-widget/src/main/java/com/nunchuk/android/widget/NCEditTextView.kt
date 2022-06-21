package com.nunchuk.android.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleObserver
import com.nunchuk.android.widget.databinding.NcEditTextViewBinding

const val DEFAULT_VALUE = 0
const val NORMAL_THEME: Int = 0
const val TEXT_NO_SUGGESTION: Int = 4
const val TEXT_READ_ONLY_TYPE: Int = 3
const val TEXT_MULTI_LINE_TYPE: Int = 2
const val NUMBER_TYPE: Int = 1
const val TEXT_TYPE: Int = 0
const val GRAVITY_CENTER_VERTICAL = 10
const val GRAVITY_TOP = 11

class NCEditTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), LifecycleObserver {

    private var titleId: Int = DEFAULT_VALUE
    private var title: String? = null
    private var titleColor: Int = ContextCompat.getColor(context, R.color.nc_primary_color)
    private var hintText: String? = null
    private var titleSize: Float = resources.getDimension(R.dimen.nc_text_size_12)
    private var hintTextColor: Int = ContextCompat.getColor(context, R.color.nc_third_color)
    private var hintTextId: Int = DEFAULT_VALUE
    private var editTextColor: Int = ContextCompat.getColor(context, R.color.nc_black_color)
    private var inputType: Int = TEXT_TYPE
    private var editTextSize: Float = resources.getDimension(R.dimen.nc_text_size_16)
    private var editTextBackground: Int = R.drawable.nc_edit_text_bg
    private var editHigh: Float = resources.getDimension(R.dimen.nc_height_44)
    private var editTheme: Int = NORMAL_THEME
    private var editGravity: Int = GRAVITY_CENTER_VERTICAL
    private val binding = NcEditTextViewBinding.inflate(LayoutInflater.from(context), this)

    var onSuffixIconClicked: (() -> Unit)? = null

    init {
        retrieveAttributes(context, attrs)
        onBindView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun retrieveAttributes(context: Context, attributeSet: AttributeSet?) {
        val attr = context.obtainStyledAttributes(attributeSet, R.styleable.NCEditTextView, 0, 0)
        titleId = attr.getResourceId(R.styleable.NCEditTextView_edit_title, DEFAULT_VALUE)
        if (titleId == DEFAULT_VALUE) {
            title = attr.getString(R.styleable.NCEditTextView_edit_title)
        }

        hintTextId = attr.getResourceId(R.styleable.NCEditTextView_edit_hint, DEFAULT_VALUE)
        if (hintTextId == DEFAULT_VALUE) {
            hintText = attr.getString(R.styleable.NCEditTextView_edit_hint)
        }

        titleColor = attr.getColor(R.styleable.NCEditTextView_edit_title_color, ContextCompat.getColor(context, R.color.nc_primary_color))
        titleSize = attr.getDimension(R.styleable.NCEditTextView_edit_title_text_size, resources.getDimension(R.dimen.nc_text_size_12))
        hintTextColor = attr.getColor(R.styleable.NCEditTextView_edit_hint_color, ContextCompat.getColor(context, R.color.nc_third_color))
        editTextColor = attr.getColor(R.styleable.NCEditTextView_edit_text_color, ContextCompat.getColor(context, R.color.nc_black_color))
        inputType = attr.getInteger(R.styleable.NCEditTextView_edit_input_type, TEXT_TYPE)
        editTextSize = attr.getDimension(R.styleable.NCEditTextView_edit_text_size, resources.getDimension(R.dimen.nc_text_size_16))
        editTextBackground = attr.getResourceId(R.styleable.NCEditTextView_edit_background, R.drawable.nc_edit_text_bg)
        editHigh = attr.getDimension(R.styleable.NCEditTextView_edit_height, resources.getDimension(R.dimen.nc_height_44))
        editTheme = attr.getInteger(R.styleable.NCEditTextView_edit_theme, NORMAL_THEME)
        editGravity = attr.getInteger(R.styleable.NCEditTextView_edit_gravity, GRAVITY_CENTER_VERTICAL)

        val suffixIconId = attr.getResourceId(R.styleable.NCEditTextView_edit_suffix_icon, DEFAULT_VALUE)
        if (suffixIconId != DEFAULT_VALUE) {
            binding.editText.setCompoundDrawablesWithIntrinsicBounds(0,0,suffixIconId,0)
            binding.editText.setOnTouchListener { v, event ->
                if(event.action == MotionEvent.ACTION_UP) {
                    if(event.rawX >= (binding.editText.right - binding.editText.compoundDrawables[2].bounds.width())) {
                        onSuffixIconClicked?.invoke()
                        return@setOnTouchListener true
                    }
                }
                return@setOnTouchListener  false
            }
        }
        attr.recycle()
    }

    private fun onBindView() {
        if (titleId == DEFAULT_VALUE) {
            if (title.isNullOrEmpty()) {
                binding.textView.visibility = View.GONE
            } else {
                binding.textView.visibility = View.VISIBLE
                binding.textView.text = title
            }
        } else {
            binding.textView.visibility = View.VISIBLE
            binding.textView.setText(titleId)
        }

        if (hintTextId == DEFAULT_VALUE) {
            binding.editText.hint = hintText
        } else {
            binding.editText.setHint(hintTextId)
        }

        binding.textView.setTextColor(titleColor)
        binding.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize)
        binding.editText.setHintTextColor(hintTextColor)
        binding.editText.setTextColor(editTextColor)
        binding.editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize)

        when (inputType) {
            TEXT_TYPE -> {
                binding.editText.inputType = EditorInfo.TYPE_CLASS_TEXT
                binding.editText.setSingleLine()
            }
            NUMBER_TYPE -> {
                binding.editText.inputType = EditorInfo.TYPE_CLASS_PHONE
                binding.editText.setSingleLine()
            }
            TEXT_MULTI_LINE_TYPE -> {
                binding.editText.inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                binding.editText.isSingleLine = false
            }
            TEXT_READ_ONLY_TYPE -> {
                binding.editText.inputType = InputType.TYPE_NULL
                binding.editText.setTextIsSelectable(false)
            }
            TEXT_NO_SUGGESTION -> {
                binding.editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
        }

        val padding12 = context.resources.getDimensionPixelOffset(R.dimen.nc_padding_12)
        val padding10 = context.resources.getDimensionPixelOffset(R.dimen.nc_padding_10)

        if (editGravity == GRAVITY_TOP) {
            binding.editText.gravity = Gravity.TOP
            binding.editText.setPadding(padding12, padding10, padding12, padding10)
        } else {
            binding.editText.gravity = Gravity.CENTER_VERTICAL
            binding.editText.setPadding(padding12, 0, padding12, 0)
        }

        binding.editText.setBackgroundResource(editTextBackground)
        binding.editText.layoutParams.height = editHigh.toInt()
    }

    fun getTextView(): TextView = binding.textView

    fun getEditText(): String = binding.editText.text.toString()
    fun getEditTextView(): EditText = binding.editText

    fun hideError() {
        binding.editText.background = ResourcesCompat.getDrawable(resources, R.drawable.nc_edit_text_bg, null)
        binding.errorText.visibility = View.GONE
        binding.errorText.text = ""
    }

    fun setError(message: String) {
        binding.editText.background = ResourcesCompat.getDrawable(resources, R.drawable.nc_edit_text_error_bg, null)
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}