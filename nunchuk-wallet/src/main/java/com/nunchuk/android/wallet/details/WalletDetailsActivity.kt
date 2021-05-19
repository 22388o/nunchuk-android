package com.nunchuk.android.wallet.details

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.nunchuk.android.arch.BaseActivity
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.core.util.getBTCAmount
import com.nunchuk.android.core.util.getConfiguration
import com.nunchuk.android.core.util.getUSDAmount
import com.nunchuk.android.core.util.observe
import com.nunchuk.android.nav.NunchukNavigator
import com.nunchuk.android.wallet.R
import com.nunchuk.android.wallet.databinding.ActivityWalletDetailBinding
import com.nunchuk.android.wallet.details.WalletDetailsEvent.WalletDetailsError
import javax.inject.Inject

class WalletDetailsActivity : BaseActivity() {

    @Inject
    lateinit var factory: NunchukFactory

    private val viewModel: WalletDetailsViewModel by lazy {
        ViewModelProviders.of(this, factory).get(WalletDetailsViewModel::class.java)
    }

    @Inject
    lateinit var navigator: NunchukNavigator

    private lateinit var binding: ActivityWalletDetailBinding

    private val args: WalletDetailsArgs by lazy { WalletDetailsArgs.deserializeFrom(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWalletDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        observeEvent()
        viewModel.init(args.walletId)

    }

    private fun observeEvent() {
        viewModel.state.observe(this, ::handleState)
        viewModel.event.observe(this, ::handleEvent)
    }

    private fun handleEvent(event: WalletDetailsEvent) {
        when (event) {
            is WalletDetailsError -> Toast.makeText(applicationContext, event.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleState(state: WalletDetailsState) {
        val wallet = state.wallet
        val configutation = "${wallet.getConfiguration()} ${getString(R.string.nc_wallet_multisig)}"
        binding.multisigConfiguration.text = configutation
        binding.btcAmount.text = wallet.getBTCAmount()
        binding.cashAmount.text = wallet.getUSDAmount()
    }

    private fun setupViews() {
        binding.viewWalletConfig.setOnClickListener { navigator.openWalletConfigScreen(this, args.walletId) }
        binding.btnReceive.setOnClickListener { navigator.openReceiveTransactionScreen(this, args.walletId) }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    companion object {

        fun start(activityContext: Context, walletId: String) {
            activityContext.startActivity(WalletDetailsArgs(walletId = walletId).buildIntent(activityContext))
        }
    }

}