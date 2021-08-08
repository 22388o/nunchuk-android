package com.nunchuk.android.messages.components.create

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nunchuk.android.core.base.BaseViewHolder
import com.nunchuk.android.core.util.shorten
import com.nunchuk.android.messages.R
import com.nunchuk.android.messages.databinding.ItemSuggestContactBinding
import com.nunchuk.android.model.Contact
import com.nunchuk.android.widget.util.inflate

internal class ContactsAdapter(
    private val listener: (Contact) -> Unit
) : RecyclerView.Adapter<ContactViewHolder>() {

    internal var items: List<Contact> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContactViewHolder(
        parent.inflate(R.layout.item_suggest_contact),
        listener
    )

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

}

internal class ContactViewHolder(
    itemView: View,
    val listener: (Contact) -> Unit
) : BaseViewHolder<Contact>(itemView) {

    private val binding = ItemSuggestContactBinding.bind(itemView)

    override fun bind(data: Contact) {
        binding.avatar.text = data.name.shorten()
        binding.name.text = data.name
        binding.email.text = data.email
        binding.root.setOnClickListener { listener(data) }
    }

}