package com.nunchuk.android.signer.ss.confirm

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.nunchuk.android.arch.BaseActivity
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.nav.NunchukNavigator
import com.nunchuk.android.signer.R
import com.nunchuk.android.signer.databinding.ActivityConfirmSeedBinding
import com.nunchuk.android.signer.ss.confirm.ConfirmSeedEvent.*
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.setLightStatusBar
import javax.inject.Inject

class ConfirmSeedActivity : BaseActivity() {

    @Inject
    lateinit var factory: NunchukFactory

    @Inject
    lateinit var navigator: NunchukNavigator

    private val viewModel: ConfirmSeedViewModel by lazy {
        ViewModelProviders.of(this, factory).get(ConfirmSeedViewModel::class.java)
    }

    private lateinit var binding: ActivityConfirmSeedBinding

    private val args: ConfirmSeedArgs by lazy { ConfirmSeedArgs.deserializeFrom(intent) }

    private lateinit var adapter: ConfirmSeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        binding = ActivityConfirmSeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        observeEvent()

        viewModel.init(args.mnemonic)
    }

    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
        viewModel.state.observe(this, ::handleState)
    }

    private fun handleState(state: ConfirmSeedState) {
        adapter.items = state.groups
    }

    private fun handleEvent(event: ConfirmSeedEvent) {
        when (event) {
            ConfirmSeedCompletedEvent -> openSetNameScreen()
            SelectedIncorrectWordEvent -> NCToastMessage(this).showError(getString(R.string.nc_ssigner_confirm_seed_error))
        }
    }

    private fun openSetNameScreen() {
        navigator.openAddSoftwareSignerNameScreen(this, args.mnemonic)
    }

    private fun setupViews() {
        adapter = ConfirmSeedAdapter(viewModel::updatePhraseWordGroup)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.btnContinue.setOnClickListener { viewModel.handleContinueEvent() }
    }

    companion object {

        fun start(activityContext: Context, mnemonic: String) {
            activityContext.startActivity(ConfirmSeedArgs(mnemonic = mnemonic).buildIntent(activityContext))
        }
    }

}