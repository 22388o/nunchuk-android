package com.nunchuk.android.messages.room.detail

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nunchuk.android.arch.vm.ViewModelFactory
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.messages.databinding.ActivityRoomDetailBinding
import com.nunchuk.android.messages.room.detail.RoomDetailEvent.ContactNotFoundEvent
import com.nunchuk.android.messages.room.detail.RoomDetailEvent.RoomNotFoundEvent
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.setLightStatusBar
import com.nunchuk.android.widget.util.setOnEnterListener
import javax.inject.Inject

class RoomDetailActivity : BaseActivity() {

    @Inject
    lateinit var factory: ViewModelFactory

    private val viewModel: RoomDetailViewModel by viewModels { factory }

    private val args: RoomDetailArgs by lazy { RoomDetailArgs.deserializeFrom(intent) }

    private lateinit var binding: ActivityRoomDetailBinding

    private lateinit var roomAdapter: RoomDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        setupViews()

        viewModel.initialize(args.roomId)

        observeEvent()
    }

    private fun observeEvent() {
        viewModel.state.observe(this, ::handleState)
        viewModel.event.observe(this, ::handleEvent)
    }

    private fun handleState(state: RoomDetailState) {
        binding.toolbarTitle.text = state.roomInfo.roomName
        binding.memberCount.text = "${state.roomInfo.memberCount} members"
        roomAdapter.messages = ArrayList(state.messages)
        if (state.messages.isNotEmpty()) {
            binding.recyclerView.scrollToPosition(roomAdapter.messages.size - 1)
        }
    }

    private fun handleEvent(event: RoomDetailEvent) {
        when (event) {
            RoomNotFoundEvent -> finishWithMessage("Room not found!")
            ContactNotFoundEvent -> finishWithMessage("Contact not found!")
        }
    }

    private fun finishWithMessage(message: String) {
        NCToastMessage(this).showError(message)
        finish()
    }

    private fun setupViews() {
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.send.setOnClickListener { sendMessage() }
        binding.editText.setOnEnterListener(::sendMessage)

        roomAdapter = RoomDetailsAdapter(this)
        binding.recyclerView.adapter = roomAdapter
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun sendMessage() {
        val content = binding.editText.text.toString()
        if (content.trim().isNotBlank()) {
            viewModel.handleSendMessage(content)
            runOnUiThread {
                binding.editText.setText("")
            }
        }
    }

    companion object {
        fun start(activityContext: Context, roomId: String) {
            activityContext.startActivity(RoomDetailArgs(roomId = roomId).buildIntent(activityContext))
        }
    }
}

