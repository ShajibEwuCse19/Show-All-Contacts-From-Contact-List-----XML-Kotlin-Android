package com.shajib.chooseandselectcontract

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shajib.chooseandselectcontract.databinding.ViewItemBinding

/**
 * @author Shajib
 * @since Aug 28, 2024
 **/
class ContactInformationAdapter(private val contactList: ArrayList<Pair<String, String>>) :
    RecyclerView.Adapter<ContactInformationAdapter.ContactInformationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactInformationViewHolder {
        return ContactInformationViewHolder(
            ViewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    override fun onBindViewHolder(holder: ContactInformationViewHolder, position: Int) {
        holder.setData(contactList[position])
    }

    inner class ContactInformationViewHolder(private val binding: ViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal fun setData(contactInfo: Pair<String, String>) {
            binding.tvName.text = contactInfo.first
            binding.tvNumber.text = contactInfo.second
        }
    }

    //add contacts from a single page [pagination]
    fun addContacts(contacts: List<Pair<String, String>>) {
        val startPosition = contactList.size
        contactList.addAll(contacts)
        val endPosition = contactList.size
        notifyItemRangeInserted(startPosition, endPosition)
    }
}