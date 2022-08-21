package com.nunchuk.android.wallet.components.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nunchuk.android.core.base.BaseFragment
import com.nunchuk.android.core.constants.RoomAction
import com.nunchuk.android.core.qr.convertToQRCode
import com.nunchuk.android.core.share.IntentSharingController
import com.nunchuk.android.core.sheet.BottomSheetOption
import com.nunchuk.android.core.sheet.BottomSheetOptionListener
import com.nunchuk.android.core.sheet.SheetOption
import com.nunchuk.android.core.sheet.SheetOptionType
import com.nunchuk.android.core.util.*
import com.nunchuk.android.share.model.TransactionOption
import com.nunchuk.android.share.wallet.bindWalletConfiguration
import com.nunchuk.android.wallet.R
import com.nunchuk.android.wallet.components.details.WalletDetailsEvent.*
import com.nunchuk.android.wallet.databinding.FragmentWalletDetailBinding
import com.nunchuk.android.widget.NCToastMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WalletDetailsFragment : BaseFragment<FragmentWalletDetailBinding>(), BottomSheetOptionListener {

    @Inject
    lateinit var textUtils: TextUtils

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            activity?.onBackPressed()
        }
    }

    private val controller: IntentSharingController by lazy { IntentSharingController.from(requireActivity()) }

    private val viewModel: WalletDetailsViewModel by viewModels()

    private val adapter: TransactionAdapter by lazy {
        TransactionAdapter {
            navigator.openTransactionDetailsScreen(
                activityContext = requireActivity(),
                walletId = args.walletId,
                txId = it.txId,
                initEventId = viewModel.getRoomWallet()?.initEventId.orEmpty(),
                roomId = viewModel.getRoomWallet()?.roomId.orEmpty()
            )
        }
    }

    private val args: WalletDetailsArgs by lazy { WalletDetailsArgs.deserializeFrom(requireArguments()) }

    override fun initializeBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWalletDetailBinding {
        return FragmentWalletDetailBinding.inflate(inflater, container, false)
    }

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.isSweepBalance) {
            NCToastMessage(requireActivity()).show(getString(R.string.nc_satscard_sweeped))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeEvent()
        viewModel.init(args.walletId, args.isSweepBalance)
    }

    override fun onResume() {
        super.onResume()
        showLoading()
        viewModel.syncData()
    }

    override fun onOptionClicked(option: SheetOption) {
        when (option.type) {
            SheetOptionType.TYPE_IMPORT_PSBT -> handleImportPSBT()
            SheetOptionType.TYPE_IMPORT_PSBT_QR -> showSubImportPsbtViaQr()
            SheetOptionType.TYPE_PSBT_QR_KEY_STONE -> openImportTransactionScreen(TransactionOption.IMPORT_KEYSTONE)
            SheetOptionType.TYPE_PSBT_QR_PASSPORT -> openImportTransactionScreen(TransactionOption.IMPORT_PASSPORT)
            SheetOptionType.TYPE_SAVE_WALLET_CONFIG -> handleExportBSMS()
        }
    }

    private fun setupPaginationAdapter() {
        binding.transactionList.adapter = adapter.withLoadStateFooter(LoadStateAdapter())
        adapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.Loading -> {}
                is LoadState.Error -> hideLoading()
                is LoadState.NotLoading -> hideLoading()
            }
        }
        lifecycleScope.launch {
            adapter.loadStateFlow.distinctUntilChangedBy(CombinedLoadStates::refresh)
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.transactionList.scrollToPosition(0) }
        }
    }

    private fun paginateTransactions() {
        adapter.submitData(lifecycle, PagingData.empty())
        job?.cancel()
        job = lifecycleScope.launch(Dispatchers.IO) {
            viewModel.paginateTransactions()
                .catch { hideLoading() }
                .collectLatest(adapter::submitData)
        }
    }

    private fun observeEvent() {
        viewModel.state.observe(viewLifecycleOwner, ::handleState)
        viewModel.event.observe(viewLifecycleOwner, ::handleEvent)
    }

    private fun handleEvent(event: WalletDetailsEvent) {
        when (event) {
            is WalletDetailsError -> onGetWalletError(event)
            is SendMoneyEvent -> openInputAmountScreen(event)
            is UpdateUnusedAddress -> bindUnusedAddress(event.address)
            is UploadWalletConfigEvent -> shareConfigurationFile(event.filePath)
            is Loading -> showOrHideLoading(event.loading)
            ImportPSBTSuccess -> onPSBTImported()
            is PaginationTransactions -> startPagination(event.hasTransactions)
        }
    }

    private fun startPagination(hasTx: Boolean) {
        hideLoading()
        binding.emptyTxContainer.isVisible = !hasTx
        binding.transactionTitle.isVisible = hasTx
        binding.transactionList.isVisible = hasTx
        if (hasTx) {
            paginateTransactions()
        }
    }

    private fun onPSBTImported() {
        viewModel.syncData()
        hideLoading()
        NCToastMessage(requireActivity()).showMessage(getString(R.string.nc_wallet_psbt_imported))
    }

    private fun onGetWalletError(event: WalletDetailsError) {
        hideLoading()
        NCToastMessage(requireActivity()).showError(event.message)
    }

    private fun openInputAmountScreen(event: SendMoneyEvent) {
        if (event.walletExtended.isShared) {
            navigator.openRoomDetailActivity(
                activityContext = requireActivity(),
                roomId = event.walletExtended.roomWallet!!.roomId,
                roomAction = RoomAction.SEND
            )
        } else {
            navigator.openInputAmountScreen(
                activityContext = requireActivity(),
                walletId = args.walletId,
                availableAmount = event.walletExtended.wallet.balance.pureBTC()
            )
        }
    }

    private fun bindUnusedAddress(address: String) {
        hideLoading()
        if (address.isEmpty()) {
            binding.emptyTxContainer.isVisible = false
        } else {
            binding.emptyTxContainer.isVisible = true
            binding.addressQR.setImageBitmap(address.convertToQRCode())
            binding.addressText.text = address
        }
    }

    private fun handleState(state: WalletDetailsState) {
        val wallet = state.walletExtended.wallet

        binding.toolbarTitle.text = wallet.name
        binding.configuration.bindWalletConfiguration(wallet)

        binding.btcAmount.text = wallet.getBTCAmount()
        binding.cashAmount.text = wallet.getUSDAmount()
        binding.btnSend.isClickable = wallet.balance.value > 0

        binding.shareIcon.isVisible = state.walletExtended.isShared
    }

    private fun setupViews() {
        binding.transactionList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.transactionList.isNestedScrollingEnabled = false
        binding.transactionList.setHasFixedSize(false)
        binding.transactionList.adapter = adapter

        binding.viewWalletConfig.setUnderline()
        binding.viewWalletConfig.setOnClickListener {
            navigator.openWalletConfigScreen(launcher, requireActivity(), args.walletId)
        }
        binding.btnReceive.setOnClickListener { navigator.openReceiveTransactionScreen(requireActivity(), args.walletId) }
        binding.btnSend.setOnClickListener { viewModel.handleSendMoneyEvent() }
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.menu_search -> {
                    Toast.makeText(requireActivity(), "Coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_more -> {
                    onMoreClicked()
                    true
                }
                else -> false
            }
        }

        binding.btnCopy.setOnClickListener { copyAddress(binding.addressText.text.toString()) }
        binding.btnShare.setOnClickListener { controller.shareText(binding.addressText.text.toString()) }

        setupPaginationAdapter()
    }

    private fun shareConfigurationFile(filePath: String) {
        controller.shareFile(filePath)
    }

    private fun onMoreClicked() {
        val options = listOf(
            SheetOption(SheetOptionType.TYPE_IMPORT_PSBT, R.drawable.ic_import, R.string.nc_wallet_import_psbt),
            SheetOption(SheetOptionType.TYPE_IMPORT_PSBT_QR, R.drawable.ic_import, R.string.nc_import_psbt_via_qr),
            SheetOption(SheetOptionType.TYPE_SAVE_WALLET_CONFIG, R.drawable.ic_backup, R.string.nc_wallet_save_wallet_configuration),
        )
        val bottomSheet = BottomSheetOption.newInstance(options)
        bottomSheet.show(childFragmentManager, "BottomSheetOption")
    }

    private fun showSubImportPsbtViaQr() {
        val options = listOf(
            SheetOption(SheetOptionType.TYPE_PSBT_QR_KEY_STONE, R.drawable.ic_import, R.string.nc_wallet_import_keystone_seed_signer),
            SheetOption(SheetOptionType.TYPE_PSBT_QR_PASSPORT, R.drawable.ic_import, R.string.nc_wallet_import_passport),
        )
        val bottomSheet = BottomSheetOption.newInstance(options)
        bottomSheet.show(childFragmentManager, "BottomSheetOption")
    }

    private fun openImportTransactionScreen(transactionOption: TransactionOption) {
        navigator.openImportTransactionScreen(
            activityContext = requireActivity(),
            walletId = args.walletId,
            transactionOption = transactionOption
        )
    }

    private fun handleExportBSMS() {
        if (requireActivity().checkReadExternalPermission()) {
            viewModel.handleExportBSMS()
        }
    }

    private fun handleImportPSBT() {
        if (requireActivity().checkReadExternalPermission()) {
            requireActivity().openSelectFileChooser()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == CHOOSE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            intent?.data?.let {
                getFileFromUri(requireActivity().contentResolver, it, requireActivity().cacheDir)
            }?.absolutePath?.let(viewModel::handleImportPSBT)
        }
    }

    private fun copyAddress(address: String) {
        textUtils.copyText(text = address)
        NCToastMessage(requireActivity()).showMessage(getString(R.string.nc_address_copy_to_clipboard))
    }
}