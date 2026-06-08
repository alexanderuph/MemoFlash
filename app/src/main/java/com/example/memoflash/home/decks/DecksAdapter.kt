package com.example.memoflash.home.decks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memoflash.R
import com.example.memoflash.core.model.StudyDeck
import com.example.memoflash.databinding.ItemDeckBinding

class DecksAdapter(
    private val onDeckClick: (StudyDeck) -> Unit
) : ListAdapter<StudyDeck, DecksAdapter.DeckViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = ItemDeckBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeckViewHolder(
        private val binding: ItemDeckBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(deck: StudyDeck) {
            binding.txtDeckTitle.text = deck.title
            binding.txtDeckSubject.text = deck.subject
            binding.txtDeckDescription.text = deck.description
            binding.txtDeckMeta.text = binding.root.resources.getQuantityString(
                R.plurals.deck_card_count,
                deck.cards.size,
                deck.cards.size
            )
            binding.root.setOnClickListener { onDeckClick(deck) }
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<StudyDeck>() {
            override fun areItemsTheSame(oldItem: StudyDeck, newItem: StudyDeck): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: StudyDeck, newItem: StudyDeck): Boolean =
                oldItem == newItem
        }
    }
}
