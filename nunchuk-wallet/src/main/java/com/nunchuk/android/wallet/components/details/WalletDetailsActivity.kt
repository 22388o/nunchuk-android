package com.nunchuk.android.wallet.components.details

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.core.qr.convertToQRCode
import com.nunchuk.android.core.share.IntentSharingController
import com.nunchuk.android.core.util.*
import com.nunchuk.android.wallet.R
import com.nunchuk.android.wallet.components.details.WalletDetailsEvent.*
import com.nunchuk.android.wallet.components.details.WalletDetailsOption.*
import com.nunchuk.android.wallet.databinding.ActivityWalletDetailBinding
import com.nunchuk.android.wallet.util.bindWalletConfiguration
import com.nunchuk.android.widget.NCToastMessage
import javax.inject.Inject

class WalletDetailsActivity : BaseActivity<ActivityWalletDetailBinding>() {

    @Inject
    lateinit var factory: NunchukFactory

    private val controller: IntentSharingController by lazy { IntentSharingController.from(this) }

    private val viewModel: WalletDetailsViewModel by viewModels { factory }

    private lateinit var adapter: TransactionAdapter

    private val args: WalletDetailsArgs by lazy { WalletDetailsArgs.deserializeFrom(intent) }

    override fun initializeBinding() = ActivityWalletDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViews()
        observeEvent()
        viewModel.init(args.walletId)
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncData()
    }

    private fun observeEvent() {
        viewModel.state.observe(this, ::handleState)
        viewModel.event.observe(this, ::handleEvent)
    }

    private fun handleEvent(event: WalletDetailsEvent) {
        when (event) {
            is WalletDetailsError -> onGetWalletError(event)
            is SendMoneyEvent -> openInputAmountScreen(event)
            is UpdateUnusedAddress -> bindUnusedAddress(event.address)
            is OpenDynamicQRScreen -> navigator.openDynamicQRScreen(this, event.descriptors)
            is UploadWalletConfigEvent -> shareConfigurationFile(event.filePath)
            is BackupWalletDescriptorEvent -> shareDescriptor(event.descriptor)
            is Loading -> showOrHideLoading(event.loading)
            DeleteWalletSuccess -> walletDeleted()
        }
    }

    private fun onGetWalletError(event: WalletDetailsError) {
        hideLoading()
        NCToastMessage(this).showError(event.message)
    }

    private fun openInputAmountScreen(event: SendMoneyEvent) {
        navigator.openInputAmountScreen(
            activityContext = this,
            walletId = args.walletId,
            availableAmount = event.amount.pureBTC()
        )
    }

    private fun walletDeleted() {
        NCToastMessage(this).showMessage(getString(R.string.nc_wallet_delete_wallet_success))
        finish()
    }

    private fun bindUnusedAddress(address: String) {
        if (address.isEmpty()) {
            binding.emptyTxContainer.isVisible = false
        } else {
            binding.emptyTxContainer.isVisible = true
            binding.addressQR.setImageBitmap(address.convertToQRCode())
            binding.addressText.text = address
        }
    }

    private fun handleState(state: WalletDetailsState) {
        val wallet = state.wallet

        binding.toolbarTitle.text = wallet.name
        binding.configuration.bindWalletConfiguration(wallet)

        binding.btcAmount.text = wallet.getBTCAmount()
        binding.cashAmount.text = wallet.getUSDAmount()
        binding.btnSend.isClickable = state.wallet.balance.value > 0

        adapter.items = state.transactions
        val emptyTransactions = state.transactions.isEmpty()
        binding.emptyTxContainer.isVisible = emptyTransactions
        binding.transactionTitle.isVisible = !emptyTransactions
        binding.transactionList.isVisible = !emptyTransactions
    }

    private fun setupViews() {
        adapter = TransactionAdapter {
            navigator.openTransactionDetailsScreen(this, args.walletId, it.txId)
        }
        binding.transactionList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.transactionList.isNestedScrollingEnabled = false
        binding.transactionList.adapter = adapter

        binding.viewWalletConfig.setUnderline()
        binding.viewWalletConfig.setOnClickListener {
            navigator.openWalletConfigScreen(this, args.walletId)
        }
        binding.btnReceive.setOnClickListener { navigator.openReceiveTransactionScreen(this, args.walletId) }
        binding.btnSend.setOnClickListener { viewModel.handleSendMoneyEvent() }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.toolbar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.menu_search -> {
                    Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_more -> {
                    onMoreClicked()
                    true
                }
                else -> false
            }
        }
    }

    private fun shareDescriptor(descriptor: String) {
        controller.shareText(descriptor)
    }

    private fun shareConfigurationFile(filePath: String) {
        controller.shareFile(filePath)
    }

    private fun onMoreClicked() {
        val bottomSheet = WalletUpdateBottomSheet.show(fragmentManager = supportFragmentManager)
        bottomSheet.setListener {
            when (it) {
                BACKUP -> handleBackupWallet()
                UPLOAD -> handleUploadWallet()
                QR -> viewModel.handleExportWalletQR()
                DELETE -> viewModel.handleDeleteWallet()
            }
        }
    }

    private fun handleUploadWallet() {
        if (checkReadExternalPermission()) {
            viewModel.handleUploadWallet()
        }
    }

    private fun handleBackupWallet() {
        if (checkReadExternalPermission()) {
            viewModel.handleBackupWallet()
        }
    }

    companion object {

        fun start(activityContext: Context, walletId: String) {
            activityContext.startActivity(WalletDetailsArgs(walletId = walletId).buildIntent(activityContext))
        }

    }

}