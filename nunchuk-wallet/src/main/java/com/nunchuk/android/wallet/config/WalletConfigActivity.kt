package com.nunchuk.android.wallet.config

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.nunchuk.android.arch.BaseActivity
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.core.signer.toModel
import com.nunchuk.android.core.util.getConfiguration
import com.nunchuk.android.model.SingleSigner
import com.nunchuk.android.model.Wallet
import com.nunchuk.android.nav.NunchukNavigator
import com.nunchuk.android.type.WalletType
import com.nunchuk.android.wallet.R
import com.nunchuk.android.wallet.config.WalletConfigEvent.UpdateNameErrorEvent
import com.nunchuk.android.wallet.config.WalletConfigEvent.UpdateNameSuccessEvent
import com.nunchuk.android.wallet.databinding.ActivityWalletConfigBinding
import com.nunchuk.android.wallet.util.toReadableString
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.setLightStatusBar
import javax.inject.Inject

class WalletConfigActivity : BaseActivity() {

    @Inject
    lateinit var factory: NunchukFactory

    private val viewModel: WalletConfigViewModel by lazy {
        ViewModelProviders.of(this, factory).get(WalletConfigViewModel::class.java)
    }

    @Inject
    lateinit var navigator: NunchukNavigator

    private lateinit var binding: ActivityWalletConfigBinding

    private val args: WalletConfigArgs by lazy { WalletConfigArgs.deserializeFrom(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        binding = ActivityWalletConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        observeEvent()
        viewModel.init(args.walletId)

    }

    private fun observeEvent() {
        viewModel.state.observe(this, ::handleState)
        viewModel.event.observe(this, ::handleEvent)
    }

    private fun handleEvent(event: WalletConfigEvent) {
        when (event) {
            UpdateNameSuccessEvent -> showEditWalletSuccess()
            is UpdateNameErrorEvent -> NCToastMessage(this).showWarning(event.message)
        }
    }

    private fun handleState(wallet: Wallet) {
        binding.walletName.text = wallet.name

        val configutation = "${wallet.getConfiguration()} ${getString(R.string.nc_wallet_multisig)}"
        binding.multisigConfigutation.text = configutation

        binding.walletType.text = (if (wallet.escrow) WalletType.ESCROW else WalletType.MULTI_SIG).toReadableString(this)
        binding.addressType.text = wallet.addressType.toReadableString(this)

        SignersViewBinder(binding.signersContainer, wallet.signers.map(SingleSigner::toModel)).bindItems()
    }

    private fun setupViews() {
        binding.walletName.setOnClickListener { onEditClicked() }
        binding.btnDone.setOnClickListener {
            navigator.openMainScreen(this)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun onEditClicked() {
        val bottomSheet = WalletUpdateBottomSheet.show(
            fragmentManager = supportFragmentManager,
            walletName = binding.walletName.text.toString()
        )

        bottomSheet.setListener(viewModel::handleEditCompleteEvent)
    }

    private fun showEditWalletSuccess() {
        binding.root.post { NCToastMessage(this).show(R.string.nc_text_change_wallet_success) }
    }

    companion object {

        fun start(activityContext: Context, walletId: String) {
            activityContext.startActivity(WalletConfigArgs(walletId = walletId).buildIntent(activityContext))
        }
    }

}