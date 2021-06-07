package com.nunchuk.android.transaction.export

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProviders
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.qr.convertToQRCode
import com.nunchuk.android.transaction.databinding.ActivityExportTransactionBinding
import com.nunchuk.android.transaction.export.ExportTransactionEvent.ExportTransactionError
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.setLightStatusBar
import javax.inject.Inject

class ExportTransactionActivity : BaseActivity() {

    @Inject
    lateinit var factory: NunchukFactory

    private val argsExport: ExportTransactionArgs by lazy { ExportTransactionArgs.deserializeFrom(intent) }

    private lateinit var bitmaps: List<Bitmap>

    private var index = 0

    private val viewModel: ExportTransactionViewModel by lazy {
        ViewModelProviders.of(this, factory).get(ExportTransactionViewModel::class.java)
    }

    private lateinit var binding: ActivityExportTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        binding = ActivityExportTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeEvent()
        viewModel.init(walletId = argsExport.walletId, txId = argsExport.txId)
    }

    private val updateTextTask = object : Runnable {
        override fun run() {
            handler.postDelayed(this, INTERVAL)
            bindQrCodes()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTextTask)
    }

    private fun bindQrCodes() {
        calculateIndex()
        binding.qrCode.setImageBitmap(bitmaps[index])
    }

    private fun calculateIndex() {
        index++
        if (index >= bitmaps.size) {
            index = 0
        }
    }

    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
        viewModel.state.observe(this, ::handleState)
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleState(state: ExportTransactionState) {
        if (state.qrcode.isNotEmpty()) {
            bitmaps = state.qrcode.mapNotNull(String::convertToQRCode)
            handler.post(updateTextTask)
        }
    }

    private fun handleEvent(event: ExportTransactionEvent) {
        if (event is ExportTransactionError) {
            NCToastMessage(this).showError(event.message)
        }
    }

    companion object {

        const val INTERVAL = 500L

        private var handler = Handler(Looper.getMainLooper())

        fun start(activityContext: Activity, walletId: String, txId: String) {
            activityContext.startActivity(
                ExportTransactionArgs(
                    walletId = walletId,
                    txId = txId
                ).buildIntent(activityContext)
            )
        }

    }

}

