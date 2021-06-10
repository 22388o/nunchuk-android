package com.nunchuk.android.transaction.send.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.nunchuk.android.arch.ext.isVisible
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.qr.QRCodeParser
import com.nunchuk.android.qr.startQRCodeScan
import com.nunchuk.android.transaction.R
import com.nunchuk.android.transaction.databinding.ActivityTransactionAddReceiptBinding
import com.nunchuk.android.transaction.send.receipt.AddReceiptEvent.*
import com.nunchuk.android.widget.util.addTextChangedCallback
import com.nunchuk.android.widget.util.setLightStatusBar
import com.nunchuk.android.widget.util.setMaxLength
import javax.inject.Inject

class AddReceiptActivity : BaseActivity() {

    @Inject
    lateinit var factory: NunchukFactory

    private val args: AddReceiptArgs by lazy { AddReceiptArgs.deserializeFrom(intent) }

    private val viewModel: AddReceiptViewModel by lazy {
        ViewModelProviders.of(this, factory).get(AddReceiptViewModel::class.java)
    }

    private lateinit var binding: ActivityTransactionAddReceiptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        binding = ActivityTransactionAddReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeEvent()
        viewModel.init()
    }


    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
        viewModel.state.observe(this, ::handleState)
    }

    private fun setupViews() {
        binding.receiptInput.addTextChangedCallback(viewModel::handleReceiptChanged)
        binding.privateNoteInput.setMaxLength(MAX_NOTE_LENGTH)
        binding.privateNoteInput.addTextChangedCallback(viewModel::handlePrivateNoteChanged)

        binding.qrCode.setOnClickListener { startQRCodeScan() }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            viewModel.handleContinueEvent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        QRCodeParser.parse(requestCode, resultCode, data)?.apply {
            binding.receiptInput.setText(this)
        }
    }

    private fun handleState(state: AddReceiptState) {
        binding.privateNoteCounter.text = "${state.privateNote.length}/$MAX_NOTE_LENGTH"
    }

    private fun handleEvent(event: AddReceiptEvent) {
        when (event) {
            is AcceptedAddressEvent -> openEstimatedFeeScreen(event.address, event.privateNote)
            AddressRequiredEvent -> showAddressRequiredError()
            InvalidAddressEvent -> showInvalidAddressError()
        }
    }

    private fun showInvalidAddressError() {
        showError(getString(R.string.nc_transaction_invalid_address))
    }

    private fun showAddressRequiredError() {
        showError(getString(R.string.nc_text_required))
    }

    private fun showError(message: String) {
        binding.errorText.isVisible = true
        binding.errorText.text = message
    }

    private fun hideError() {
        binding.errorText.isVisible = false
    }

    private fun openEstimatedFeeScreen(address: String, privateNote: String) {
        hideError()
        navigator.openEstimatedFeeScreen(
            activityContext = this,
            walletId = args.walletId,
            outputAmount = args.outputAmount,
            availableAmount = args.availableAmount,
            address = address,
            privateNote = privateNote
        )
    }

    companion object {
        private const val MAX_NOTE_LENGTH = 80

        fun start(activityContext: Context, walletId: String, outputAmount: Double, availableAmount: Double) {
            activityContext.startActivity(
                AddReceiptArgs(
                    walletId = walletId,
                    outputAmount = outputAmount,
                    availableAmount = availableAmount
                ).buildIntent(activityContext)
            )
        }

    }

}